package org.brapi.schematools.core.brapischema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import graphql.com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.model.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.Files.find;
import static java.util.Collections.singletonList;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

@AllArgsConstructor
public class BrAPISchemaReader {

  private static final Pattern FILE_NAME_PATTERN = Pattern.compile("^(\\w+)\\.json$");
  private static final Pattern REF_PATTERN = Pattern.compile("^(\\w+).json#\\/\\$defs\\/(\\w+)$");

  private final JsonSchemaFactory factory ;
  private final ObjectMapper objectMapper ;

  public BrAPISchemaReader() {
    factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    objectMapper = new ObjectMapper() ;
  }

  /**
   * Reads the schema module directories within a parent directory.
   * Each directory in the parent directory is a module and the JSON schemas in the directories are object types
   *
   * @param schemaDirectory the parent directory that holds all the module directories
   * @return a list of BrAPIObjectType with one type per JSON Schema
   * @throws BrAPISchemaReaderException if there is a problem reading the directories or JSON schemas
   */
  public List<BrAPIObjectType> readDirectories(Path schemaDirectory) throws BrAPISchemaReaderException {
    try {
      return find(schemaDirectory, 2, this::schemaPathMatcher).flatMap(this::createBrAPISchemas).collect(Response.toList()).
              getResultOrThrow(response -> new RuntimeException(response.getMessagesCombined(",")));
    } catch (IOException | RuntimeException e) {
      throw new BrAPISchemaReaderException(e);
    }
  }

  /**
   * Reads a single object type from an JSON schema. If the JSON schema
   * contain more than one type definition only the first is returned
   * @param schemaPath a JSON schema file
   * @param module the module in which the object resides
   * @return BrAPIObjectType with one type per JSON Schema
   * @throws BrAPISchemaReaderException if there is a problem reading the JSON schema
   */
  public BrAPIObjectType readSchema(Path schemaPath, String module) throws BrAPISchemaReaderException {
    try {
      return createBrAPISchemas(schemaPath, module).collect(Response.toList()).mapResult(list -> list.get(0)).
              getResultOrThrow(response -> new RuntimeException(response.getMessagesCombined(",")));
    } catch (RuntimeException e) {
      throw new BrAPISchemaReaderException(e);
    }
  }

  /**
   * Reads a single object type from an JSON schema string. If the JSON schema
   * contain more than one type definition only the first is returned
   * @param schema a JSON schema string
   * @param module the module in which the object resides
   * @return BrAPIObjectType with one type per JSON Schema
   * @throws BrAPISchemaReaderException if there is a problem reading the JSON schema
   */
  public BrAPIObjectType readSchema(String schema, String module) throws BrAPISchemaReaderException {
    try {
      return createBrAPISchemas(objectMapper.readTree(schema), module).collect(Response.toList()).mapResult(list -> list.get(0)).
              getResultOrThrow(response -> new RuntimeException(response.getMessagesCombined(",")));
    } catch (RuntimeException | JsonProcessingException e) {
      throw new BrAPISchemaReaderException(e);
    }
  }

  private Stream<Response<BrAPIObjectType>> createBrAPISchemas(Path path) {
    return createBrAPISchemas(path, path.getParent().getFileName().toString()) ;
  }

  private Stream<Response<BrAPIObjectType>> createBrAPISchemas(Path path, String module) {
    JsonSchema schema = factory.getSchema(path.toUri());

    JsonNode json = schema.getSchemaNode() ;

    return createBrAPISchemas(json, module) ;
  }

  private Stream<Response<BrAPIObjectType>> createBrAPISchemas(JsonNode json, String module) {
    JsonNode defs = json.get("$defs");

    if (defs != null) {
      json = defs ;
    }

    Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();
    return Stream.generate(() -> null)
            .takeWhile(x -> iterator.hasNext())
            .map(n -> iterator.next()).map(entry -> createObjectType(entry.getValue(), entry.getKey(), module).
                    mapResult(result -> (BrAPIObjectType)result));
  }

  private boolean schemaPathMatcher(Path path, BasicFileAttributes basicFileAttributes) {
    return basicFileAttributes.isRegularFile() ;
  }

  private Response<BrAPIType> createType(JsonNode jsonNode, String fallbackName, String module) {

    return findChildNode(jsonNode, "$ref", false).ifPresentMapResultToResponseOr(
            this::createReferenceType,

            () -> findStringList(jsonNode, "type", true).
                    mapResultToResponse(types -> {

                      if (types.contains("object")) {
                        if (jsonNode.has("oneOf")) {
                          return createOneOfType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                        } else  {
                          return createObjectType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                        }
                      }

                      if (types.contains("array")) {
                        return createArrayType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                      }

                      if (types.contains("string")) {
                        return success(BrAPIPrimitiveType.STRING);
                      }

                      if (types.contains("integer")) {
                        return success(BrAPIPrimitiveType.INTEGER);
                      }

                      if (types.contains("number")) {
                        return success(BrAPIPrimitiveType.NUMBER);
                      }

                      if (types.contains("boolean")) {
                        return success(BrAPIPrimitiveType.BOOLEAN);
                      }

                      return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type(s) '%s' in node '%s'", types, jsonNode));

                    }));

  }

  private Response<String> findName(JsonNode jsonNode) {
    return findString(jsonNode, "title", false).mapResult(name -> name != null ? name.replace(" ", "") : null ) ;
  }

  private String findName(Path path) {
    Matcher matcher = FILE_NAME_PATTERN.matcher(path.getFileName().toString());

    if (matcher.matches()) {
      return matcher.group(1) ;
    } else {
      return path.getFileName().toString() ;
    }
  }

  private Response<BrAPIType> createReferenceType(JsonNode jsonNode) {

    BrAPIReferenceType.BrAPIReferenceTypeBuilder builder = BrAPIReferenceType.builder() ;

    return findString(jsonNode).
            mapResultToResponse(this::parseRef).
            onSuccessDoWithResult(builder::name).
            map(() -> success(builder.build()));
  }

  private Response<String> parseRef(String ref) {
    Matcher matcher = REF_PATTERN.matcher(ref);

    if (matcher.matches()) {
      return success(matcher.group(2)) ;
    } else {
      return fail(Response.ErrorType.VALIDATION, String.format("Ref '%s' does not match ref pattern '%s'", ref, REF_PATTERN)) ;
    }
  }

  private Response<BrAPIType> createArrayType(JsonNode jsonNode, String name, String module) {

    BrAPIArrayType.BrAPIArrayTypeBuilder builder = BrAPIArrayType.builder().name(name);

    return findChildNode(jsonNode, "items", true).
            mapResultToResponse(childNode -> createType(childNode, String.format("%sItem", name), module).
                    onSuccessDoWithResult(builder::items)).
            map(() -> success(builder.build()));
  }

  private Response<BrAPIType> createObjectType(JsonNode jsonNode, String name, String module) {

    BrAPIObjectType.BrAPIObjectTypeBuilder builder = BrAPIObjectType.builder().
            name(name).
            module(module);

    findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

    List<String> required = findStringList(jsonNode, "required", false).getResultIfPresentOrElseResult(Collections.emptyList());

    List<BrAPIObjectProperty> properties = new ArrayList<>() ;
    return Response.empty().
            mapOnCondition(jsonNode.has("additionalProperties"), () -> findChildNode(jsonNode, "additionalProperties", false).
                    mapResultToResponse(additionalPropertiesNode -> createProperty(additionalPropertiesNode, "additionalProperties",
                            module, required.contains("additionalProperties")).onSuccessDoWithResult(properties::add))).
            mapOnCondition(jsonNode.has("properties"), () -> findChildNode(jsonNode, "properties", false).
                    mapResult(JsonNode::fields).
                    mapResultToResponse(fields -> createProperties(fields, module, required)).
                    onSuccessDoWithResult(properties::addAll)).
            onSuccessDo(() -> builder.properties(properties)).
            map(() -> success(builder.build()));
  }

  private Response<List<BrAPIObjectProperty>> createProperties(Iterator<Map.Entry<String, JsonNode>> fields, String module, List<String> required) {
    return Streams.stream(fields).map(field -> createProperty(field.getValue(), field.getKey(), module, required.contains(field.getKey()))).collect(Response.toList());
  }

  private Response<BrAPIObjectProperty> createProperty(JsonNode jsonNode, String name, String module, boolean required) {

    BrAPIObjectProperty.BrAPIObjectPropertyBuilder builder = BrAPIObjectProperty.builder().
            name(name).
            required(required) ;

    findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

    return createType(jsonNode, StringUtils.toSentenceCase(name), module).
            onSuccessDoWithResult(builder::type).
            map(() -> success(builder.build()));
  }

  private Response<BrAPIType> createOneOfType(JsonNode jsonNode, String name, String module) {

    BrAPIOneOfType.BrAPIOneOfTypeBuilder builder = BrAPIOneOfType.builder().
            name(name);

    findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

    return findChildNode(jsonNode, "oneOf", true).
            mapResult(this::childNodes).
            mapResultToResponse(childNodes -> childNodes.mapResultToResponse(nodes -> createPossibleTypes(nodes, name, module))).
            onSuccessDoWithResult(builder::possibleTypes).
            map(() -> success(builder.build()));
  }

  private Response<List<BrAPIType>> createPossibleTypes(List<JsonNode> jsonNodes, String fallbackNamePrefix, String module) {

    AtomicInteger i = new AtomicInteger() ;

    return jsonNodes.stream().map(jsonNode -> createType(jsonNode, String.format("%s%d", fallbackNamePrefix, i.incrementAndGet()), module)).collect(Response.toList()) ;
  }

  private Response<String> findString(JsonNode parentNode, String fieldName, boolean required) {
    return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
      if (jsonNode instanceof TextNode textNode) {
        return success(textNode.asText());
      }
      return required ?
              fail(Response.ErrorType.VALIDATION,
                      String.format("Unknown child node type '%s' with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
              Response.empty();
    });
  }

  private Response<List<String>> findStringList(JsonNode parentNode, String fieldName, boolean required) {

    return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
      if (jsonNode instanceof ArrayNode arrayNode) {
        return StreamSupport.stream(arrayNode.spliterator(), false).
                map(this::findString).filter(stringResponse -> stringResponse.getResult() != null).
                collect(Response.toList());
      }

      if (jsonNode instanceof TextNode textNode) {
        return success(singletonList(textNode.asText()));
      }

      return required ?
              fail(Response.ErrorType.VALIDATION,
                      String.format("Unknown child node type '%s' with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
              Response.empty();
    });
  }

  private Response<String> findString(JsonNode jsonNode) {
    if (jsonNode instanceof TextNode textNode) {
      return success(textNode.asText());
    }

    if (jsonNode != null) {
      return fail(Response.ErrorType.VALIDATION, String.format("Node type '%s' is not string", jsonNode.getClass().getName()));
    } else {
      return Response.empty();
    }
  }

  private Response<JsonNode> findChildNode(JsonNode parentNode, String fieldName, boolean required) {
    JsonNode jsonNode = parentNode.get(fieldName);

    if (jsonNode != null) {
      return success(jsonNode);
    }

    if (required) {
      return fail(Response.ErrorType.VALIDATION, String.format("Parent node '%s' does not have child node with field name '%s'", parentNode, fieldName));
    } else {
      return Response.empty();
    }
  }

  private Response<List<JsonNode>> childNodes(JsonNode parentNode) {

    if (parentNode instanceof ArrayNode arrayNode) {
      return success(StreamSupport.stream(arrayNode.spliterator(), false).toList());
    }

    return fail(Response.ErrorType.VALIDATION,
            String.format("Parent Node type '%s' is not ArrayNode", parentNode.getClass().getName())) ;
  }

}
