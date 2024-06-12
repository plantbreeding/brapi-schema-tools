package org.brapi.schematools.core.openapi;

import graphql.schema.GraphQLSchema;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.openapi.metadata.OpenAPIGeneratorMetadata;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Generates a OpenAPI Specification from a BrAPI Json Schema.
 */
@AllArgsConstructor
public class OpenAPIGenerator {

    private final BrAPISchemaReader schemaReader;
    private final OpenAPIComponentsReader componentsReader;
    private final OpenAPIGeneratorOptions options ;

    /**
     * Creates a OpenAPIGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link OpenAPIGeneratorOptions}.
     */
    public OpenAPIGenerator() {
        this(new BrAPISchemaReader(), new OpenAPIComponentsReader(), OpenAPIGeneratorOptions.load()) ;
    }

    /**
     * Creates a GraphQLGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link OpenAPIGeneratorOptions}.
     * @param options The options to be used in the generation.
     */
    public OpenAPIGenerator(OpenAPIGeneratorOptions options) {
        this(new BrAPISchemaReader(), new OpenAPIComponentsReader(), options) ;
    }

    /**
     * Generates a list of {@link OpenAPI} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules. The list will contain a single {@link OpenAPI} or separate {@link OpenAPI}
     * for each module. See {@link OpenAPIGeneratorOptions#separatingByModule}.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param componentsDirectory the path to the additional OpenAPI components needed to generate the Specification
     * @return a list of {@link OpenAPI} generated from the complete BrAPI Specification
     */
    public Response<List<OpenAPI>> generate(Path schemaDirectory, Path componentsDirectory) {
        return generate(schemaDirectory, componentsDirectory, new OpenAPIGeneratorMetadata()) ;
    }

    /**
     * Generates a list of {@link OpenAPI} from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules. The list will contain a single {@link OpenAPI} or separate {@link OpenAPI}
     * for each module. See {@link OpenAPIGeneratorOptions#separatingByModule}.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param componentsDirectory the path to the additional OpenAPI components needed to generate the Specification
     * @param metadata additional metadata that is used in the generation
     * @return a list of {@link OpenAPI} generated from the complete BrAPI Specification
     */
    public Response<List<OpenAPI>> generate(Path schemaDirectory, Path componentsDirectory, OpenAPIGeneratorMetadata metadata) {

        try {
            Components components = componentsReader.readComponents(componentsDirectory) ;

            return schemaReader.readDirectories(schemaDirectory).mapResultToResponse(
                brAPISchemas -> new OpenAPIGenerator.Generator(options, metadata, brAPISchemas, components).generate());
        } catch (BrAPISchemaReaderException | OpenAPIComponentsException e) {
            return fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    private static class Generator {
        private final OpenAPIGeneratorOptions options;

        private final OpenAPIGeneratorMetadata metadata;
        private final Map<String, BrAPIClass> brAPISchemas;

        private final Map<String, Parameter> parameters;
        private final Map<String, ApiResponse> responses;
        private final Map<String, Schema> schemas;
        private final Map<String, SecurityScheme> securitySchemes;

        public Generator(OpenAPIGeneratorOptions options, OpenAPIGeneratorMetadata metadata, List<BrAPIClass> brAPISchemas, Components components) {
            this.options = options;
            this.metadata = metadata ;
            this.brAPISchemas = brAPISchemas.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));
            this.parameters = new HashMap<>(components.getParameters());
            this.responses = new HashMap<>(components.getResponses());
            this.schemas = new HashMap<>(components.getSchemas());
            this.securitySchemes = new HashMap<>(components.getSecuritySchemes());
        }

        public Response<List<OpenAPI>> generate() {
            if (options.isSeparatingByModule()) {
                return brAPISchemas.values().stream().
                    filter(type -> Objects.nonNull(type.getModule())).
                    collect(Collectors.groupingBy(BrAPIClass::getModule, toList())).
                    entrySet().stream().map(entry -> generate(entry.getKey(), entry.getValue())).
                    collect(Response.toList());
            } else {
                return generate("BrAPI", brAPISchemas.values().stream().filter(type -> Objects.nonNull(type.getModule())).toList()).
                    mapResult(Collections::singletonList);
            }
        }

        private Response<OpenAPI> generate(String title, Collection<BrAPIClass> types) {

            OpenAPI openAPI = new OpenAPI();

            Info info = new Info();

            info.setTitle(title);
            info.setVersion(metadata.getVersion() != null ?metadata.getVersion() : "0.0.0");

            openAPI.setInfo(info);

            // TODO merge in openAPIMetadata

            List<BrAPIClass> nonPrimaryTypes = types.stream().filter(type -> Objects.isNull(type.getMetadata()) || !type.getMetadata().isPrimaryModel()).toList();

            List<BrAPIObjectType> primaryTypes = types.stream().
                filter(type -> type instanceof BrAPIObjectType).
                filter(type -> Objects.nonNull(type.getMetadata()) && type.getMetadata().isPrimaryModel()).
                map(type -> (BrAPIObjectType)type).
                toList();

            return Response.empty().
                mergeOnCondition(options.isGeneratingEndpoint(), // these are GET and POST endpoints with the pattern /<entity-plural> e.g. /locations
                    () -> primaryTypes.stream().
                        filter(type -> options.isGeneratingEndpointFor(type.getName())).
                        map(type -> generatePathItem(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createPathItemName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                mergeOnCondition(options.isGeneratingEndpointWithId(),  // these are GET, PUT and DELETE endpoints with the pattern /<entity-plural>/{<entity-id>} e.g. /locations/{locationDbId}
                    () -> primaryTypes.stream().
                        filter(type -> options.isGeneratingEndpointNameWithIdFor(type.getName())).
                        map(type -> createPathItemWithId(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createPathItemWithIdName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                mergeOnCondition(options.isGeneratingSearchEndpoint(),  // this is a POST endpoint with the pattern /search/<entity-plural> e.g. /search/locations
                    () -> primaryTypes.stream().
                        filter(type -> options.isGeneratingSearchEndpointFor(type.getName())).
                        map(type -> createSearchPathItem(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createSearchPathItemName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                mergeOnCondition(options.isGeneratingSearchEndpoint(), // this is a GET endpoint are endpoints with the pattern /search/<entity-plural>/{searchResultsDbId} e.g. /search/locations/{searchResultsDbId}
                    () -> primaryTypes.stream().
                        filter(type -> options.isGeneratingSearchEndpointFor(type.getName())).
                        map(type -> createSearchPathItemWithId(type).onSuccessDoWithResult(
                            pathItem -> {
                                openAPI.path(createSearchPathItemWithIdName(type.getName()), pathItem);
                            })).collect(Response.toList())).
                merge(() -> generateComponents(primaryTypes, nonPrimaryTypes).onSuccessDoWithResult(openAPI::components)).
                map(() -> success(openAPI));

        }

        private String createPathItemName(String entityName) {
            return String.format("/%s", toParameterCase(options.getPluralFor(entityName)));
        }

        private Response<PathItem> generatePathItem(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            return Response.empty().
                mergeOnCondition(options.isGeneratingListGetEndpointFor(type.getName()), () -> generateListGetOperation(type).onSuccessDoWithResult(pathItem::setGet)).
                mergeOnCondition(options.isGeneratingPostEndpointFor(type.getName()), () -> generatePostOperation(type).onSuccessDoWithResult(pathItem::setPost)).
                merge(() -> generateListResponse(type)).
                map(() -> success(pathItem));
        }

        private String createPathItemWithIdName(String entityName) {
            return String.format("/%s/{%s}", toParameterCase(options.getPluralFor(entityName)), String.format(options.getIds().getNameFormat(), toParameterCase(entityName)));
        }

        public Response<PathItem> createPathItemWithId(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            return Response.empty().
                mergeOnCondition(options.isGeneratingSingleGetEndpointFor(type.getName()), () -> generateSingleGetOperation(type).onSuccessDoWithResult(pathItem::setGet)).
                mergeOnCondition(options.isGeneratingPutEndpointFor(type.getName()), () -> generatePutOperation(type).onSuccessDoWithResult(pathItem::setPut)).
                mergeOnCondition(options.isGeneratingDeleteEndpointFor(type.getName()), () -> generateDeleteOperation(type).onSuccessDoWithResult(pathItem::setDelete)).
                merge(() -> generateSingleResponse(type)).
                map(() -> success(pathItem));
        }

        private Response<ApiResponse> generateSingleResponse(BrAPIObjectType type) {

            String name = options.getListResponseNameFor(type.getName());

            ApiResponse apiResponse = new ApiResponse().description("OK").content(
                new Content().addMediaType("application/json",
                    new MediaType().schema(
                        new ObjectSchema().title(name).
                            addProperty("'@context'", new ObjectSchema().$ref(createSchemaRef("Context"))).
                            addProperty("metadata", new ObjectSchema().$ref(createSchemaRef("metadata"))).
                            addProperty("result", new ObjectSchema().$ref(createSchemaRef(type.getName()))).
                            addRequiredItem("metadata").
                            addRequiredItem("result")
                    )
                )
            );

            responses.put(name, apiResponse) ;

            return Response.success(apiResponse) ;
        }

        private Response<ApiResponse> generateListResponse(BrAPIObjectType type) {
            String name = options.getSingleResponseNameFor(type.getName());

            ApiResponse apiResponse = new ApiResponse().description("OK").content(
                new Content().addMediaType("application/json",
                    new MediaType().schema(
                        new ObjectSchema().title(name).
                            addProperty("'@context'", new ObjectSchema().$ref(createSchemaRef("Context"))).
                            addProperty("metadata", new ObjectSchema().$ref(createSchemaRef("Context"))).
                            addProperty("result", new ObjectSchema().
                                addProperty("data", new ArraySchema().items(new ObjectSchema().$ref(createSchemaRef(type.getName())))).
                                addRequiredItem("data")
                            ).
                            addRequiredItem("metadata").
                            addRequiredItem("result")
                    )
                )
            );

            responses.put(name, apiResponse) ;

            return Response.success(apiResponse) ;
        }

        private Response<Operation> generateListGetOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getListGet().getSummaries().getOrDefault(type.getName(), String.format(options.getListGet().getSummaryFormat(), options.getPluralFor(type.getName())))) ;
            operation.setDescription(metadata.getListGet().getDescriptions().getOrDefault(type.getName(), String.format(options.getListGet().getDescriptionFormat(), options.getPluralFor(type.getName()))));

            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            return createListGetParametersFor(type).
                onSuccessDoWithResult(operation::parameters).
                map(() -> success(operation));
        }

        private Response<List<Parameter>> createListGetParametersFor(BrAPIObjectType type) {
            BrAPIClass requestSchema = this.brAPISchemas.get(String.format("%sRequest", type.getName()));

            if (requestSchema == null) {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find '%sRequest' to create parameters for list get endpoint for '%s'", type.getName(), createSearchPathItemName(type.getName()))) ;
            }

            if (requestSchema instanceof BrAPIObjectType brAPIObjectType) {
                List<Parameter> parameters = new ArrayList<>();

                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceID"));
                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceId")); // TODO depreciated, remove?
                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceSource"));
                parameters.add(new Parameter().$ref("#/components/parameters/page"));
                parameters.add(new Parameter().$ref("#/components/parameters/pageSize"));
                parameters.add(new Parameter().$ref("#/components/parameters/authorizationHeader"));

                return brAPIObjectType.getProperties().stream().map(this::createListGetParameter).collect(Response.toList()).
                    onSuccessDoWithResult(result -> parameters.addAll(0, result)).
                    map(() -> success(parameters));
            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("'%sRequest' must be BrAPIObjectType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
            }
        }

        private Response<Parameter> createListGetParameter(BrAPIObjectProperty property) {
            return createSchemaForType(property.getType()).mapResult(
                schema -> new Parameter().
                    name(options.getSingularForProperty(property.getName())).
                    in("query").
                    description(property.getDescription()).
                    required(property.isRequired()).
                    schema(upwrapSchema(schema))) ;
        }

        private Schema upwrapSchema(Schema schema) {
            if (schema instanceof ArraySchema) {
                return schema.getItems() ;
            } else {
                return schema ;
            }
        }

        private Response<Operation> generatePostOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getPost().getSummaries().getOrDefault(type.getName(), String.format(options.getPost().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getPost().getDescriptions().getOrDefault(type.getName(), String.format(options.getPost().getDescriptionFormat(), type.getName())));

            operation.addParametersItem(new Parameter().$ref("#/components/parameters/authorizationHeader")) ;

            String requestBodyName = options.getCreatingNewRequestFor().getOrDefault(type.getName(), options.isCreatingNewRequest()) ?
                String.format(options.getNewRequestNameFormat(), type.getName()) : type.getName() ;

            operation.requestBody(
                new RequestBody().content(
                    new Content().addMediaType("application/json",
                        new MediaType().schema(
                            new ArraySchema().$ref(String.format("#/components/schemas/%s", requestBodyName))))));

            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            return success(operation);
        }

        private Response<Operation> generateSingleGetOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getSingleGet().getSummaries().getOrDefault(type.getName(), String.format(options.getSingleGet().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getSingleGet().getDescriptions().getOrDefault(type.getName(), String.format(options.getSingleGet().getDescriptionFormat(), type.getName())));

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            return success(operation);
        }

        private Response<Operation> generatePutOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getPut().getSummaries().getOrDefault(type.getName(), String.format(options.getPut().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getPut().getDescriptions().getOrDefault(type.getName(), String.format(options.getPut().getDescriptionFormat(), type.getName())));

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            return success(operation);
        }

        private Response<Operation> generateDeleteOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getDelete().getSummaries().getOrDefault(type.getName(), String.format(options.getDelete().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getDelete().getDescriptions().getOrDefault(type.getName(), String.format(options.getDelete().getDescriptionFormat(), type.getName())));

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            return success(operation);
        }

        private ApiResponses createSingleApiResponses(BrAPIObjectType type) {
            return addStandardApiResponses(new ApiResponses().
                addApiResponse("200", new ApiResponse().$ref(String.format("#/components/responses/%sSingleResponse", type.getName())))) ;
        }

        private ApiResponses createListApiResponses(BrAPIObjectType type) {
            return addStandardApiResponses(new ApiResponses().
                addApiResponse("200", new ApiResponse().$ref(String.format("#/components/responses/%sListResponse", type.getName())))) ;
        }

        private ApiResponses addStandardApiResponses(ApiResponses apiResponses) {
            return apiResponses.
                addApiResponse("400", new ApiResponse().$ref("#/components/responses/400BadRequest")).
                addApiResponse("401", new ApiResponse().$ref("#/components/responses/401Unauthorized")).
                addApiResponse("403", new ApiResponse().$ref("#/components/responses/403Forbidden")) ;
        }

        private String createSearchPathItemName(String entityName) {
            return String.format("/search/%s", toParameterCase(options.getPluralFor(entityName)));
        }

        private String createSearchPathItemWithIdName(String entityName) {
            return String.format("/search/%s/{%s}", toParameterCase(options.getPluralFor(entityName)), String.format(options.getIds().getNameFormat(), toParameterCase(entityName)));
        }

        public Response<PathItem> createSearchPathItem(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            Operation operation = new Operation();

            operation.setSummary(metadata.getSearch().getSummaries().getOrDefault(type.getName(), String.format(options.getSearch().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getSearch().getDescriptions().getOrDefault(type.getName(), String.format(options.getSearch().getSubmitDescriptionFormat(), type.getName(), toParameterCase(type.getName()))));

            operation.responses(createSearchPostResponseRefs(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            pathItem.setPost(operation);

            return success(pathItem);
        }

        private ApiResponses createSearchPostResponseRefs(BrAPIObjectType type) {
            return addStandardApiResponses(new ApiResponses().
                addApiResponse("200", new ApiResponse().$ref(String.format("#/components/responses/%sListResponse", type.getName()))).
                addApiResponse("202", new ApiResponse().$ref("#/components/responses/202AcceptedSearchResponse"))) ;
        }

        public Response<PathItem> createSearchPathItemWithId(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            Operation operation = new Operation();

            operation.setSummary(metadata.getSearch().getSummaries().getOrDefault(type.getName(), String.format(options.getSearch().getSummaryFormat(), type.getName()))) ;
            operation.setDescription(metadata.getSearch().getDescriptions().getOrDefault(type.getName(), String.format(options.getSearch().getRetrieveDescriptionFormat(), type.getName(), toParameterCase(type.getName()))));

            operation.responses(createSearchGetResponseRefs(type)) ;
            operation.addTagsItem(options.getPluralFor(type.getName())) ;

            pathItem.setGet(operation);

            return success(pathItem);
        }

        private ApiResponses createSearchGetResponseRefs(BrAPIObjectType type) {
            return addStandardApiResponses(new ApiResponses().
                addApiResponse("200", new ApiResponse().$ref(String.format("#/components/responses/%sListResponse", type.getName())))) ;
        }

        private Response<Components> generateComponents(Collection<BrAPIObjectType> primaryTypes, Collection<BrAPIClass> nonPrimaryTypes) {
            Components components = new Components() ;

            return generateSchemas(primaryTypes, nonPrimaryTypes).
                onSuccessDoWithResult(components::setSchemas).
                merge(this::generateResponses).
                onSuccessDoWithResult(components::setResponses).
                merge(this::generateParameters).
                onSuccessDoWithResult(components::setParameters).
                merge(this::generateSecuritySchemes).
                onSuccessDoWithResult(components::setSecuritySchemes).
                map(() -> success(components));
        }

        private Response<Map<String, Schema>> generateSchemas(Collection<BrAPIObjectType> primaryTypes, Collection<BrAPIClass> nonPrimaryTypes) {
            Map<String, Schema> schemas = new HashMap<>() ;

            return primaryTypes.stream().map(type -> generateSchemasForType(type).onSuccessDoWithResult(schemas::putAll)).collect(Response.toList()).
                merge(nonPrimaryTypes.stream().map(type -> createSchemaForType(type).onSuccessDoWithResult(schema -> schemas.put(type.getName(), schema))).collect(Response.toList())).
                onSuccessDo(() -> schemas.putAll(this.schemas)).
                map(() -> success(schemas)) ;
        }

        private Response<Map<String, Schema>> generateSchemasForType(BrAPIObjectType type) {

            Map<String, Schema> schemas = new HashMap<>() ;

            boolean creatingNewRequest = options.isGeneratingNewRequestFor(type.getName()) ;

            return Response.empty().
                merge(
                    () -> createSchemaForType(type, creatingNewRequest).onSuccessDoWithResult(result -> schemas.put(type.getName(), result))).
                mergeOnCondition(creatingNewRequest,
                    () -> createNewRequestSchemaForType(type).onSuccessDoWithResult(result -> schemas.put(options.getNewRequestNameFor(type.getName()), result))).
                mergeOnCondition(options.isGeneratingSearchRequestFor(type.getName()),
                    () -> createSearchRequestSchemaForType(type).onSuccessDoWithResult(result -> schemas.put(options.getSearchRequestNameFor(type.getName()), result))).
                map(() -> success(schemas)) ;
        }

        private Response<Schema> createSchemaForType(BrAPIObjectType type, boolean creatingNewRequest) {
            if (creatingNewRequest) {
                String idParameter = options.getIds().getIDParameterFor(type.getName()) ;

                return type.getProperties().stream().filter(property -> property.getName().equals(idParameter)).findAny().map(this::createProperty).
                    orElse(fail(Response.ErrorType.VALIDATION, String.format("Can not find property '%s' in type '%s'", idParameter, type.getName()))).
                    mapResultToResponse(idProperty -> success(new ObjectSchema().name(type.getName())).
                        onSuccessDoWithResult(schema -> schema.addAllOfItem(new ObjectSchema().
                            addRequiredItem(idParameter).
                            addProperty(idParameter, idProperty))));
            } else {
                return createObjectSchema(type) ;
            }
        }

        private Response<Schema> createNewRequestSchemaForType(BrAPIObjectType type) {
            String idParameter = options.getIds().getIDParameterFor(type.getName()) ;

            return createProperties(type.getProperties().stream().filter(property -> !property.getName().equals(idParameter)).toList()).mapResult(
                schema -> new ObjectSchema().properties(schema).name(type.getName()).description(type.getDescription())) ;
        }

        private Response<Schema> createSearchRequestSchemaForType(BrAPIObjectType type) {
            BrAPIClass requestSchema = this.brAPISchemas.get(String.format("%sRequest", type.getName()));

            String name = options.getSearchRequestNameFor(type.getName()) ;

            if (requestSchema == null) {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find '%sRequest' when creating '%s'", type.getName(), name)) ;
            }

            if (requestSchema instanceof BrAPIObjectType brAPIObjectType) {
                return createProperties(brAPIObjectType.getProperties().stream().toList()).mapResult(
                    schema -> new ObjectSchema().properties(schema).name(name).description(type.getDescription()));
            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("'%sRequest' must be BrAPIObjectType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
            }
        }

        private Response<Map<String, ApiResponse>> generateResponses() {
            return success(responses) ;
        }

        private Response<Map<String, Parameter>> generateParameters() {
            return success(parameters) ;
        }

        private Response<Map<String, SecurityScheme>> generateSecuritySchemes() {
            return success(securitySchemes) ;
        }

        private Response<Schema> createSchemaForType(BrAPIType type) {

            if (type instanceof BrAPIObjectType) {
                return createObjectSchema((BrAPIObjectType) type) ;
            } else if (type instanceof BrAPIOneOfType) {
                return createOneOfType((BrAPIOneOfType) type) ;
            } else if (type instanceof BrAPIArrayType) {
                return createArraySchema((BrAPIArrayType) type) ;
            } else if (type instanceof BrAPIReferenceType) {
                return createReferenceSchema((BrAPIReferenceType) type) ;
            } else if (type instanceof BrAPIEnumType) {
                return createEnumSchema((BrAPIEnumType) type) ;
            } else if (type instanceof BrAPIPrimitiveType) {
                return createScalarSchema((BrAPIPrimitiveType) type) ;
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type.getClass()));
            }
        }

        private Response<Schema> createObjectSchema(BrAPIObjectType type) {
            return createProperties(type.getProperties()).mapResult(
                schema -> new ObjectSchema().properties(schema).name(type.getName()).description(type.getDescription())) ;
        }

        private Response<Map<String, Schema>> createProperties(List<BrAPIObjectProperty> properties) {

            Map<String, Schema> schemas = new HashMap<>() ;

            return properties.stream().map(property -> createProperty(property).onSuccessDoWithResult(schema -> schemas.put(property.getName(), schema))).collect(Response.toList()).
                map(() -> success(schemas));
        }

        private Response<Schema> createProperty(BrAPIObjectProperty property) {
            return createSchemaForType(property.getType()) ;
        }

        private Response<Schema> createOneOfType(BrAPIOneOfType type) {
            return type.getPossibleTypes().stream().map(this::createSchemaForType).collect(Response.toList()).mapResult(
                schema -> new ObjectSchema().oneOf(schema).name(type.getName()).description(type.getDescription())) ;
        }

        private Response<Schema> createArraySchema(BrAPIArrayType type) {
            return createSchemaForType(type.getItems()).mapResult(schema -> new ArraySchema().items(schema)) ;
        }

        private Response<Schema> createReferenceSchema(BrAPIReferenceType type) {
            return success(new ObjectSchema().$ref(createSchemaRef(type.getName()))) ;
        }

        private String createSchemaRef(String name) {
            return String.format("#/components/schemas/%s", name) ;
        }

        private Response<Schema> createEnumSchema(BrAPIEnumType type) {
            return switch (type.getType()) {
                case "string" -> createStringEnumSchema(type) ;
                case "integer" -> createIntegerEnumSchema(type) ;
                case "number" -> createNumberEnumSchema(type) ;
                case "boolean" -> createBooleanEnumSchema(type) ;
                default -> Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s' for enum '%s'", type.getType(), type.getName()));
            };
        }

        private Response<Schema> createStringEnumSchema(BrAPIEnumType type) {
            StringSchema schema = new StringSchema() ;

            updateSchema(type, schema) ;

            try {
                schema.setEnum(type.getValues().stream().map(value -> (String) value.getValue()).toList());

                return success(schema);
            } catch (ClassCastException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cast value to String : %s", e.getMessage()));
            }
        }

        private Response<Schema> createIntegerEnumSchema(BrAPIEnumType type) {
            IntegerSchema schema = new IntegerSchema() ;

            updateSchema(type, schema) ;

            try {
                schema.setEnum(type.getValues().stream().map(value -> (Number) value.getValue()).toList());

                return success(schema);
            } catch (ClassCastException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cast value to Number : %s", e.getMessage()));
            }
        }

        private Response<Schema> createNumberEnumSchema(BrAPIEnumType type) {
            NumberSchema schema = new NumberSchema() ;

            updateSchema(type, schema) ;

            try {
                schema.setEnum(type.getValues().stream().map(value -> (BigDecimal) value.getValue()).toList());

                return success(schema);
            } catch (ClassCastException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cast value to BigDecimal : %s", e.getMessage()));
            }
        }

        private Response<Schema> createBooleanEnumSchema(BrAPIEnumType type) {
            BooleanSchema schema = new BooleanSchema() ;

            updateSchema(type, schema) ;

            try {
                schema.setEnum(type.getValues().stream().map(value -> (Boolean) value.getValue()).toList());

                return success(schema);
            } catch (ClassCastException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cast value to BigDecimal : %s", e.getMessage()));
            }
        }

        private Response<Schema> createScalarSchema(BrAPIPrimitiveType type) {
            return switch (type.getName()) {
                case "string" -> success(new StringSchema());
                case "integer" -> success(new IntegerSchema());
                case "number" -> success(new NumberSchema());
                case "boolean" -> success(new BooleanSchema());
                default ->
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown primitive type '%s'", type.getName()));
            };
        }

        private Schema updateSchema(BrAPIEnumType type, Schema schema) {
            schema.name(type.getName()) ;
            schema.description(type.getDescription()) ;

            return schema ;
        }
    }

}
