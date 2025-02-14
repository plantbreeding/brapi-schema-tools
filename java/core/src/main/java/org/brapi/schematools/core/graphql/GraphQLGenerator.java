package org.brapi.schematools.core.graphql;

import graphql.AssertException;
import graphql.TypeResolutionEnvironment;
import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.graphql.options.LinkType;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import static graphql.schema.GraphQLInterfaceType.newInterface;
import static graphql.schema.GraphQLObjectType.newObject;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.makeValidName;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;

/**
 * Generates a GraphQL schema from a BrAPI Json Schema.
 */
@AllArgsConstructor
@Slf4j
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
        return options.validate().asResponse().merge(
            schemaReader.readDirectories(schemaDirectory).mapResultToResponse(brAPISchemas -> new Generator(options, metadata, brAPISchemas).generate())) ;
    }

    @Getter
    private static class Generator {
        private final GraphQLGeneratorOptions options;
        private final GraphQLGeneratorMetadata metadata;
        private final Map<String, BrAPIClass> brAPISchemas;
        private final Map<String, GraphQLObjectType> objectOutputTypes;
        private final Map<String, GraphQLInterfaceType> interfaceTypes;
        private final Map<String, GraphQLUnionType> unionTypes;
        private final Map<String, GraphQLEnumType> enumTypes;
        private final Map<String, GraphQLNamedInputType> inputTypes;
        private final Map<String, String> listResponseTypesToBeCreated;
        private final Map<String, String> inputObjectTypeForListQueryToBeCreated;
        private final GraphQLCodeRegistry.Builder codeRegistry = GraphQLCodeRegistry.newCodeRegistry();

        public Generator(GraphQLGeneratorOptions options, GraphQLGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas) {
            this.options = options;
            this.metadata = metadata;
            this.brAPISchemas = brAPISchemas.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));
            objectOutputTypes = new HashMap<>();
            interfaceTypes = new HashMap<>();
            unionTypes = new HashMap<>();
            enumTypes = new HashMap<>();
            inputTypes = new HashMap<>();
            listResponseTypesToBeCreated = new HashMap<>();
            inputObjectTypeForListQueryToBeCreated = new HashMap<>();
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
                    filter(this::isInterface).
                    map(this::createInterfaceType).
                    collect(Response.toList())).
                map(() -> brAPISchemas.values().stream().
                    filter(this::isPrimaryModel).
                    map(this::createObjectType).
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

        private boolean isPrimaryModel(BrAPIClass type) {
            return type.getMetadata() != null && type.getMetadata().isPrimaryModel() ;
        }

        private boolean isNonPrimaryModel(BrAPIClass type) {
            return type.getMetadata() == null || !(type.getMetadata().isPrimaryModel() || type.getMetadata().isRequest() || type.getMetadata().isParameters() || type.getMetadata().isInterfaceClass());
        }

        private boolean isModel(BrAPIClass type) {
            return type.getMetadata() == null || !(type.getMetadata().isRequest() || type.getMetadata().isParameters());
        }

        private boolean isRequest(BrAPIClass type) {
            return type.getMetadata() != null && type.getMetadata().isRequest() ;
        }

        private boolean isInterface(BrAPIClass type) {
            return type.getMetadata() != null && type.getMetadata().isInterfaceClass() ;
        }

        private Response<GraphQLSchema> createSchema(List<GraphQLObjectType> primaryTypes) {
            GraphQLSchema.Builder builder = GraphQLSchema.newSchema();

            objectOutputTypes.values().forEach(builder::additionalType);
            interfaceTypes.values().forEach(builder::additionalType);
            enumTypes.values().forEach(builder::additionalType);
            inputTypes.values().forEach(builder::additionalType);
            unionTypes.values().forEach(builder::additionalType);

            new HashMap<>(listResponseTypesToBeCreated).forEach((key, value) -> {
                GraphQLObjectType type = objectOutputTypes.get(value);

                if (type != null) {
                    boolean paged = options.getQueryType().getListQuery().isPagedFor(type.getName());

                    builder.additionalType(createListResponse(paged, type)) ;
                } else {
                    log.warn(String.format("Can not create '%s' no type '%s'", key, value)) ;
                }
            });

            new HashMap<>(inputObjectTypeForListQueryToBeCreated).forEach((key, value) -> {
                BrAPIClass type = brAPISchemas.get(value);

                if (type != null) {
                    createInputObjectTypeForListQuery(type).onSuccessDoWithResult(builder::additionalType) ;
                } else {
                    log.warn(String.format("Can not create '%s' no type '%s'", key, value)) ;
                }
            });

            if (options.isGeneratingListQueries() && options.getQueryType().getListQuery().hasPaging() ) {
                builder.additionalType(createPageInputType());
                builder.additionalType(createPageType());
            }

            interfaceTypes.values().forEach(graphQLType -> codeRegistry.typeResolver(graphQLType, new InterfaceTypeResolver(graphQLType)));
            unionTypes.values().forEach(graphQLType -> codeRegistry.typeResolver(graphQLType, new UnionTypeResolver(graphQLType)));

            builder.codeRegistry(codeRegistry.build());

            try {
                return Response.empty()
                    .mapOnCondition(options.isGeneratingQueryType(), () -> generateQueryType(primaryTypes).onSuccessDoWithResult(builder::query))
                    .mapOnCondition(options.isGeneratingMutationType(), () -> generateMutationType(primaryTypes).onSuccessDoWithResult(builder::mutation))
                    .withResult(builder.build()) ;
            } catch (AssertException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<GraphQLObjectType> generateQueryType(List<GraphQLObjectType> primaryTypes) {
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

            return success(query.build()) ;
        }

        private Response<GraphQLObjectType> generateMutationType(List<GraphQLObjectType> primaryTypes) {
            GraphQLObjectType.Builder mutation = newObject().name(options.getMutationType().getName());

            primaryTypes.stream()
                .filter(type -> options.isGeneratingCreateMutationFor(type.getName()))
                .map(this::generateCreateGraphQLMutation)
                .forEach(mutation::field);

            primaryTypes.stream()
                .filter(type -> options.isGeneratingUpdateMutationFor(type.getName()))
                .map(this::generateUpdateGraphQLMutation)
                .forEach(mutation::field);

            return primaryTypes.stream()
                .filter(type -> options.isGeneratingDeleteMutationFor(type.getName()))
                .map(this::generateDeleteGraphQLMutation)
                .collect(Response.toList())
                .withResult(mutation.build());

        }

        private Response<GraphQLOutputType> createOutputType(BrAPIType type) {

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return createObjectType(brAPIObjectType).mapResult(t -> t);
            } else if (type instanceof BrAPIOneOfType brAPIOneOfType) {
                return createOutputType(brAPIOneOfType).mapResult(t -> t);
            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                return createOutputListType(brAPIArrayType).mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType brAPIReferenceType) {
                return createOutputReferenceType(brAPIReferenceType).mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType).mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                return createScalarType(brAPIPrimitiveType).mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown output type '%s'", type.getName()));
        }

        private Response<GraphQLTypeReference> createOutputReferenceType(BrAPIReferenceType type) {
            return success(GraphQLTypeReference.typeRef(type.getName()));
        }

        private Response<GraphQLList> createOutputListType(BrAPIArrayType type) {
            return createOutputType(type.getItems()).mapResult(GraphQLList::list);
        }

        private Response<GraphQLObjectType> createObjectType(BrAPIClass brAPIClass) {
            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                GraphQLObjectType existingType = objectOutputTypes.get(brAPIObjectType.getName());

                if (existingType != null) {
                    return success(existingType);
                }

                GraphQLObjectType.Builder builder = newObject().
                    name(brAPIObjectType.getName()).
                    description(brAPIObjectType.getDescription());

                brAPIObjectType.getInterfaces().forEach(interfaceType -> builder.withInterface(GraphQLTypeReference.typeRef(interfaceType.getName())));

                return brAPIObjectType.getInterfaces().stream().map(
                        interfaceType -> createInterfaceType(interfaceType).onSuccessDoWithResult(builder::withInterface)).collect(Response.toList())
                    .map(() -> extractProperties(brAPIObjectType)
                        .filter(property -> !LinkType.NONE.equals(options.getProperties().getLinkTypeFor(brAPIObjectType, property)))
                        .map(property -> createFieldDefinition(brAPIObjectType, property)).collect(Response.toList()))
                    .onSuccessDoWithResult(builder::fields)
                    .map(() -> addObjectType(builder.build()));
            } else {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("Can not create GraphQLObjectType, type is not BrAPIObjectType, but was '%s'", brAPIClass.getClass()));
            }
        }

        private Response<GraphQLInterfaceType> createInterfaceType(BrAPIClass brAPIClass) {
            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                GraphQLInterfaceType existingType = interfaceTypes.get(brAPIObjectType.getName());

                if (existingType != null) {
                    return success(existingType);
                }

                GraphQLInterfaceType.Builder builder = newInterface().
                    name(brAPIObjectType.getName()).
                    description(brAPIObjectType.getDescription());

                brAPIObjectType.getInterfaces().forEach(interfaceType -> builder.withInterface(GraphQLTypeReference.typeRef(interfaceType.getName())));

                return brAPIObjectType.getInterfaces().stream().map(
                        interfaceType -> createInterfaceType(interfaceType).onSuccessDoWithResult(builder::withInterface)).collect(Response.toList())
                    .map(() -> brAPIObjectType.getProperties().stream()
                        .filter(property -> !LinkType.NONE.equals(options.getProperties().getLinkTypeFor(brAPIObjectType, property)))
                        .map(property -> createFieldDefinition(brAPIObjectType, property)).collect(Response.toList()))
                    .onSuccessDoWithResult(builder::fields)
                    .map(() -> addInterfaceType(builder.build()));
            } else {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("Can not create GraphQLInterfaceType, type is not BrAPIObjectType, but was '%s'", brAPIClass.getClass()));
            }
        }

        private Response<GraphQLFieldDefinition> createFieldDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {
            LinkType linkType = options.getProperties().getLinkTypeFor(parentType, property);

            return switch (linkType) {
                case EMBEDDED -> {
                    GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                        .name(property.getName())
                        .description(property.getDescription());

                    yield createOutputType(property.getType())
                        .onSuccessDoWithResult(builder::type)
                        .map(() -> success(builder.build()));
                }
                case SUB_QUERY -> {
                    GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                        .name(property.getName())
                        .description(property.getDescription());

                    if (property.getType() instanceof BrAPIArrayType) {
                        BrAPIType type = unwrapType(property.getType());

                        boolean paged = options.getQueryType().getListQuery().isPagedFor(type.getName());
                        boolean hasInput = options.getQueryType().getListQuery().hasInputFor(type.getName());

                        String responseTypeName = options.getQueryType().getListQuery().getResponseTypeNameForType(type.getName());
                        String inputTypeName = options.getQueryInputTypeNameFor(type.getName()) ;

                        listResponseTypesToBeCreated.put(responseTypeName, type.getName()) ;
                        inputObjectTypeForListQueryToBeCreated.put(inputTypeName, type.getName()) ;

                        yield success(GraphQLTypeReference.typeRef(responseTypeName))
                            .onSuccessDoWithResult(builder::type)
                            .map(() -> success(createListQueryArguments(paged, hasInput, type.getName())))
                            .onSuccessDoWithResult(builder::arguments)
                            .map(() -> success(builder.build()));
                    } else {
                        yield createOutputType(property.getType())
                            .onSuccessDoWithResult(builder::type)
                            .map(() -> success(builder.build()));
                    }
                }
                case ID -> {
                    GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                        .description(property.getDescription());

                    if (property.getType() instanceof BrAPIArrayType) {
                        builder
                            .name(options.getProperties().getIds().getIdFieldFor(property))
                            .type(GraphQLList.list(options.isUsingIDType() ? GraphQLID : GraphQLString)) ;
                    } else {
                        builder.name(options.getProperties().getIds().getIdsFieldFor(property))
                            .type(options.isUsingIDType() ? GraphQLID : GraphQLString) ;
                    }

                    yield success(builder.build()) ;
                }
                case NONE -> fail(Response.ErrorType.VALIDATION, "Should have been filtered out!");
            } ;
        }

        private Response<GraphQLTypeReference> createInputReferenceType(BrAPIReferenceType type) {
            BrAPIClass referencedSchema = this.brAPISchemas.get(type.getName());

            if (referencedSchema != null && !(referencedSchema instanceof BrAPIEnumType)) {
                String inputTypeName = options.getInput().getTypeNameFor(referencedSchema) ;

                if (isModel(referencedSchema)) {
                    return createInputObjectTypeForModel(referencedSchema).
                        withResult(GraphQLTypeReference.typeRef(inputTypeName)) ;
                } else if (isRequest(referencedSchema)) {
                    return createInputTypeFromClass(inputTypeName, referencedSchema).
                        withResult(GraphQLTypeReference.typeRef(inputTypeName));
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
            BrAPIClass requestSchema = this.brAPISchemas.get(String.format("%sRequest", type.getName()));

            return createInputTypeFromClass(options.getQueryInputTypeNameFor(type), Objects.requireNonNullElse(requestSchema, type));
        }

        private Response<GraphQLNamedInputType> createInputObjectTypeForSearchQuery(BrAPIClass type) {
            BrAPIClass requestSchema = this.brAPISchemas.get(String.format("%sRequest", type.getName()));

            return createInputTypeFromClass(options.getQueryInputTypeNameFor(type), Objects.requireNonNullElse(requestSchema, type));
        }

        private Response<GraphQLNamedInputType> createInputTypeFromClass(String name, BrAPIClass type) {
            if (type instanceof BrAPIObjectType brAPIObjectType) {
                if (type.getName().endsWith("Request")) {
                    return createInputObjectType(options.getInput().getTypeNameForQuery(
                            options.getQueryType().getListQuery().getNameFor(options.getPluralFor(type.getName().substring(0, type.getName().length() - 7)))), type)
                        .onSuccessDoWithResult(inputType -> inputObjectTypeForListQueryToBeCreated.remove(inputType.getName()));
                } else {
                    return createInputObjectType(name, brAPIObjectType)
                        .onSuccessDoWithResult(inputType -> inputObjectTypeForListQueryToBeCreated.remove(inputType.getName()))
                        .mapResult(t -> t);
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

                boolean hasExternalReferences = brAPIObjectType.getProperties().stream().anyMatch(property -> property.getName().equals("externalReferences")) ;

                return brAPIObjectType.getProperties().stream().map(this::createInputObjectField).collect(Response.toList())
                    .onSuccessDoWithResult(builder::fields)
                    .mapOnCondition(hasExternalReferences, () -> createExternalReferencesInputObjectField().onSuccessDoWithResult(builder::field))
                    .map(() -> addInputObjectType(builder.build()));
            } else if (type instanceof BrAPIOneOfType brAPIOneOfType) {
                GraphQLInputObjectType.Builder builder = newInputObject().
                    name(name).
                    description(brAPIOneOfType.getDescription());

                if (options.isMergingOneOfType(type)) {
                    return brAPIOneOfType.getPossibleTypes().stream().flatMap(this::extractProperties)
                        .map(this::createInputObjectField).collect(Response.toList())
                        .mapResultToResponse(this::removeDuplicates)
                        .onSuccessDoWithResult(builder::fields)
                        .map(() -> addInputObjectType(builder.build()));
                } else {
                    return brAPIOneOfType.getPossibleTypes().stream()
                        .map(this::createInputType).collect(Response.toList())
                        .mapResultToResponse(this::createInputObjectFieldForTypes)
                        .onSuccessDoWithResult(builder::fields)
                        .map(() -> addInputObjectType(builder.build()));
                }
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Input object '%s' must be BrAPIObjectType or BrAPIOneOfType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
            }
        }

        private Response<List<GraphQLInputObjectField>> createInputObjectFieldForTypes(List<GraphQLInputType> graphQLInputTypes) {
            return graphQLInputTypes.stream().map(this::createInputObjectFieldForType).collect(Response.toList()) ;
        }

        private Response<GraphQLInputObjectField> createInputObjectFieldForType(GraphQLInputType graphQLInputType) {
            if (graphQLInputType instanceof GraphQLNamedInputType graphQLNamedInputType) {
                return success(newInputObjectField()
                    .name(StringUtils.toParameterCase(graphQLNamedInputType.getName()))
                    .description(String.format("Field for possible type '%s'", graphQLNamedInputType.getName()))
                    .type(graphQLInputType)
                    .build());
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Input type must be GraphQLNamedInputType but was '%s'", graphQLInputType.getClass().getSimpleName())) ;
            }
        }

        private Response<List<GraphQLInputObjectField>> removeDuplicates(List<GraphQLInputObjectField> graphQLInputObjectFields) {
            try {
                return Response.success(new ArrayList<>(graphQLInputObjectFields.stream().
                    collect(Collectors.toMap(GraphQLInputObjectField::getName, Function.identity(), this::merge)).values()));
            } catch (RuntimeException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private GraphQLInputObjectField merge(GraphQLInputObjectField fieldA, GraphQLInputObjectField fieldB) {
            if (!typeEquals(fieldA.getType(), fieldB.getType())) {
                throw new RuntimeException(String.format("Can not merge fields called '%s', field A has type '%s' and field B has type '%s",
                    fieldA.getName(), fieldA.getType(), fieldB.getType()));
            }

            return fieldA ;
        }

        private boolean typeEquals(GraphQLType typeA, GraphQLType typeB) {
            if (typeA.equals(typeB)) {
                return true ;
            } else {
                if (typeA instanceof GraphQLNamedType graphQLNamedTypeA && typeB instanceof GraphQLNamedType graphQLNamedTypeB) {
                    return graphQLNamedTypeA.equals(graphQLNamedTypeB) ;
                } else if (typeA instanceof GraphQLList graphQLListA && typeB instanceof GraphQLList graphQLListB) {
                    return typeEquals(graphQLListA.getWrappedType(), graphQLListB.getWrappedType()) ;
                }
            }

            return false ;
        }

        private Stream<BrAPIObjectProperty> extractProperties(BrAPIType brAPIType) {
            BrAPIType type ;

            if (brAPIType instanceof BrAPIReferenceType brAPIReferenceType) {
                BrAPIClass referencedSchema = this.brAPISchemas.get(brAPIReferenceType.getName());
                type = referencedSchema != null ? referencedSchema : brAPIType ;
            } else {
                type = brAPIType;
            }

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return brAPIObjectType.getProperties().stream() ;
            } else {
                return Stream.empty() ;
            }
        }

        private Response<GraphQLInputType> createInputType(BrAPIType type) {

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return createInputObjectTypeForModel(brAPIObjectType).mapResult(t -> t)
                    .mapResult(t -> t);
            } else if (type instanceof BrAPIOneOfType brAPIOneOfType) {
                return createInputObjectType(options.getInput().getTypeNameFor(brAPIOneOfType), brAPIOneOfType)
                    .mapResult(t -> t);
            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                return createInputListType(brAPIArrayType)
                    .mapResult(t -> t);
            } else if (type instanceof BrAPIReferenceType brAPIReferenceType) {
                return createInputReferenceType(brAPIReferenceType)
                    .mapResult(t -> t);
            } else if (type instanceof BrAPIEnumType brAPIEnumType) {
                return createEnumType(brAPIEnumType)
                    .mapResult(t -> t);
            } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                return createScalarType(brAPIPrimitiveType)
                    .mapResult(t -> t);
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown input type '%s' for '%s'", type.getClass().getSimpleName(), type.getName()));
        }

        private Response<GraphQLInputObjectField> createInputObjectField(BrAPIObjectProperty property) {
            if (property.getType() instanceof BrAPIClass brAPIClass && isPrimaryModel(brAPIClass)) {
                GraphQLInputObjectField.Builder builder = newInputObjectField()
                    .name(options.getProperties().getIds().getIDFieldFor(property.getType()))
                    .description(property.getDescription())
                    .type(options.isUsingIDType() ? GraphQLID : GraphQLString);

                return success(builder.build());

            } else {
                GraphQLInputObjectField.Builder builder = newInputObjectField().
                    name(property.getName()).
                    description(property.getDescription());

                return createInputType(property.getType()).
                    onSuccessDoWithResult(builder::type).
                    map(() -> success(builder.build()));
            }
        }

        private Response<GraphQLInputObjectField> createExternalReferencesInputObjectField() {
            return success(newInputObjectField()
                .name("externalReferences")
                .description("Filter by External References")
                .type(GraphQLList.list(GraphQLTypeReference.typeRef("ExternalReferenceInput")))
                .build()) ;
        }

        private Response<GraphQLNamedOutputType> createOutputType(BrAPIOneOfType type) {

            GraphQLUnionType existingType = unionTypes.
                get(type.getName());

            if (existingType != null) {
                return success(existingType);
            }

            GraphQLUnionType.Builder builder = GraphQLUnionType.newUnionType().
                name(type.getName()).
                description(type.getDescription());

            return type.getPossibleTypes().stream().map(this::createNamedOutputType).collect(Response.toList())
                .onSuccessDoWithResult(builder::replacePossibleTypes)
                .map(() -> addUnionType(builder.build()))
                .mapResult(result -> result);
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

        private Response<GraphQLInterfaceType> addInterfaceType(GraphQLInterfaceType type) {
            interfaceTypes.put(type.getName(), type);

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
                name(options.getProperties().getIds().getNameFor(type.getName())).
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
                arguments(createListQueryArguments(paged, hasInput, type.getName())).
                type(createListResponse(paged, type));
        }

        private String createListQueryDescription(GraphQLObjectType type) {
            return options.getQueryType().getListQuery().getDescriptionFor(type.getName());
        }

        private List<GraphQLArgument> createListQueryArguments(boolean paged, boolean hasInput, String typeName) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getQueryInputTypeNameFor(typeName) ;

            if (hasInput) {
                arguments.add(GraphQLArgument.newArgument().
                    name(options.getQueryInputParameterNameFor(typeName)).
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

        private BrAPIType unwrapType(BrAPIType type) {
            if (type instanceof BrAPIArrayType brAPIArrayType) {
                return unwrapType(brAPIArrayType.getItems());
            }

            if (type instanceof BrAPIReferenceType brAPIReferenceType && brAPISchemas.containsKey(brAPIReferenceType.getName())) {
                return this.brAPISchemas.get(brAPIReferenceType.getName()) ;
            }

            return type;
        }

        private boolean hasField(String typeName, String fieldName) {
            GraphQLNamedInputType type = this.inputTypes.get(typeName);

            if (type instanceof GraphQLInputObjectType graphQLInputObjectType) {
                return graphQLInputObjectType.getField(fieldName) == null ;
            }

            return false ;
        }

        private GraphQLOutputType createListResponse(boolean paged, GraphQLObjectType graphQLObjectType) {
            String name = String.format(options.getQueryType().getListQuery().getResponseTypeNameForType(graphQLObjectType.getName())) ;

            if (objectOutputTypes.containsKey(name)) {
                // TODO possible that the cache version is page and this is not, and vise versa.
                return objectOutputTypes.get(name);
            }

            GraphQLObjectType.Builder builder = newObject().
                name(name).
                field(createListDataField(graphQLObjectType));

            if (paged) {
                builder.field(GraphQLFieldDefinition.newFieldDefinition().
                    name(options.getQueryType().getListQuery().getPageFieldName()).
                    type(GraphQLTypeReference.typeRef(options.getQueryType().getListQuery().getPageTypeName())).
                    build());
            }

            listResponseTypesToBeCreated.remove(name) ;

            return addObjectType(builder.build()).getResult() ;
        }

        private GraphQLFieldDefinition createListDataField(GraphQLObjectType graphQLObjectType) {
            return GraphQLFieldDefinition.newFieldDefinition().
                name(options.getQueryType().getListQuery().getDataFieldName()).
                type(GraphQLList.list(GraphQLTypeReference.typeRef(graphQLObjectType.getName()))).
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

        private GraphQLFieldDefinition.Builder generateSearchGraphQLQuery(GraphQLObjectType graphQLObjectType) {
            String queryName = options.getSearchQueryNameFor(graphQLObjectType.getName()) ;

            return GraphQLFieldDefinition.newFieldDefinition()
                .name(queryName)
                .description(createSearchQueryDescription(graphQLObjectType))
                .arguments(createSearchQueryArguments(graphQLObjectType))
                .type(createSearchResponse(queryName, graphQLObjectType));
        }

        private String createSearchQueryDescription(GraphQLObjectType graphQLObjectType) {
            return options.getQueryType().getSearchQuery().getDescriptionFor(graphQLObjectType.getName());
        }

        private List<GraphQLArgument> createSearchQueryArguments(GraphQLObjectType graphQLObjectType) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getQueryInputTypeNameFor(graphQLObjectType.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                name(options.getInput().getNameFor(graphQLObjectType.getName())).
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

        private GraphQLOutputType createSearchResponse(String queryName, GraphQLObjectType type) {
            GraphQLObjectType.Builder builder = newObject().
                name(String.format(options.getQueryType().getSearchQuery().getResponseTypeNameForQuery(queryName))).
                field(GraphQLFieldDefinition.newFieldDefinition().
                    name(options.getQueryType().getSearchQuery().getSearchIdFieldName()).
                    type(GraphQLString).
                    build()).
                field(createListDataField(type));

            return builder.build();
        }

        private GraphQLFieldDefinition.Builder generateCreateGraphQLMutation(GraphQLObjectType graphQLObjectType) {
            return newFieldDefinition()
                .name(options.getCreateMutationNameFor(graphQLObjectType.getName()))
                .description(createCreateMutationDescription(graphQLObjectType))
                .arguments(createCreateMutationArguments(graphQLObjectType))
                .type(options.getMutationType().getCreateMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(graphQLObjectType.getName())) : GraphQLTypeReference.typeRef(graphQLObjectType.getName()));
        }

        private String createCreateMutationDescription(GraphQLObjectType graphQLObjectType) {
            return options.getMutationType().getCreateMutation().getDescriptionFor(graphQLObjectType.getName());
        }

        private List<GraphQLArgument> createCreateMutationArguments(GraphQLObjectType graphQLObjectType) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getInput().getTypeNameFor(graphQLObjectType.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                    name(options.getInput().getNameFor(graphQLObjectType.getName())).
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

        private GraphQLFieldDefinition.Builder generateUpdateGraphQLMutation(GraphQLObjectType graphQLObjectType) {
            return GraphQLFieldDefinition.newFieldDefinition()
                .name(options.getUpdateMutationNameFor(graphQLObjectType.getName()))
                .description(createUpdateMutationDescription(graphQLObjectType))
                .arguments(createUpdateMutationArguments(graphQLObjectType))
                .type(options.getMutationType().getUpdateMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(graphQLObjectType.getName())) : GraphQLTypeReference.typeRef(graphQLObjectType.getName())) ;
        }

        private String createUpdateMutationDescription(GraphQLObjectType graphQLObjectType) {
            return options.getMutationType().getUpdateMutation().getDescriptionFor(graphQLObjectType.getName());
        }

        private List<GraphQLArgument> createUpdateMutationArguments(GraphQLObjectType graphQLObjectType) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String inputTypeName = options.getInput().getTypeNameFor(graphQLObjectType.getName()) ;

            arguments.add(GraphQLArgument.newArgument().
                name(options.getInput().getNameFor(graphQLObjectType.getName())).
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

        private Response<GraphQLFieldDefinition.Builder> generateDeleteGraphQLMutation(GraphQLObjectType graphQLObjectType) {
            GraphQLFieldDefinition.Builder builder = newFieldDefinition()
                .name(options.getDeleteMutationNameFor(graphQLObjectType.getName()))
                .description(createDeleteMutationDescription(graphQLObjectType))
                .type(options.getMutationType().getDeleteMutation().isMultiple() ?
                    GraphQLList.list(GraphQLTypeReference.typeRef(graphQLObjectType.getName())) : GraphQLTypeReference.typeRef(graphQLObjectType.getName())) ;

            return createDeleteMutationArguments(graphQLObjectType)
                .onSuccessDoWithResult(builder::arguments)
                .withResult(builder);
        }

        private String createDeleteMutationDescription(GraphQLObjectType graphQLObjectType) {
            return options.getMutationType().getDeleteMutation().getDescriptionFor(graphQLObjectType.getName());
        }

        private Response<List<GraphQLArgument>> createDeleteMutationArguments(GraphQLObjectType graphQLObjectType) {
            List<GraphQLArgument> arguments = new ArrayList<>();

            String idField = options.getProperties().getIds().getIDFieldFor(graphQLObjectType.getName()) ;

            if (graphQLObjectType.getFields().stream().noneMatch(field -> field.getName().equals(idField))) {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find field '%s' in type '%s'", idField, graphQLObjectType.getName())) ;
            }

            GraphQLScalarType idType = options.isUsingIDType() ? GraphQLID : GraphQLString ;

            if (options.getMutationType().getDeleteMutation().isMultiple()) {
                arguments.add(GraphQLArgument.newArgument().
                    name(toPlural(idField)).
                    type(GraphQLList.list(idType)).
                    build());
            } else {
                arguments.add(GraphQLArgument.newArgument().
                    name(options.getProperties().getIds().getNameFor(graphQLObjectType.getName())).
                    type(idType).
                    build());
            }

            if (options.getQueryType().isPartitionedByCrop()) {
                arguments.add(GraphQLArgument.newArgument().
                    name("commonCropName").
                    type(GraphQLString).
                    build());
            }

            return success(arguments) ;
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

    @AllArgsConstructor
    private static class InterfaceTypeResolver implements TypeResolver {

        private GraphQLInterfaceType interfaceType;

        @Override
        public GraphQLObjectType getType(TypeResolutionEnvironment schemaName) {
            return (GraphQLObjectType) this.interfaceType.getChildren().get(0) ;
        }
    }
}
