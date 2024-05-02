package org.brapi.schematools.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import graphql.GraphQL;
import graphql.TypeResolutionEnvironment;
import graphql.com.google.common.collect.Streams;
import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;
import org.brapi.schematools.core.model.BrAPISchema;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static java.util.Collections.singletonList;
import static org.brapi.schematools.core.Response.fail;
import static org.brapi.schematools.core.Response.success;
import static org.brapi.schematools.core.StringUtils.toParameterCase;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class GraphQLGenerator {

  private static final Pattern REF_PATTERN = Pattern.compile("^(\\w+).json#\\/\\$defs\\/(\\w+)$");
  private final BrAPISchemaReader schemaReader;

  public GraphQLGenerator() {
    this.schemaReader = new BrAPISchemaReader();
  }

  public Response<GraphQLSchema> generate(Path schemaDirectory, GraphQLGenerator.Options options) {

    try {
      return new Generator(options, schemaReader.read(schemaDirectory)).generate();
    } catch (BrAPISchemaReaderException e) {
      return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
    }
  }

  @Getter
  public class Generator {
    private final GraphQLGenerator.Options options ;
    private final List<BrAPISchema> brAPISchemas ;

    private final Map<String, GraphQLOutputType> objectTypes;
    private final Map<String, GraphQLUnionType> unionTypes;

    private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

    public Generator(Options options, List<BrAPISchema> brAPISchemas) {
      this.options = options;
      this.brAPISchemas = brAPISchemas;
      objectTypes = new HashMap<>() ;
      unionTypes = new HashMap<>() ;
    }

    public Response<GraphQLSchema> generate() {
      return brAPISchemas.stream().
              map(this::createObjectType).
              collect(Response.toList()).mapResultToResponse(
                      this::createSchema);
    }

    private Response<GraphQLSchema> createSchema(List<GraphQLObjectType> types) {

      GraphQLSchema.Builder builder = GraphQLSchema.newSchema() ;

      if (options.isGeneratingQueryType()) {
        GraphQLObjectType.Builder query = newObject().name(options.getQueryTypeName());

        types.stream().map(this::generateSingleGraphQLQuery).forEach(query::field);

        builder.query(query);
      }

      if (options.isGeneratingMutationType()) {
        GraphQLObjectType.Builder mutation = newObject().name(options.getQueryTypeName());

        types.stream().map(this::generateSingleGraphQLMutation).forEach(mutation::field);

        builder.mutation(mutation);
      }

      Set<GraphQLType> additionalTypes = new HashSet<>(types);

      builder.additionalTypes(additionalTypes);

      unionTypes.values().forEach(graphQLType -> codeRegistry.typeResolver(graphQLType, new UnionTypeResolver(graphQLType)));

      builder.codeRegistry(codeRegistry.build());

      return success(builder.build());
    }

    private Response<GraphQLObjectType> createObjectType(BrAPISchema schema) {
      return createObjectType(schema.getSchema(), schema.getName()).
              mapResultToResponse(
                      type -> type instanceof GraphQLObjectType ?
                              success((GraphQLObjectType) type) :
                              fail(Response.ErrorType.VALIDATION, String.format("Type is '%s' and not GraphQLObjectType", type.getClass())));
    }

    private Response<GraphQLOutputType> createType(JsonNode jsonNode, String fallbackName) {

      return findChildNode(jsonNode, "$ref", false).ifPresentMapResultToResponseOr(
              this::createReferenceType,

              () -> findStringList(jsonNode, "type", true).
              mapResultToResponse(types -> {

                if (types.contains("object")) {
                 if (jsonNode.has("oneOf")) {
                    return createUnionType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName));
                  } else  {
                    return createObjectType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName));
                  }
                }

                if (types.contains("array")) {
                  return createListType(jsonNode, findName(jsonNode).getResultIfPresentOrElseResult(fallbackName));
                }

                if (types.contains("string")) {
                  return success(GraphQLString);
                }

                if (types.contains("integer")) {
                  return success(GraphQLInt);
                }

                if (types.contains("number")) {
                  return success(GraphQLFloat);
                }

                if (types.contains("boolean")) {
                  return success(GraphQLBoolean);
                }

                return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type(s) '%s' in node '%s'", types, jsonNode));

              }));

    }

    private Response<String> findName(JsonNode jsonNode) {
      return findString(jsonNode, "title", false).mapResult(name -> name != null ? name.replace(" ", "") : null ) ;
    }

    private Response<GraphQLOutputType> createReferenceType(JsonNode jsonNode) {
      return findString(jsonNode).
              mapResultToResponse(this::parseRef).
              mapResult(GraphQLTypeReference::typeRef) ;
    }

    private Response<String> parseRef(String ref) {
      Matcher matcher = REF_PATTERN.matcher(ref);

      if (matcher.matches()) {
        return success(matcher.group(2)) ;
      } else {
        return fail(Response.ErrorType.VALIDATION, String.format("Ref '%s' does not match ref pattern '%s'", ref, REF_PATTERN)) ;
      }
    }

    private Response<GraphQLOutputType> createListType(JsonNode jsonNode, String fallbackName) {

      return findChildNode(jsonNode, "items", true).
              mapResultToResponse(childNode -> createType(childNode, fallbackName)).
              mapResult(GraphQLList::list);
    }

    private Response<GraphQLOutputType> createObjectType(JsonNode jsonNode, String name) {

      GraphQLOutputType existingType = objectTypes.get(name);

      if (existingType != null) {
        return success(existingType) ;
      }

      GraphQLObjectType.Builder builder = newObject().
              name(name);

      findString(jsonNode, "description", false).
              onSuccessDoWithResult(builder::description);

      List<String> required = findStringList(jsonNode, "required", false).getResultIfPresentOrElseResult(Collections.emptyList());

      return Response.empty().
              mapOnCondition(jsonNode.has("additionalProperties"), () -> findChildNode(jsonNode, "additionalProperties", false).
                      mapResultToResponse(additionalPropertiesNode -> createFieldDefinition("additionalProperties",
                              additionalPropertiesNode, required.contains("additionalProperties")).onSuccessDoWithResult(builder::field))).
              mapOnCondition(jsonNode.has("properties"), () -> findChildNode(jsonNode, "properties", false).
                      mapResult(JsonNode::fields).
                      mapResultToResponse(fields -> createFieldDefinitions(fields, required)).
                      onSuccessDoWithResult(builder::fields)).
              map(() -> addObjectType(builder.build()));
    }

    private Response<GraphQLOutputType> addObjectType(GraphQLObjectType type) {
      objectTypes.put(type.getName(), type) ;

      return success(type);
    }

    private Response<GraphQLOutputType> addUnionType(GraphQLUnionType type) {
      unionTypes.put(type.getName(), type) ;

      return success(type);
    }

    private Response<List<GraphQLFieldDefinition>> createFieldDefinitions(Iterator<Map.Entry<String, JsonNode>> fields, List<String> required) {
      return Streams.stream(fields).map(field -> createFieldDefinition(field.getKey(), field.getValue(), required.contains(field.getKey()))).collect(Response.toList());
    }

    private Response<GraphQLFieldDefinition> createFieldDefinition(String name, JsonNode jsonNode, boolean required) throws GeneratorException {

      GraphQLFieldDefinition.Builder builder = newFieldDefinition().name(name);

      return Response.empty().
              merge(createType(jsonNode, StringUtils.toSentenceCase(name)).
                      conditionalMapResult(required, GraphQLNonNull::nonNull)).
              onSuccessDoWithResult(builder::type).
              map(() -> success(builder.build()));

    }

    private Response<GraphQLOutputType> createUnionType(JsonNode jsonNode, String name) {

      GraphQLOutputType existingType = unionTypes.get(name);

      if (existingType != null) {
        return success(existingType) ;
      }

      GraphQLUnionType.Builder builder = GraphQLUnionType.newUnionType().
              name(name);

      findString(jsonNode, "description", false).
              onSuccessDoWithResult(builder::description);

      return findChildNode(jsonNode, "oneOf", true).
              mapResult(this::childNodes).
              mapResultToResponse(childNodes -> childNodes.mapResultToResponse(nodes -> createNamedOutputTypes(nodes, name))).
              onSuccessDoWithResult(builder::replacePossibleTypes).
              map(() -> addUnionType(builder.build()));
    }

    private Response<List<GraphQLNamedOutputType>> createNamedOutputTypes(List<JsonNode> jsonNodes, String fallbackNamePrefix) {

      AtomicInteger i = new AtomicInteger() ;

      return jsonNodes.stream().map(jsonNode -> createNamedOutputType(jsonNode, String.format("%s%d", fallbackNamePrefix, i.incrementAndGet()))).collect(Response.toList()) ;
    }

    private Response<GraphQLNamedOutputType> createNamedOutputType(JsonNode jsonNode, String fallbackName) {
      try {
        return createType(jsonNode, fallbackName).mapResult(type -> (GraphQLNamedOutputType) type);
      } catch (ClassCastException e) {
        return fail(Response.ErrorType.VALIDATION,
                String.format("Type can not be cast to GraphQLNamedOutputType, due to '%s'", e)) ;
      }
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

    private GraphQLFieldDefinition.Builder generateSingleGraphQLQuery(GraphQLObjectType type) {

      return GraphQLFieldDefinition.newFieldDefinition().
              name(toParameterCase(type.getName())).
              description(createSingleQueryDescription(type)).
              arguments(createSingleQueryArguments(type)).
              type(GraphQLTypeReference.typeRef(type.getName()));
    }

    private String createSingleQueryDescription(GraphQLObjectType type) {
      return String.format(options.getSingleQueryDescriptionFormat(), type.getName()) ;
    }

    private GraphQLFieldDefinition.Builder generateSingleGraphQLMutation(GraphQLObjectType type) {
      return GraphQLFieldDefinition.newFieldDefinition().
              name(toParameterCase(type.getName())) ;
    }

    private List<GraphQLArgument> createSingleQueryArguments(GraphQLObjectType type) {
      return Collections.singletonList(GraphQLArgument.newArgument().
              name(String.format(options.getIdFormat(), StringUtils.toParameterCase(type.getName()))).
              type(options.isUsingIDType() ? GraphQLID : GraphQLString).
              build());
    }
  }

  @Builder(toBuilder = true)
  @Value
  public static class Options {
    boolean generatingQueryType = true;
    String queryTypeName = "Query";

    boolean generatingMutationType = false;
    String mutationTypeName = "Mutation";

    boolean usingIDType = true;
    String singleQueryDescriptionFormat = "Returns a %s object by id" ;

    String idFormat = "%sDbId" ;
  }

  @AllArgsConstructor
  private static class UnionTypeResolver implements TypeResolver {

    private GraphQLUnionType unionType;

    @Override
    public GraphQLObjectType getType(TypeResolutionEnvironment schemaName) {
      return (GraphQLObjectType) this.unionType.getTypes().get(0);
    }
  }
}
