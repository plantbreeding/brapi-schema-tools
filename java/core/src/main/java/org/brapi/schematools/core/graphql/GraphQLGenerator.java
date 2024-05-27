package org.brapi.schematools.core.graphql;

import graphql.TypeResolutionEnvironment;
import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.model.*;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.makeValidName;
import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

import java.nio.file.Path;
import java.util.*;

@AllArgsConstructor
public class GraphQLGenerator {

  private final BrAPISchemaReader schemaReader;

  public GraphQLGenerator() {
    this.schemaReader = new BrAPISchemaReader();
  }

  public Response<GraphQLSchema> generate(Path schemaDirectory, GraphQLGeneratorOptions options) {

    try {
      return new Generator(options, schemaReader.readDirectories(schemaDirectory)).generate();
    } catch (BrAPISchemaReaderException e) {
      return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
    }
  }

  @Getter
  public class Generator {
    private final GraphQLGeneratorOptions options ;
    private final List<BrAPIObjectType> brAPISchemas ;

    private final Map<String, GraphQLOutputType> objectTypes;
    private final Map<String, GraphQLUnionType> unionTypes;
    private final Map<String, GraphQLEnumType> enumTypes;

    private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

    public Generator(GraphQLGeneratorOptions options, List<BrAPIObjectType> brAPISchemas) {
      this.options = options;
      this.brAPISchemas = brAPISchemas;
      objectTypes = new HashMap<>() ;
      unionTypes = new HashMap<>() ;
      enumTypes = new HashMap<>() ;
    }

    public Response<GraphQLSchema> generate() {
      return brAPISchemas.stream().
              map(this::createObjectType).
              collect(Response.toList()).mapResultToResponse(this::createSchema);
    }

    private Response<GraphQLSchema> createSchema(List<GraphQLOutputType> types) {

      GraphQLSchema.Builder builder = GraphQLSchema.newSchema() ;

      if (options.isGeneratingQueryType()) {
        GraphQLObjectType.Builder query = newObject().name(options.getQueryType().getName());

        types.stream().map(type -> generateSingleGraphQLQuery((GraphQLObjectType)type)).forEach(query::field);

        builder.query(query);
      }

      if (options.isGeneratingMutationType()) {
        GraphQLObjectType.Builder mutation = newObject().name(options.getMutationType().getName());

        types.stream().map(type -> generateSingleGraphQLMutation((GraphQLObjectType)type)).forEach(mutation::field);

        builder.mutation(mutation);
      }

      Set<GraphQLType> additionalTypes = new HashSet<>(types);

      builder.additionalTypes(additionalTypes);

      unionTypes.values().forEach(graphQLType -> codeRegistry.typeResolver(graphQLType, new UnionTypeResolver(graphQLType)));

      builder.codeRegistry(codeRegistry.build());

      return success(builder.build());
    }

    private Response<GraphQLOutputType> createType(BrAPIType type) {

      if (type instanceof BrAPIObjectType) {
        return createObjectType((BrAPIObjectType) type);
      } else if (type instanceof BrAPIOneOfType) {
        return createUnionType((BrAPIOneOfType) type);
      } else if (type instanceof BrAPIArrayType) {
        return createListType((BrAPIArrayType) type);
      } else if (type instanceof BrAPIReferenceType) {
        return createReferenceType((BrAPIReferenceType) type);
      } else if (type instanceof BrAPIEnumType) {
        return createEnumType((BrAPIEnumType) type);
      } else if (type instanceof BrAPIPrimitiveType primitiveType) {

        return switch (primitiveType.getName()) {
          case "string" -> success(GraphQLString);
          case "integer" -> success(GraphQLInt);
          case "number" -> success(GraphQLFloat);
          case "boolean" -> success(GraphQLBoolean);
          default -> Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", primitiveType.getName()));
        };
      }

      return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type.getName()));
    }

    private Response<GraphQLOutputType> createReferenceType(BrAPIReferenceType type) {
      return success(GraphQLTypeReference.typeRef(type.getName())) ;
    }

    private Response<GraphQLOutputType> createListType(BrAPIArrayType type) {
      return createType(type.getItems()).mapResult(GraphQLList::list);
    }

    private Response<GraphQLOutputType> createObjectType(BrAPIObjectType type) {

      GraphQLOutputType existingType = objectTypes.get(type.getName());

      if (existingType != null) {
        return success(existingType) ;
      }

      GraphQLObjectType.Builder builder = newObject().
              name(type.getName()).
              description(type.getDescription());

      return type.getProperties().stream().map(this::createFieldDefinition).collect(Response.toList()).
              onSuccessDoWithResult(builder::fields).
              map(() -> addObjectType(builder.build()));
    }

    private Response<GraphQLFieldDefinition> createFieldDefinition(BrAPIObjectProperty property) {

      GraphQLFieldDefinition.Builder builder = newFieldDefinition().
              name(property.getName()).
              description(property.getDescription());


      return createType(property.getType()).
              onSuccessDoWithResult(builder::type).
              map(() -> success(builder.build()));
    }

    private Response<GraphQLOutputType> createUnionType(BrAPIOneOfType type) {

      GraphQLOutputType existingType = unionTypes.
              get(type.getName());

      if (existingType != null) {
        return success(existingType) ;
      }

      GraphQLUnionType.Builder builder = GraphQLUnionType.newUnionType().
              name(type.getName()).
              description(type.getDescription());

      return type.getPossibleTypes().stream().map(this::createNamedOutputType).collect(Response.toList()).
              onSuccessDoWithResult(builder::replacePossibleTypes).
              map(() -> addUnionType(builder.build()));
    }

    private Response<GraphQLNamedOutputType> createNamedOutputType(BrAPIType type) {
      try {
        return createType(type).mapResult(t -> (GraphQLNamedOutputType) t);
      } catch (ClassCastException e) {
        return fail(Response.ErrorType.VALIDATION,
                String.format("Type can not be cast to GraphQLNamedOutputType, due to '%s'", e)) ;
      }
    }

    private Response<GraphQLOutputType> createEnumType(BrAPIEnumType type) {

      GraphQLOutputType existingType = enumTypes.
              get(type.getName());

      if (existingType != null) {
        return success(existingType) ;
      }

      return addEnumType(GraphQLEnumType.
              newEnum().
              name(type.getName()).
              description(type.getDescription()).
              values(type.getValues().stream().map(this::createEnumValue).toList()).
              build()) ;
    }

    private GraphQLEnumValueDefinition createEnumValue(BrAPIEnumValue brAPIEnumValue) {
      return GraphQLEnumValueDefinition.
              newEnumValueDefinition().
              name(makeValidName(brAPIEnumValue.getName())).
              value(brAPIEnumValue.getValue()).
              build() ;
    }

    private Response<GraphQLOutputType> addObjectType(GraphQLObjectType type) {
      objectTypes.put(type.getName(), type) ;

      return success(type);
    }

    private Response<GraphQLOutputType> addUnionType(GraphQLUnionType type) {
      unionTypes.put(type.getName(), type) ;

      return success(type);
    }

    private Response<GraphQLOutputType> addEnumType(GraphQLEnumType type) {
      enumTypes.put(type.getName(), type) ;

      return success(type);
    }

    private GraphQLFieldDefinition.Builder generateSingleGraphQLQuery(GraphQLObjectType type) {

      return GraphQLFieldDefinition.newFieldDefinition().
              name(toParameterCase(type.getName())).
              description(createSingleQueryDescription(type)).
              arguments(createSingleQueryArguments(type)).
              type(GraphQLTypeReference.typeRef(type.getName()));
    }

    private String createSingleQueryDescription(GraphQLObjectType type) {
      return String.format(options.getQueryType().getSingleQuery().getDescriptionFormat(), type.getName()) ;
    }

    private GraphQLFieldDefinition.Builder generateSingleGraphQLMutation(GraphQLObjectType type) {
      return GraphQLFieldDefinition.newFieldDefinition().
              name(toParameterCase(type.getName())) ;
    }

    private List<GraphQLArgument> createSingleQueryArguments(GraphQLObjectType type) {
      return Collections.singletonList(GraphQLArgument.newArgument().
              name(String.format(options.getIds().getNameFormat(), StringUtils.toParameterCase(type.getName()))).
              type(options.getIds().isUsingIDType() ? GraphQLID : GraphQLString).
              build());
    }
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
