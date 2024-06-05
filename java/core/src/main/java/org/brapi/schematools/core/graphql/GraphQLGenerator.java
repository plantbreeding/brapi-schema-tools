package org.brapi.schematools.core.graphql;

import graphql.AssertException;
import graphql.TypeResolutionEnvironment;
import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static graphql.Scalars.GraphQLBoolean;
import static graphql.Scalars.GraphQLFloat;
import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;
import static graphql.schema.GraphQLInputObjectType.newInputObject;
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
    private final GraphQLGeneratorOptions options;

    public GraphQLGenerator() {
        this(new BrAPISchemaReader(), GraphQLGeneratorOptions.load()) ;
    }

    public GraphQLGenerator(GraphQLGeneratorOptions options) {
        this(new BrAPISchemaReader(), options) ;
    }

    public Response<GraphQLSchema> generate(Path schemaDirectory) {
        return generate(schemaDirectory, new GraphQLGeneratorMetadata()) ;
    }

    public Response<GraphQLSchema> generate(Path schemaDirectory, GraphQLGeneratorMetadata metadata) {

        try {
            return new Generator(options, metadata, schemaReader.readDirectories(schemaDirectory)).generate();
        } catch (BrAPISchemaReaderException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    @Getter
    public static class Generator {
        private final GraphQLGeneratorOptions options;
        private final GraphQLGeneratorMetadata metadata;
        private final Map<String, BrAPIObjectType> brAPISchemas;

        private final Map<String, GraphQLObjectType> objectOutputTypes;
        private final Map<String, GraphQLUnionType> unionTypes;
        private final Map<String, GraphQLEnumType> enumTypes;
        private final Map<String, GraphQLInputObjectType> inputObjectTypes;

        private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();


        public Generator(GraphQLGeneratorOptions options, GraphQLGeneratorMetadata metadata, List<BrAPIObjectType> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            this.brAPISchemas = brAPISchemas.stream().collect(Collectors.toMap(BrAPIObjectType::getName, Function.identity()));
            objectOutputTypes = new HashMap<>();
            unionTypes = new HashMap<>();
            enumTypes = new HashMap<>();
            inputObjectTypes = new HashMap<>();
        }

        public Response<GraphQLSchema> generate() {
            return brAPISchemas.values().stream().
                filter(BrAPIObjectType::isRequest).
                map(this::createInputObjectType).collect(Response.toList()).
                map(() -> brAPISchemas.values().stream().
                    filter(type -> !type.isRequest()).
                    map(this::createObjectType).
                    collect(Response.toList())).
                mapResultToResponse(this::createSchema);
        }

        private Response<GraphQLSchema> createSchema(List<GraphQLObjectType> types) {

            GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

            if (options.isGeneratingQueryType()) {
                GraphQLObjectType.Builder query = newObject().name(options.getQueryType().getName());

                if (options.isGeneratingSingleQueries()) {
                    types.stream().filter(type -> options.getQueryType().getSingleQuery().getGeneratingFor().
                        getOrDefault(type.getName(), options.getQueryType().getSingleQuery().isGenerating())).map(this::generateSingleGraphQLQuery).forEach(query::field);
                }

                if (options.isGeneratingListQueries()) {
                    types.stream().filter(type -> options.getQueryType().getListQuery().getGeneratingFor().
                        getOrDefault(type.getName(), options.getQueryType().getListQuery().isGenerating())).map(this::generateListGraphQLQuery).forEach(query::field);
                }

                if (options.isGeneratingSearchQueries()) {
                    types.stream().filter(type -> options.getQueryType().getSearchQuery().getGeneratingFor().
                        getOrDefault(type.getName(), options.getQueryType().getSearchQuery().isGenerating())).map(this::generateSearchGraphQLQuery).forEach(query::field);
                }

                builder.query(query);
            }

            if (options.isGeneratingMutationType()) {
                GraphQLObjectType.Builder mutation = newObject().name(options.getMutationType().getName());

                types.stream().filter(type -> options.getMutationType().getGeneratingFor().
                    getOrDefault(type.getName(), options.getMutationType().isGenerating())).map(this::generateSingleGraphQLMutation).forEach(mutation::field);

                builder.mutation(mutation);
            }

            Set<GraphQLType> additionalTypes = new HashSet<>(types);

            if (options.isGeneratingListQueries() &&
                (options.getQueryType().getListQuery().isPagedDefault() || options.getQueryType().getListQuery().getPaged().values().stream().anyMatch(paged -> paged))) {
                additionalTypes.add(createPageInputType());
                additionalTypes.add(createPageType());
            }

            builder.additionalTypes(additionalTypes);

            unionTypes.values().forEach(graphQLType -> codeRegistry.typeResolver(graphQLType, new UnionTypeResolver(graphQLType)));

            builder.codeRegistry(codeRegistry.build());

            try {
                return success(builder.build());
            } catch (AssertException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<GraphQLOutputType> createOutputType(BrAPIType type) {

            if (type instanceof BrAPIObjectType) {
                return createObjectType((BrAPIObjectType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIOneOfType) {
                return createUnionOutputType((BrAPIOneOfType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIArrayType) {
                return createListType((BrAPIArrayType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType) {
                return createReferenceType((BrAPIReferenceType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType) {
                return createEnumType((BrAPIEnumType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType) {
                return createScalarType((BrAPIPrimitiveType) type).mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown output type '%s'", type.getName()));
        }

        private Response<GraphQLTypeReference> createReferenceType(BrAPIReferenceType type) {
            return success(GraphQLTypeReference.typeRef(type.getName()));
        }

        private Response<GraphQLList> createListType(BrAPIArrayType type) {
            return createOutputType(type.getItems()).mapResult(GraphQLList::list);
        }

        private Response<GraphQLObjectType> createObjectType(BrAPIObjectType type) {

            GraphQLObjectType existingType = objectOutputTypes.get(type.getName());

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

            return createOutputType(property.getType()).
                onSuccessDoWithResult(builder::type).
                map(() -> success(builder.build()));
        }

        private Response<GraphQLInputObjectType> createInputObjectType(BrAPIObjectType type) {

            String queryName = toPlural(toParameterCase(type.getName().substring(0, type.getName().length() - 6)));

            String name = String.format(options.getQueryType().getListQuery().getInputTypeNameFormat(), StringUtils.toSentenceCase(queryName));

            GraphQLInputObjectType existingType = inputObjectTypes.get(name);

            if (existingType != null) {
                return success(existingType);
            }

            GraphQLInputObjectType.Builder builder = newInputObject().
                name(name).
                description(type.getDescription());

            return type.getProperties().stream().map(this::createInputObjectField).collect(Response.toList()).
                onSuccessDoWithResult(builder::fields).
                map(() -> addInputObjectType(builder.build()));
        }

        private Response<GraphQLInputType> createInputType(BrAPIType type) {

            if (type instanceof BrAPIObjectType) {
                return createInputObjectType((BrAPIObjectType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIArrayType) {
                return createListType((BrAPIArrayType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType) {
                return createReferenceType((BrAPIReferenceType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType) {
                return createEnumType((BrAPIEnumType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType) {
                return createScalarType((BrAPIPrimitiveType) type).mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown input type '%s'", type.getName()));
        }

        private Response<GraphQLInputObjectField> createInputObjectField(BrAPIObjectProperty property) {
            GraphQLInputObjectField.Builder builder = newInputObjectField().
                name(property.getName()).
                description(property.getDescription());

            return createInputType(property.getType()).
                onSuccessDoWithResult(builder::type).
                map(() -> success(builder.build()));
        }

        private Response<GraphQLUnionType> createUnionOutputType(BrAPIOneOfType type) {

            GraphQLUnionType existingType = unionTypes.
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
                return createOutputType(type).mapResult(t -> (GraphQLNamedOutputType) t);
            } catch (ClassCastException e) {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("Type can not be cast to GraphQLNamedOutputType, due to '%s'", e));
            }
        }

        private Response<GraphQLEnumType> createEnumType(BrAPIEnumType type) {

            GraphQLEnumType existingType = enumTypes.
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

        private Response<GraphQLScalarType> createScalarType(BrAPIPrimitiveType type) {
            return switch (type.getName()) {
                case "string" -> success(GraphQLString);
                case "integer" -> success(GraphQLInt);
                case "number" -> success(GraphQLFloat);
                case "boolean" -> success(GraphQLBoolean);
                default ->
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", type.getName()));
            };
        }

        private Response<GraphQLObjectType> addObjectType(GraphQLObjectType type) {
            objectOutputTypes.put(type.getName(), type);

            return success(type);
        }

        private Response<GraphQLUnionType> addUnionType(GraphQLUnionType type) {
            unionTypes.put(type.getName(), type);

            return success(type);
        }

        private Response<GraphQLEnumType> addEnumType(GraphQLEnumType type) {
            enumTypes.put(type.getName(), type);

            return success(type);
        }

        private Response<GraphQLInputObjectType> addInputObjectType(GraphQLInputObjectType type) {
            inputObjectTypes.put(type.getName(), type);

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

            List<GraphQLArgument> arguments = new ArrayList<>();

            arguments.add(GraphQLArgument.newArgument().
                name(String.format(options.getIds().getNameFormat(), StringUtils.toParameterCase(type.getName()))).
                type(options.isUsingIDType() ? GraphQLID : GraphQLString).
                build());

            if (options.getQueryType().isPartitionedByCrop()) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
                    build());
            }

            return arguments;
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

            boolean hasInput = options.getQueryType().getListQuery().getInput().getOrDefault(type.getName(), true);

            if (hasInput) {
                arguments.add(GraphQLArgument.newArgument().
                    name(options.getQueryType().getListQuery().getInputNameFormat() != null ?
                        String.format(options.getQueryType().getListQuery().getInputNameFormat(), StringUtils.toParameterCase(type.getName())) :
                        options.getQueryType().getListQuery().getInputName()).
                    type(GraphQLTypeReference.typeRef(String.format(options.getQueryType().getListQuery().getInputTypeNameFormat(), StringUtils.toSentenceCase(queryName)))).
                    build());
            }

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
            return GraphQLInputObjectType.newInputObject().
                name(options.getQueryType().getListQuery().getPageInputTypeName()).
                field(GraphQLInputObjectField.newInputObjectField().name("page").type(GraphQLInt)).
                field(GraphQLInputObjectField.newInputObjectField().name("pageSize").type(GraphQLInt)).build();
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

            boolean paged = options.getQueryType().getListQuery().getPaged().getOrDefault(type.getName(), options.getQueryType().getListQuery().isPagedDefault());

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
