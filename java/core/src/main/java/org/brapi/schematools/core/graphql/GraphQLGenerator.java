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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

/**
 * Generates a GraphQL schema from a BrAPI Json Schema.
 */
@AllArgsConstructor
public class GraphQLGenerator {

    private final BrAPISchemaReader schemaReader;
    private final GraphQLGeneratorOptions options;

    /**
     * Creates a GraphQLGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link GraphQLGeneratorOptions}.
     */
    public GraphQLGenerator() {
        this(new BrAPISchemaReader(), GraphQLGeneratorOptions.load()) ;
    }

    /**
     * Creates a GraphQLGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link GraphQLGeneratorOptions}.
     * @param options The options to be used in the generation.
     */
    public GraphQLGenerator(GraphQLGeneratorOptions options) {
        this(new BrAPISchemaReader(), options) ;
    }

    /**
     * Generates the {@link GraphQLSchema} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the {@link GraphQLSchema} from the complete BrAPI Specification
     */
    public Response<GraphQLSchema> generate(Path schemaDirectory) {
        return generate(schemaDirectory, new GraphQLGeneratorMetadata()) ;
    }

    /**
     * Generates the {@link GraphQLSchema} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param metadata additional metadata that is used in the generation
     * @return the {@link GraphQLSchema} from the complete BrAPI Specification
     */
    public Response<GraphQLSchema> generate(Path schemaDirectory, GraphQLGeneratorMetadata metadata) {

        try {
            return schemaReader.readDirectories(schemaDirectory).mapResultToResponse(brAPISchemas -> new Generator(options, metadata, brAPISchemas).generate()) ;
        } catch (BrAPISchemaReaderException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    @Getter
    private static class Generator {
        private final GraphQLGeneratorOptions options;
        private final GraphQLGeneratorMetadata metadata;
        private final Map<String, BrAPIClass> brAPISchemas;
        private final Map<String, GraphQLObjectType> objectOutputTypes;
        private final Map<String, GraphQLUnionType> unionTypes;
        private final Map<String, GraphQLEnumType> enumTypes;
        private final Map<String, GraphQLNamedInputType> inputTypes;
        private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

        public Generator(GraphQLGeneratorOptions options, GraphQLGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            this.brAPISchemas = brAPISchemas.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));
            objectOutputTypes = new HashMap<>();
            unionTypes = new HashMap<>();
            enumTypes = new HashMap<>();
            inputTypes = new HashMap<>();
        }

        public Response<GraphQLSchema> generate() {
            return brAPISchemas.values().stream().
                    filter(this::isNonPrimaryModel).
                    map(this::createOutputType).
                    collect(Response.toList()).
                mapOnCondition(options.isGeneratingCreateMutation() || options.isGeneratingUpdateMutation(),
                    () -> brAPISchemas.values().stream().
                        filter(this::isGeneratingInputTypeForMutation).
                        map(this::createInputObjectTypeForModel).
                        collect(Response.toList())).
                mapOnCondition(options.isGeneratingListQueries(),
                    () -> brAPISchemas.values().stream().
                        filter(this::isGeneratingInputTypeForListQuery).
                        map(this::createInputObjectTypeForListQuery).
                        collect(Response.toList())).
                mapOnCondition(options.isGeneratingSearchQueries(),
                    () -> brAPISchemas.values().stream().
                        filter(this::isGeneratingInputTypeForSearchQuery).
                        map(this::createInputObjectTypeForSearchQuery).
                        collect(Response.toList())).
                map(() -> brAPISchemas.values().stream().
                    filter(this::isPrimaryModel).
                    map(type -> createObjectType((BrAPIObjectType) type)).
                    collect(Response.toList())).
                mapResultToResponse(this::createSchema);
        }

        private boolean isGeneratingInputTypeForListQuery(BrAPIClass brAPIClass) {
            return isPrimaryModel(brAPIClass) &&
                (options.isGeneratingListQueryFor(brAPIClass.getName()) &&
                    !inputTypes.containsKey(options.getQueryInputTypeNameFor(brAPIClass)) &&
                    options.getQueryType().getListQuery().hasInputFor(brAPIClass)) ;
        }

        private boolean isGeneratingInputTypeForSearchQuery(BrAPIClass brAPIClass) {
            return isPrimaryModel(brAPIClass) &&
                (options.isGeneratingSearchQueryFor(brAPIClass.getName()) &&
                    !inputTypes.containsKey(options.getQueryInputTypeNameFor(brAPIClass)) &&
                    options.getQueryType().getSearchQuery().hasInputFor(brAPIClass)) ;
        }

        private boolean isGeneratingInputTypeForMutation(BrAPIClass brAPIClass) {
            return isPrimaryModel(brAPIClass) && (options.isGeneratingCreateMutationFor(brAPIClass.getName()) || options.isGeneratingDeleteMutationFor(brAPIClass.getName())) ;
        }

        private boolean isNotInputType(BrAPIClass type) {
            return !(type instanceof BrAPIObjectType) || type.getMetadata() == null || !type.getMetadata().isRequest();
        }

        private boolean isPrimaryModel(BrAPIClass type) {
            return isNotInputType(type) && type.getMetadata() != null && type.getMetadata().isPrimaryModel() ;
        }

        private boolean isNonPrimaryModel(BrAPIClass type) {
            return isNotInputType(type) && (type.getMetadata() == null || !type.getMetadata().isPrimaryModel()) ;
        }

        private Response<GraphQLSchema> createSchema(List<GraphQLObjectType> primaryTypes) {
            GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

            if (options.isGeneratingQueryType()) {
                GraphQLObjectType.Builder query = newObject().name(options.getQueryType().getName());

                if (options.isGeneratingSingleQueries()) {
                    primaryTypes.stream().filter(type -> options.getQueryType().getSingleQuery().isGeneratingFor(type.getName())).map(this::generateSingleGraphQLQuery).forEach(query::field);
                }

                if (options.isGeneratingListQueries()) {
                    primaryTypes.stream().filter(type -> options.getQueryType().getListQuery().isGeneratingFor(type.getName())).map(this::generateListGraphQLQuery).forEach(query::field);
                }

                if (options.isGeneratingSearchQueries()) {
                    primaryTypes.stream().filter(type -> options.getQueryType().getSearchQuery().isGeneratingFor(type.getName())).map(this::generateSearchGraphQLQuery).forEach(query::field);
                }

                builder.query(query);
            }

            if (options.isGeneratingMutationType()) {
                GraphQLObjectType.Builder mutation = newObject().name(options.getMutationType().getName());

                primaryTypes.stream().filter(type -> options.isGeneratingCreateMutationFor(type.getName()))
                    .map(this::generateCreateGraphQLMutation).forEach(mutation::field);

                primaryTypes.stream().filter(type -> options.isGeneratingUpdateMutationFor(type.getName()))
                    .map(this::generateUpdateGraphQLMutation).forEach(mutation::field);

                primaryTypes.stream().filter(type -> options.isGeneratingDeleteMutationFor(type.getName()))
                    .map(this::generateDeleteGraphQLMutation).forEach(mutation::field);

                builder.mutation(mutation);
            }

            objectOutputTypes.values().forEach(builder::additionalType);
            enumTypes.values().forEach(builder::additionalType);
            inputTypes.values().forEach(builder::additionalType);
            unionTypes.values().forEach(builder::additionalType);

            if (options.isGeneratingListQueries() && options.getQueryType().getListQuery().hasPaging() ) {
                builder.additionalType(createPageInputType());
                builder.additionalType(createPageType());
            }

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
                return createOutputListType((BrAPIArrayType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType) {
                return createOutputReferenceType((BrAPIReferenceType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType) {
                return createEnumType((BrAPIEnumType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType) {
                return createScalarType((BrAPIPrimitiveType) type).mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown output type '%s'", type.getName()));
        }

        private Response<GraphQLTypeReference> createOutputReferenceType(BrAPIReferenceType type) {
            return success(GraphQLTypeReference.typeRef(type.getName()));
        }

        private Response<GraphQLList> createOutputListType(BrAPIArrayType type) {
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

        private Response<GraphQLTypeReference> createInputReferenceType(BrAPIReferenceType type) {
            BrAPIClass referencedSchema = this.brAPISchemas.get(type.getName());

            if (referencedSchema != null && !(referencedSchema instanceof BrAPIEnumType)) {
                String inputTypeName = options.getInput().getTypeNameFor(referencedSchema) ;

                if (isNotInputType(referencedSchema)) {
                    return createInputObjectTypeForModel(referencedSchema).
                        withResult(GraphQLTypeReference.typeRef(inputTypeName)) ;
                }

                return success(GraphQLTypeReference.typeRef(inputTypeName)) ;
            }

            return success(GraphQLTypeReference.typeRef(type.getName()));
        }

        private Response<GraphQLList> createInputListType(BrAPIArrayType type) {
            return createInputType(type.getItems()).mapResult(GraphQLList::list);
        }

        private Response<GraphQLNamedInputType> createInputObjectTypeForModel(BrAPIClass type) {
            return createInputObjectType(options.getInput().getTypeNameFor(type), type) ;
        }

        private Response<GraphQLNamedInputType> createInputObjectTypeForListQuery(BrAPIClass type) {
            return createInputTypeFromClass(options.getQueryInputTypeNameFor(type), type) ;
        }

        private Response<GraphQLNamedInputType> createInputObjectTypeForSearchQuery(BrAPIClass type) {
            return createInputTypeFromClass(options.getQueryInputTypeNameFor(type), type) ;
        }

        private Response<GraphQLNamedInputType> createInputTypeFromClass(String name, BrAPIClass type) {
            if (type instanceof BrAPIObjectType brAPIObjectType) {
                if (type.getName().endsWith("Request")) {
                    return createInputObjectType(options.getInput().getTypeNameForQuery(
                        options.getQueryType().getListQuery().getNameFor(options.getPluralFor(type.getName().substring(0, type.getName().length() - 7)))), type) ;
                } else {
                    return createInputObjectType(name, brAPIObjectType).mapResult(t -> t);
                }

            } else if (type instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType).mapResult(t -> t);
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Input object '%s' must be BrAPIObjectType or BrAPIEnumType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
            }
        }

        private Response<GraphQLNamedInputType> createInputObjectType(String name, BrAPIClass type) {
            GraphQLNamedInputType existingType = inputTypes.get(name);

            if (existingType != null) {
                return success(existingType);
            } else {
                addInputObjectType(GraphQLTypeReference.typeRef(name));
            }

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                GraphQLInputObjectType.Builder builder = newInputObject().
                    name(name).
                    description(brAPIObjectType.getDescription());

                return brAPIObjectType.getProperties().stream().map(this::createInputObjectField).collect(Response.toList()).
                    onSuccessDoWithResult(builder::fields).
                    map(() -> addInputObjectType(builder.build()));
            } else if (type instanceof BrAPIOneOfType brAPIOneOfType) {
                GraphQLInputObjectType.Builder builder = newInputObject().
                    name(name).
                    description(brAPIOneOfType.getDescription());

                return brAPIOneOfType.getPossibleTypes().stream().flatMap(this::extractProperties).map(this::createInputObjectField).collect(Response.toList()).
                    onSuccessDoWithResult(builder::fields).
                    map(() -> addInputObjectType(builder.build()));
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Input object '%s' must be BrAPIObjectType or BrAPIOneOfType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
            }
        }

        private Stream<BrAPIObjectProperty> extractProperties(BrAPIType brAPIType) {
            if (brAPIType instanceof BrAPIObjectType brAPIObjectType) {
                return brAPIObjectType.getProperties().stream() ;
            } else {
                return Stream.empty() ;
            }
        }

        private Response<GraphQLInputType> createInputType(BrAPIType type) {

            if (type instanceof BrAPIObjectType) {
                return createInputObjectTypeForModel((BrAPIObjectType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIArrayType) {
                return createInputListType((BrAPIArrayType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType) {
                return createInputReferenceType((BrAPIReferenceType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType) {
                return createEnumType((BrAPIEnumType) type).mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType) {
                return createScalarType((BrAPIPrimitiveType) type).mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown input type '%s' for '%s'", type.getClass().getSimpleName(), type.getName()));
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

        private Response<GraphQLNamedInputType> addInputObjectType(GraphQLNamedInputType type) {
            inputTypes.put(type.getName(), type);

            return success(type);
        }

        private GraphQLFieldDefinition.Builder generateSingleGraphQLQuery(GraphQLObjectType type) {

            return GraphQLFieldDefinition.newFieldDefinition().
                name(options.getSingleQueryNameFor(type.getName())).
                description(createSingleQueryDescription(type)).
                arguments(createSingleQueryArguments(type)).
                type(GraphQLTypeReference.typeRef(type.getName()));
        }

        private String createSingleQueryDescription(GraphQLObjectType type) {
            return options.getQueryType().getSingleQuery().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createSingleQueryArguments(GraphQLObjectType type) {

            List<GraphQLArgument> arguments = new ArrayList<>();

            arguments.add(GraphQLArgument.newArgument().
                name(options.getIds().getNameFor(type.getName())).
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
            String queryName = options.getListQueryNameFor(type.getName()) ;

            boolean paged = options.getQueryType().getListQuery().isPagedFor(type.getName());
            boolean hasInput = options.getQueryType().getListQuery().hasInputFor(type.getName()) ;

            return GraphQLFieldDefinition.newFieldDefinition().
                name(queryName).
                description(createListQueryDescription(type)).
                arguments(createListQueryArguments(paged, hasInput, type)).
                type(createListResponse(queryName, paged, type));
        }

        private String createListQueryDescription(GraphQLObjectType type) {
            return options.getQueryType().getListQuery().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createListQueryArguments(boolean paged,boolean hasInput, GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getQueryInputTypeNameFor(type.getName()) ;

            if (hasInput) {
                arguments.add(GraphQLArgument.newArgument().
                    name(options.getInput().getNameFor(type.getName())).
                    type(GraphQLTypeReference.typeRef(inputTypeName)).
                    build());
            }

            if (options.getQueryType().isPartitionedByCrop() && !hasField(inputTypeName, "commonCropName")) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
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

        private boolean hasField(String typeName, String fieldName) {
            GraphQLNamedInputType type = this.inputTypes.get(typeName);

            if (type instanceof GraphQLInputObjectType graphQLInputObjectType) {
                return graphQLInputObjectType.getField(fieldName) == null ;
            }

            return false ;
        }

        private GraphQLOutputType createListResponse(String queryName, boolean paged, GraphQLObjectType type) {
            GraphQLObjectType.Builder builder = newObject().
                name(String.format(options.getQueryType().getListQuery().getResponseTypeNameForQuery(queryName))).
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
            String queryName = options.getSearchQueryNameFor(type.getName()) ;

            return GraphQLFieldDefinition.newFieldDefinition()
                .name(queryName)
                .description(createSearchQueryDescription(type))
                .arguments(createSearchQueryArguments(type))
                .type(creatSearchResponse(queryName, type));
        }

        private String createSearchQueryDescription(GraphQLObjectType type) {
            return options.getQueryType().getSearchQuery().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createSearchQueryArguments(GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getQueryInputTypeNameFor(type.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                name(options.getInput().getNameFor(type.getName())).
                type(GraphQLTypeReference.typeRef(inputTypeName)).
                build());

            if (options.getQueryType().isPartitionedByCrop() && !hasField(inputTypeName, "commonCropName")) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
                    build());
            }

            return arguments;
        }

        private GraphQLOutputType creatSearchResponse(String queryName, GraphQLObjectType type) {
            GraphQLObjectType.Builder builder = newObject().
                name(String.format(options.getQueryType().getSearchQuery().getResponseTypeNameForQuery(queryName))).
                field(GraphQLFieldDefinition.newFieldDefinition().
                    name(options.getQueryType().getSearchQuery().getSearchIdFieldName()).
                    type(GraphQLList.list(GraphQLTypeReference.typeRef(type.getName()))).
                    build()).
                field(createListDataField(type));

            return builder.build();
        }

        private GraphQLFieldDefinition.Builder generateCreateGraphQLMutation(GraphQLObjectType type) {
            return GraphQLFieldDefinition.newFieldDefinition()
                .name(options.getCreateMutationNameFor(type.getName()))
                .description(createCreateMutationDescription(type))
                .arguments(createCreateMutationArguments(type))
                .type(options.getMutationType().getCreateMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(type.getName())) : GraphQLTypeReference.typeRef(type.getName())) ;
        }

        private String createCreateMutationDescription(GraphQLObjectType type) {
            return options.getMutationType().getCreateMutation().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createCreateMutationArguments(GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getInput().getTypeNameFor(type.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                    name(options.getInput().getNameFor(type.getName())).
                    type(options.getMutationType().getCreateMutation().isMultiple() ?
                        GraphQLList.list(GraphQLTypeReference.typeRef(inputTypeName)) : GraphQLTypeReference.typeRef(inputTypeName)).
                    build());

            if (options.getQueryType().isPartitionedByCrop() && !hasField(inputTypeName, "commonCropName")) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
                    build());
            }

            return arguments;
        }

        private GraphQLFieldDefinition.Builder generateUpdateGraphQLMutation(GraphQLObjectType type) {
            return GraphQLFieldDefinition.newFieldDefinition()
                .name(options.getUpdateMutationNameFor(type.getName()))
                .description(createUpdateMutationDescription(type))
                .arguments(createUpdateMutationArguments(type))
                .type(options.getMutationType().getUpdateMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(type.getName())) : GraphQLTypeReference.typeRef(type.getName())) ;
        }

        private String createUpdateMutationDescription(GraphQLObjectType type) {
            return options.getMutationType().getUpdateMutation().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createUpdateMutationArguments(GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getInput().getTypeNameFor(type.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                name(options.getInput().getNameFor(type.getName())).
                type(options.getMutationType().getCreateMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(inputTypeName)) : GraphQLTypeReference.typeRef(inputTypeName)).
                build());

            if (options.getQueryType().isPartitionedByCrop() && !hasField(inputTypeName, "commonCropName")) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
                    build());
            }

            return arguments;
        }

        private GraphQLFieldDefinition.Builder generateDeleteGraphQLMutation(GraphQLObjectType type) {
            return GraphQLFieldDefinition.newFieldDefinition()
                .name(options.getDeleteMutationNameFor(type.getName()))
                .description(createDeleteMutationDescription(type))
                .arguments(createDeleteMutationArguments(type))
                .type(options.getMutationType().getDeleteMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(type.getName())) : GraphQLTypeReference.typeRef(type.getName())) ;
        }

        private String createDeleteMutationDescription(GraphQLObjectType type) {
            return options.getMutationType().getDeleteMutation().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createDeleteMutationArguments(GraphQLObjectType type) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            arguments.add(GraphQLArgument.newArgument().
                name(options.getIds().getNameFor(type.getName())).
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
