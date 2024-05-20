package org.brapi.schematools.core.graphql;

import graphql.TypeResolutionEnvironment;
import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.nio.file.Path;
import java.util.*;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.makeValidName;
import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSentenceCase;

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
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    @Getter
    public static class Generator {
        private final GraphQLGeneratorOptions options;
        private final List<BrAPIObjectType> brAPISchemas;

        private final Map<String, GraphQLOutputType> objectTypes;
        private final Map<String, GraphQLUnionType> unionTypes;
        private final Map<String, GraphQLEnumType> enumTypes;

        private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

        public Generator(GraphQLGeneratorOptions options, List<BrAPIObjectType> brAPISchemas) {
            this.options = options;
            this.brAPISchemas = brAPISchemas;
            objectTypes = new HashMap<>();
            unionTypes = new HashMap<>();
            enumTypes = new HashMap<>();
        }

        public Response<GraphQLSchema> generate() {
            return brAPISchemas.stream().
                map(this::createObjectType).
                collect(Response.toList()).mapResultToResponse(this::createSchema);
        }

        private Response<GraphQLSchema> createSchema(List<GraphQLOutputType> types) {

            GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

            if (options.isGeneratingQueryType()) {
                GraphQLObjectType.Builder query = newObject().name(options.getQueryType().getName());

                if (options.isGeneratingSingleQueries()) {
                    types.stream().map(type -> generateSingleGraphQLQuery((GraphQLObjectType) type)).forEach(query::field);
                }

                if (options.isGeneratingListQueries()) {
                    types.stream().map(type -> generateListGraphQLQuery((GraphQLObjectType) type)).forEach(query::field);
                }

                if (options.isGeneratingSearchQueries()) {
                    types.stream().map(type -> generateSearchGraphQLQuery((GraphQLObjectType) type)).forEach(query::field);
                }

                builder.query(query);
            }

            if (options.isGeneratingMutationType()) {
                GraphQLObjectType.Builder mutation = newObject().name(options.getMutationType().getName());

                types.stream().map(type -> generateSingleGraphQLMutation((GraphQLObjectType) type)).forEach(mutation::field);

                builder.mutation(mutation);
            }

            Set<GraphQLType> additionalTypes = new HashSet<>(types);

            builder.additionalTypes(additionalTypes);

            if (options.isGeneratingListQueries() &&
                (options.getQueryType().getListQuery().isPagedDefault() || options.getQueryType().getListQuery().getPaged().values().stream().anyMatch(paged -> paged))) {
                additionalTypes.add(createPageInputType());
                additionalTypes.add(createPageType());
            }

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
                    default ->
                        Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", primitiveType.getName()));
                };
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type.getName()));
        }

        private Response<GraphQLOutputType> createReferenceType(BrAPIReferenceType type) {
            return success(GraphQLTypeReference.typeRef(type.getName()));
        }

        private Response<GraphQLOutputType> createListType(BrAPIArrayType type) {
            return createType(type.getItems()).mapResult(GraphQLList::list);
        }

        private Response<GraphQLOutputType> createObjectType(BrAPIObjectType type) {

            GraphQLOutputType existingType = objectTypes.get(type.getName());

            if (existingType != null) {
                return success(existingType);
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
                return success(existingType);
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
                    String.format("Type can not be cast to GraphQLNamedOutputType, due to '%s'", e));
            }
        }

        private Response<GraphQLOutputType> createEnumType(BrAPIEnumType type) {

            GraphQLOutputType existingType = enumTypes.
                get(type.getName());

            if (existingType != null) {
                return success(existingType);
            }

            return addEnumType(GraphQLEnumType.
                newEnum().
                name(type.getName()).
                description(type.getDescription()).
                values(type.getValues().stream().map(this::createEnumValue).toList()).
                build());
        }

        private GraphQLEnumValueDefinition createEnumValue(BrAPIEnumValue brAPIEnumValue) {
            return GraphQLEnumValueDefinition.
                newEnumValueDefinition().
                name(makeValidName(brAPIEnumValue.getName())).
                value(brAPIEnumValue.getValue()).
                build();
        }

        private Response<GraphQLOutputType> addObjectType(GraphQLObjectType type) {
            objectTypes.put(type.getName(), type);

            return success(type);
        }

        private Response<GraphQLOutputType> addUnionType(GraphQLUnionType type) {
            unionTypes.put(type.getName(), type);

            return success(type);
        }

        private Response<GraphQLOutputType> addEnumType(GraphQLEnumType type) {
            enumTypes.put(type.getName(), type);

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
            return String.format(options.getQueryType().getSingleQuery().getDescriptionFormat(), type.getName());
        }

        private List<GraphQLArgument> createSingleQueryArguments(GraphQLObjectType type) {
            return Collections.singletonList(GraphQLArgument.newArgument().
                name(String.format(options.getIds().getNameFormat(), StringUtils.toParameterCase(type.getName()))).
                type(options.getIds().isUsingIDType() ? GraphQLID : GraphQLString).
                build());
        }

        private GraphQLFieldDefinition.Builder generateListGraphQLQuery(GraphQLObjectType type) {

            String queryName = toPlural(toParameterCase(type.getName()));

            boolean paged = options.getQueryType().getListQuery().getPaged().containsKey(type.getName()) ?
                options.getQueryType().getListQuery().getPaged().get(type.getName()) : options.getQueryType().getListQuery().isPagedDefault();

            return GraphQLFieldDefinition.newFieldDefinition().
                name(queryName).
                description(createListQueryDescription(type)).
                arguments(createListQueryArguments(queryName, paged, type)).
                type(createListResponse(queryName, paged, type));
        }

        private String createListQueryDescription(GraphQLObjectType type) {
            return String.format(options.getQueryType().getListQuery().getDescriptionFormat(), type.getName());
        }

        private List<GraphQLArgument> createListQueryArguments(String queryName, boolean paged, GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            arguments.add(GraphQLArgument.newArgument().
                name(options.getQueryType().getListQuery().getInputNameFormat() != null ?
                    String.format(options.getQueryType().getListQuery().getInputNameFormat(), StringUtils.toParameterCase(type.getName())) :
                    options.getQueryType().getListQuery().getInputName()).
                type(GraphQLTypeReference.typeRef(String.format(options.getQueryType().getListQuery().getInputTypeNameFormat(), StringUtils.toSentenceCase(queryName)))).
                build());

            if (paged) {
                arguments.add(GraphQLArgument.newArgument().
                    name(options.getQueryType().getListQuery().getPagingInputName()).
                    type(GraphQLTypeReference.typeRef(options.getQueryType().getListQuery().getPageInputTypeName())).
                    build());
            }

            return arguments;
        }

        private GraphQLOutputType createListResponse(String queryName, boolean paged, GraphQLObjectType type) {
            GraphQLObjectType.Builder builder = newObject().
                name(String.format(options.getQueryType().getListQuery().getResponseTypeNameFormat(), toSentenceCase(queryName))).
                field(createListDataField(type));

            if (paged) {
                builder.field(GraphQLFieldDefinition.newFieldDefinition().
                    name(options.getQueryType().getListQuery().getPageFieldName()).
                    type(GraphQLList.list(GraphQLTypeReference.typeRef(type.getName()))).
                    build());
            }

            return builder.build();
        }

        private GraphQLFieldDefinition createListDataField(GraphQLObjectType type) {
            return GraphQLFieldDefinition.newFieldDefinition().
                name(options.getQueryType().getListQuery().getDataFieldName()).
                type(GraphQLList.list(GraphQLTypeReference.typeRef(type.getName()))).
                build();
        }

        private GraphQLType createPageInputType() {
            return GraphQLObjectType.newObject().
                name(options.getQueryType().getListQuery().getPageInputTypeName()).
                field(GraphQLFieldDefinition.newFieldDefinition().name("page").type(GraphQLInt)).
                field(GraphQLFieldDefinition.newFieldDefinition().name("pageSize").type(GraphQLInt)).build();
        }

        private GraphQLType createPageType() {
            return GraphQLObjectType.newObject().
                name(options.getQueryType().getListQuery().getPageTypeName()).
                field(GraphQLFieldDefinition.newFieldDefinition().name("currentPage").type(GraphQLInt)).
                field(GraphQLFieldDefinition.newFieldDefinition().name("pageSize").type(GraphQLInt)).
                field(GraphQLFieldDefinition.newFieldDefinition().name("totalCount").type(GraphQLInt)).
                field(GraphQLFieldDefinition.newFieldDefinition().name("totalPages").type(GraphQLInt)).build();
        }

        private GraphQLFieldDefinition.Builder generateSearchGraphQLQuery(GraphQLObjectType type) {

            String queryName = toPlural(toSentenceCase(type.getName()));

            boolean paged = options.getQueryType().getListQuery().getPaged().containsKey(type.getName()) ?
                options.getQueryType().getListQuery().getPaged().get(type.getName()) : options.getQueryType().getListQuery().isPagedDefault();

            return GraphQLFieldDefinition.newFieldDefinition().
                name(toParameterCase(queryName)).
                description(createListQueryDescription(type)).
                arguments(createListQueryArguments(queryName, paged, type)).
                type(createListResponse(queryName, paged, type));
        }

        private GraphQLFieldDefinition.Builder generateSingleGraphQLMutation(GraphQLObjectType type) {
            return GraphQLFieldDefinition.newFieldDefinition().
                name(toParameterCase(type.getName()));
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
