package org.brapi.schematools.core.brapischema;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import graphql.com.google.common.collect.Streams;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    private static final Pattern REF_PATTERN = Pattern.compile("((?:\\.{1,2}+/)*(?:[\\w-]+\\/)*(?:\\w+).json)#\\/\\$defs\\/(\\w+)");
    private static final List<String> COMMON_MODULES = List.of("Schemas", "Parameters", "Requests");

    private final JsonSchemaFactory factory;
    private final ObjectMapper objectMapper;

    public BrAPISchemaReader() {
        factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        objectMapper = new ObjectMapper();
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
            return find(schemaDirectory, 3, this::schemaPathMatcher).flatMap(this::createBrAPISchemas).collect(Response.toList()).
                getResultOrThrow(response -> new RuntimeException(response.getMessagesCombined(",")));
        } catch (IOException | RuntimeException e) {
            throw new BrAPISchemaReaderException(e);
        }
    }

    /**
     * Reads a single object type from an JSON schema. If the JSON schema
     * contain more than one type definition only the first is returned
     *
     * @param schemaPath a JSON schema file
     * @param module     the module in which the object resides
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
     *
     * @param path   the path of the schema is used to check references, if not supplied then validation is not performed
     * @param schema a JSON schema string
     * @param module the module in which the object resides
     * @return BrAPIObjectType with one type per JSON Schema
     * @throws BrAPISchemaReaderException if there is a problem reading the JSON schema
     */
    public BrAPIObjectType readSchema(Path path, String schema, String module) throws BrAPISchemaReaderException {
        try {
            return createBrAPISchemas(path, objectMapper.readTree(schema), module).collect(Response.toList()).mapResult(list -> list.get(0)).
                getResultOrThrow(response -> new RuntimeException(response.getMessagesCombined(",")));
        } catch (RuntimeException | JsonProcessingException e) {
            throw new BrAPISchemaReaderException(e);
        }
    }

    private Stream<Response<BrAPIObjectType>> createBrAPISchemas(Path path) {
        return createBrAPISchemas(path, findModule(path));
    }

    private String findModule(Path path) {
        String module = path.getParent().getFileName().toString();

        return COMMON_MODULES.contains(module) ? null : module;
    }

    private Stream<Response<BrAPIObjectType>> createBrAPISchemas(Path path, String module) {
        JsonSchema schema = factory.getSchema(path.toUri());

        JsonNode json = schema.getSchemaNode();

        return createBrAPISchemas(path, json, module);
    }

    private Stream<Response<BrAPIObjectType>> createBrAPISchemas(Path path, JsonNode json, String module) {
        JsonNode defs = json.get("$defs");

        if (defs != null) {
            json = defs;
        }

        Iterator<Map.Entry<String, JsonNode>> iterator = json.fields();
        return Stream.generate(() -> null)
            .takeWhile(x -> iterator.hasNext())
            .map(n -> iterator.next()).map(entry -> createObjectType(path, entry.getValue(), entry.getKey(), module).
                mapResult(result -> (BrAPIObjectType) result));
    }

    private boolean schemaPathMatcher(Path path, BasicFileAttributes basicFileAttributes) {
        return basicFileAttributes.isRegularFile();
    }

    private Response<BrAPIType> createType(Path path, JsonNode jsonNode, String fallbackName, String module) {

        boolean isEnum = jsonNode.has("enum");

        return findChildNode(jsonNode, "$ref", false).ifPresentMapResultToResponseOr(
            ref -> createReferenceType(path, ref),

            () -> findStringList(jsonNode, "type", true).
                mapResultToResponse(types -> {

                    if (types.contains("object")) {
                        if (isEnum) {
                            return fail(Response.ErrorType.VALIDATION, String.format("Object Type '%s' can not be an enum!", fallbackName));
                        }

                        if (jsonNode.has("oneOf")) {
                            return createOneOfType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                        } else {
                            return createObjectType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                        }
                    }

                    if (types.contains("array")) {
                        if (isEnum) {
                            return fail(Response.ErrorType.VALIDATION, String.format("Array Type '%s' can not be an enum!", fallbackName));
                        }

                        return createArrayType(path, jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), module);
                    }

                    if (types.contains("string")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "string", module);
                        } else {
                            return success(BrAPIPrimitiveType.STRING);
                        }
                    }

                    if (types.contains("integer")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "integer", module);
                        } else {
                            return success(BrAPIPrimitiveType.INTEGER);
                        }
                    }

                    if (types.contains("number")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "number", module);
                        } else {
                            return success(BrAPIPrimitiveType.NUMBER);
                        }
                    }

                    if (types.contains("boolean")) {
                        if (isEnum) {
                            return createEnumType(jsonNode, findNameFromTitle(jsonNode).getResultIfPresentOrElseResult(fallbackName), "boolean", module);
                        } else {
                            return success(BrAPIPrimitiveType.BOOLEAN);
                        }
                    }

                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type(s) '%s' in node '%s'", types, jsonNode));

                }));

    }

    private Response<String> findNameFromTitle(JsonNode jsonNode) {
        return findString(jsonNode, "title", false).mapResult(name -> name != null ? name.replace(" ", "") : null);
    }

    private Response<BrAPIType> createReferenceType(Path path, JsonNode jsonNode) {

        BrAPIReferenceType.BrAPIReferenceTypeBuilder builder = BrAPIReferenceType.builder();

        return findString(jsonNode).
            mapResultToResponse(ref -> parseRef(path, ref)).
            onSuccessDoWithResult(builder::name).
            map(() -> success(builder.build()));
    }

    private Response<String> parseRef(Path path, String ref) {
        Matcher matcher = REF_PATTERN.matcher(ref);

        if (matcher.matches()) {

            if (path != null) {
                Path refPath = path.getParent().resolve(matcher.group(1));

                if (!refPath.toFile().isFile()) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Can not find json file '%s' referenced in '%s'", refPath, path));
                }
            }
            return success(matcher.group(2));
        } else {
            return fail(Response.ErrorType.VALIDATION, String.format("Ref '%s' does not match ref pattern '%s'", ref, REF_PATTERN));
        }
    }

    private Response<BrAPIType> createArrayType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIArrayType.BrAPIArrayTypeBuilder builder = BrAPIArrayType.builder().name(name);

        return findChildNode(jsonNode, "items", true).
            mapResultToResponse(childNode -> createType(path, childNode, String.format("%sItem", name), module).
                onSuccessDoWithResult(builder::items)).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIType> createObjectType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIObjectType.BrAPIObjectTypeBuilder builder = BrAPIObjectType.builder().
            name(name).
            request(name.endsWith("Request")).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        List<String> required = findStringList(jsonNode, "required", false).getResultIfPresentOrElseResult(Collections.emptyList());

        List<BrAPIObjectProperty> properties = new ArrayList<>();
        return Response.empty().
            mapOnCondition(jsonNode.has("additionalProperties"), () -> findChildNode(jsonNode, "additionalProperties", false).
                mapResultToResponse(additionalPropertiesNode -> createProperty(path, additionalPropertiesNode, "additionalProperties",
                    module, required.contains("additionalProperties")).onSuccessDoWithResult(properties::add))).
            mapOnCondition(jsonNode.has("properties"), () -> findChildNode(jsonNode, "properties", false).
                mapResult(JsonNode::fields).
                mapResultToResponse(fields -> createProperties(path, fields, module, required)).
                onSuccessDoWithResult(properties::addAll)).
            onSuccessDo(() -> builder.properties(properties)).
            mapOnCondition(jsonNode.has("brapi-metadata"), () -> findChildNode(jsonNode, "brapi-metadata", false).
                mapResultToResponse(this::parseMetadata).onSuccessDoWithResult(builder::metadata)).
            map(() -> success(builder.build()));
    }



    private Response<List<BrAPIObjectProperty>> createProperties(Path path, Iterator<Map.Entry<String, JsonNode>> fields, String module, List<String> required) {
        return Streams.stream(fields).map(field -> createProperty(path, field.getValue(), field.getKey(), module, required.contains(field.getKey()))).collect(Response.toList());
    }

    private Response<BrAPIObjectProperty> createProperty(Path path, JsonNode jsonNode, String name, String module, boolean required) {

        BrAPIObjectProperty.BrAPIObjectPropertyBuilder builder = BrAPIObjectProperty.builder().
            name(name).
            required(required);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return createType(path, jsonNode, StringUtils.toSentenceCase(name), module).
            onSuccessDoWithResult(builder::type).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIMetadata> parseMetadata(JsonNode metadata) {
        BrAPIMetadata.BrAPIMetadataBuilder builder = BrAPIMetadata.builder();

        return findBoolean(metadata, "primaryModel", false).
            onSuccessDoWithResult(builder::primaryModel).
            map(() -> success(builder.build()));
    }

    private Response<BrAPIType> createOneOfType(Path path, JsonNode jsonNode, String name, String module) {

        BrAPIOneOfType.BrAPIOneOfTypeBuilder builder = BrAPIOneOfType.builder().
            name(name).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return findChildNode(jsonNode, "oneOf", true).
            mapResult(this::childNodes).
            mapResultToResponse(childNodes -> childNodes.mapResultToResponse(nodes -> createPossibleTypes(path, nodes, name, module))).
            onSuccessDoWithResult(builder::possibleTypes).
            map(() -> success(builder.build()));
    }

    private Response<List<BrAPIType>> createPossibleTypes(Path path, List<JsonNode> jsonNodes, String fallbackNamePrefix, String module) {

        AtomicInteger i = new AtomicInteger();

        return jsonNodes.stream().map(jsonNode -> createType(path, jsonNode, String.format("%s%d", fallbackNamePrefix, i.incrementAndGet()), module)).collect(Response.toList());
    }

    private Response<BrAPIType> createEnumType(JsonNode jsonNode, String name, String type, String module) {

        BrAPIEnumType.BrAPIEnumTypeBuilder builder = BrAPIEnumType.builder().
            name(name).
            type(type).
            module(module);

        findString(jsonNode, "description", false).
            onSuccessDoWithResult(builder::description);

        return findStringList(jsonNode, "enum", true).
            mapResultToResponse(strings -> createEnumValues(strings, type)).
            onSuccessDoWithResult(builder::values).
            map(() -> success(builder.build()));
    }

    private Response<List<BrAPIEnumValue>> createEnumValues(List<String> strings, String type) {
        return strings.stream().map(string -> createEnumValue(string, type)).collect(Response.toList());
    }

    private Response<BrAPIEnumValue> createEnumValue(String string, String type) {
        BrAPIEnumValue.BrAPIEnumValueBuilder builder = BrAPIEnumValue.builder().
            name(string);

        try {
            return switch (type) {
                case "string" -> success(builder.value(string).build());
                case "integer" -> success(builder.value(Integer.valueOf(string)).build());
                case "number" -> success(builder.value(Float.valueOf(string)).build());
                case "boolean" -> success(builder.value(Boolean.valueOf(string)).build());
                default ->
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", type));
            };
        } catch (NumberFormatException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not convert '%s' to type '%s'", string, type));
        }
    }

    private Response<String> findString(JsonNode parentNode, String fieldName, boolean required) {
        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof TextNode textNode) {
                return success(textNode.asText());
            }
            return required ?
                fail(Response.ErrorType.VALIDATION,
                    String.format("Child node type '%s' was not TextNode with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
                Response.empty();
        });
    }

    private Response<Boolean> findBoolean(JsonNode parentNode, String fieldName, boolean required) {
        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof BooleanNode booleanNode) {
                return success(booleanNode.asBoolean());
            }
            return required ?
                fail(Response.ErrorType.VALIDATION,
                    String.format("Child node type '%s' was not BooleanNode with field name '%s' for parent node '%s'", jsonNode.getClass().getName(), parentNode, fieldName)) :
                Response.empty();
        });
    }

    private Response<List<String>> findStringList(JsonNode parentNode, String fieldName, boolean required) {

        return findChildNode(parentNode, fieldName, required).mapResultToResponse(jsonNode -> {
            if (jsonNode instanceof ArrayNode arrayNode) {
                return StreamSupport.stream(arrayNode.spliterator(), false).
                    filter(childNode -> !(childNode instanceof NullNode)).
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
            String.format("Parent Node type '%s' is not ArrayNode", parentNode.getClass().getName()));
    }

}
