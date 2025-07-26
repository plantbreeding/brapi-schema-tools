package org.brapi.schematools.core.openapi.generator;

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
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.openapi.generator.metadata.OpenAPIGeneratorMetadata;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPITypeUtils;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

/**
 * Generates an OpenAPI Specification from a BrAPI Json Schema.
 */
@AllArgsConstructor
public class OpenAPIGenerator {

    public static final String BRAPI_COMMON = "BrAPI-Common";
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
     * for each module. See {@link OpenAPIGeneratorOptions#isSeparatingByModule}.
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
     * for each module. See {@link OpenAPIGeneratorOptions#isSeparatingByModule()}.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param componentsDirectory the path to the additional OpenAPI components needed to generate the Specification
     * @param metadata additional metadata that is used in the generation
     * @return a list of {@link OpenAPI} generated from the complete BrAPI Specification
     */
    public Response<List<OpenAPI>> generate(Path schemaDirectory, Path componentsDirectory, OpenAPIGeneratorMetadata metadata) {

        return options.validate().asResponse().merge(componentsReader.readComponents(componentsDirectory)).
                mapResultToResponse(components -> schemaReader.readDirectories(schemaDirectory).mapResultToResponse(
                    brAPISchemas -> new OpenAPIGenerator.Generator(options, metadata, brAPISchemas, components).generate()));

    }

    private static class Generator {
        private final OpenAPIGeneratorOptions options;

        private final OpenAPIGeneratorMetadata metadata;
        private final Map<String, BrAPIClass> brAPIClassMap;

        private final Map<String, Parameter> parameters;
        private final Map<String, ApiResponse> responses;
        private final Map<String, Schema> schemas;
        private final Map<String, SecurityScheme> securitySchemes;

        private final Set<String> referencedSchemas ;

        public Generator(OpenAPIGeneratorOptions options, OpenAPIGeneratorMetadata metadata, List<BrAPIClass> brAPIClasses, Components components) {
            this.options = options;
            this.metadata = metadata ;
            // cache all the BrAPI classes
            this.brAPIClassMap = brAPIClasses.stream().collect(Collectors.toMap(BrAPIClass::getName, Function.identity()));

            // Cache all the generic components (TODO generate these instead of reading from a directory)
            if (components.getParameters() != null) {
                this.parameters = new HashMap<>(components.getParameters());
            } else {
                this.parameters = new HashMap<>();
            }

            if (components.getResponses() != null) {
                this.responses = new HashMap<>(components.getResponses());
            } else {
                this.responses = new HashMap<>();
            }

            if (components.getSchemas() != null) {
                this.schemas = new HashMap<>(components.getSchemas());
            } else {
                this.schemas = new HashMap<>();
            }

            if (components.getSecuritySchemes() != null) {
                this.securitySchemes = new HashMap<>(components.getSecuritySchemes());
            } else {
                this.securitySchemes = new HashMap<>();
            }

            // maintain a list of schemas that have been referenced elsewhere, but not yet generated
            this.referencedSchemas = new TreeSet<>() ;
        }

        public Response<List<OpenAPI>> generate() {
            Collection<BrAPIClass> values = brAPIClassMap.values();

            if (options.isSeparatingByModule()) {
                Map<String, List<BrAPIClass>> classesByModule = values.stream().
                    filter(type -> Objects.nonNull(type.getModule())).
                    collect(Collectors.groupingBy(BrAPIClass::getModule, toList()));
                List<BrAPIClass> commonClasses = classesByModule.remove(BRAPI_COMMON);

                return classesByModule.entrySet().stream().
                    map(entry -> {
                        entry.getValue().addAll(commonClasses);
                        return entry;
                    }).
                    map(entry -> generate(entry.getKey(), entry.getValue())).
                    collect(Response.toList());
            } else {
                return generate("BrAPI", values.stream().filter(type -> Objects.nonNull(type.getModule())).toList()).
                    mapResult(Collections::singletonList);
            }
        }

        private Response<OpenAPI> generate(String title, Collection<BrAPIClass> classes) {

            OpenAPI openAPI = new OpenAPI();

            Info info = new Info();

            info.setTitle(title == null && metadata.getTitle() != null ? metadata.getTitle() : title);
            info.setVersion(metadata.getVersion() != null ? metadata.getVersion() : "0.0.0");

            openAPI.setInfo(info);

            // TODO merge in openAPIMetadata

            // get a list of non-primary classes (those with 'primaryModel=false' in their BrAPI metadata or has no metadata)
            List<BrAPIClass> nonPrimaryClasses = new ArrayList<>(classes.stream().filter(BrAPITypeUtils::isNonPrimaryModel).toList());

            // get a list of primary classes (those with 'primaryModel=true' in their BrAPI metadata) sorted by name
            List<BrAPIObjectType> primaryClasses = classes.stream().
                filter(type -> type instanceof BrAPIObjectType).
                filter(BrAPITypeUtils::isPrimaryModel).
                sorted(Comparator.comparing(BrAPIType::getName)).
                map(type -> (BrAPIObjectType)type).
                toList();

            return Response.empty()
                .mergeOnCondition(options.isGeneratingEndpoint(), // these are GET, POST or PUT endpoints with the pattern /<entity-plural> e.g. /locations
                    () -> primaryClasses.stream()
                        .filter(options::isGeneratingEndpointFor)
                        .map(type -> generatePathItem(type)
                            .onSuccessDoWithResult(
                                pathItem -> {
                                    openAPI.path(createPathItemName(type), pathItem);
                                }))
                        .collect(Response.toList()))
                .mergeOnCondition(options.isGeneratingEndpointWithId(),  // these are GET, PUT and DELETE endpoints with the pattern /<entity-plural>/{<entity-id>} e.g. /locations/{locationDbId}
                    () -> primaryClasses.stream()
                        .filter(options::isGeneratingEndpointNameWithIdFor)
                        .map(type -> createPathItemsWithId(openAPI, type))
                        .collect(Response.toList()))
                .mergeOnCondition(options.getSearch().isGenerating(),  // this is a POST endpoint with the pattern /search/<entity-plural> e.g. /search/locations
                    () -> primaryClasses.stream()
                        .filter(type -> options.getSearch().isGeneratingFor(type))
                        .map(type -> createSearchPathItem(type)
                            .onSuccessDoWithResult(
                                pathItem -> {
                                    openAPI.path(createSearchPathItemName(type), pathItem);
                                }))
                        .collect(Response.toList()))
                .mergeOnCondition(options.getSearch().isGenerating(), // this is a GET endpoint are endpoints with the pattern /search/<entity-plural>/{searchResultsDbId} e.g. /search/locations/{searchResultsDbId}
                    () -> primaryClasses.stream().
                        filter(type -> options.getSearch().isGeneratingFor(type)).
                        map(type -> createSearchPathItemWithId(type)
                            .onSuccessDoWithResult(
                                pathItem -> {
                                    openAPI.path(createSearchPathItemWithIdName(type), pathItem);
                                }))
                        .collect(Response.toList()))
                .merge(() -> processReferencedSchemas(classes)
                    .onSuccessDoWithResult(nonPrimaryClasses::addAll))
                .merge(() -> generateComponents(primaryClasses, nonPrimaryClasses).onSuccessDoWithResult(openAPI::components))
                .map(() -> success(openAPI));
        }

        private Response<List<BrAPIClass>> processReferencedSchemas(Collection<BrAPIClass> classes) {

            // remove any classes that will be created elsewhere
            classes.stream().map(BrAPIClass::getName).toList().forEach(referencedSchemas::remove) ;
            schemas.keySet().forEach(referencedSchemas::remove) ;
            securitySchemes.keySet().forEach(referencedSchemas::remove) ;

            return referencedSchemas.stream().map(this::findReferencedClass).collect(Response.toList()) ;
        }

        private Response<BrAPIClass> findReferencedClass(String typeName) {
            BrAPIClass brAPISchema = brAPIClassMap.get(typeName);

            if (brAPISchema != null) {
                return success(brAPISchema) ;
            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find referenced type %s", typeName)) ;
            }
        }

        private String createPathItemName(BrAPIObjectType type) {
            return options.getPathItemNameFor(type) ;
        }

        private Response<PathItem> generatePathItem(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            return Response.empty()
                .mergeOnCondition(options.getListGet().isGeneratingFor(type), () -> generateListGetOperation(type).onSuccessDoWithResult(pathItem::setGet))
                .mergeOnCondition(options.getPost().isGeneratingFor(type), () -> generatePostOperation(type).onSuccessDoWithResult(pathItem::setPost))
                .mergeOnCondition(options.getPut().isGeneratingEndpointFor(type), () -> generateMultiplePutOperation(type).onSuccessDoWithResult(pathItem::setPut))
                .merge(() -> generateListResponse(type))
                .map(() -> success(pathItem));
        }

        private String createPathItemWithIdName(BrAPIObjectType type) {
            return options.getPathItemWithIdNameFor(type) ;
        }

        private String createSubPathItemName(String pathItemName, BrAPIObjectProperty property) {
            return options.getSubPathItemNameFor(pathItemName, property) ;
        }

        private Response<OpenAPI> createPathItemsWithId(OpenAPI openAPI, BrAPIObjectType type) {

            String pathItemName = createPathItemWithIdName(type);

            return createPathItemWithId(type)
                .onSuccessDoWithResult(pathItem -> openAPI.path(pathItemName, pathItem))
                .merge(type.getProperties().stream()
                    .filter(property -> options.isGeneratingSubPathFor(type, property))
                    .map(property -> createSubPathItemWithId(type, property)
                        .onSuccessDoWithResult(subPathItem -> openAPI.path(createSubPathItemName(pathItemName, property), subPathItem)))
                    .collect(Response.toList()))
                .map(() -> success(openAPI));
        }

        private Response<PathItem> createPathItemWithId(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            return Response.empty()
                .mergeOnCondition(options.getSingleGet().isGeneratingFor(type), () -> generateSingleGetOperation(type).onSuccessDoWithResult(pathItem::setGet))
                .mergeOnCondition(options.getPut().isGeneratingEndpointNameWithIdFor(type), () -> generateSinglePutOperation(type).onSuccessDoWithResult(pathItem::setPut))
                .mergeOnCondition(options.getDelete().isGeneratingFor(type), () -> generateDeleteOperation(type).onSuccessDoWithResult(pathItem::setDelete))
                .merge(() -> generateSingleResponse(type))
                .map(() -> success(pathItem));
        }

        private Response<PathItem> createSubPathItemWithId(BrAPIObjectType parentType, BrAPIObjectProperty property) {
            PathItem pathItem = new PathItem();

            BrAPIType type = dereferenceType(property.getType()) ;

            if (type instanceof BrAPIObjectType brAPIObjectType) {
                return generateSubPathSingleGetOperation(parentType, brAPIObjectType)
                    .onSuccessDoWithResult(pathItem::setGet)
                    .map(() -> success(pathItem));

            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                int dimension = 1 ;
                BrAPIType itemType = dereferenceType(brAPIArrayType.getItems());

                while (itemType instanceof BrAPIArrayType brAPIArrayItemType) {
                    itemType = dereferenceType(brAPIArrayItemType.getItems());
                    ++dimension ;
                }

                if (itemType instanceof BrAPIObjectType brAPIObjectType) {
                    if (dimension > 1) {
                        return fail(Response.ErrorType.VALIDATION, String.format("Sub-path not available for property '%s' on type '%s' with type '%s', dimension > 1", property.getName(), parentType.getName(), type.getName()));
                    }

                    return generateSubPathListGetOperation(parentType, brAPIObjectType)
                        .onSuccessDoWithResult(pathItem::setGet)
                        .map(() -> success(pathItem));
                } else {
                    return fail(Response.ErrorType.VALIDATION, String.format("Sub-path not available for property '%s' on type '%s' with type '%s'", property.getName(), parentType.getName(), type.getName())) ;
                }
            }

            return fail(Response.ErrorType.VALIDATION, String.format("Sub-path not available for property '%s' on type '%s' with type '%s'", property.getName(), parentType.getName(), type.getName())) ;
        }

        private Response<ApiResponse> generateSingleResponse(BrAPIObjectType type) {

            String name = options.getSingleResponseNameFor(type);

            ApiResponse apiResponse = new ApiResponse().description("OK").content(
                new Content().addMediaType("application/json",
                    new MediaType().schema(
                        new ObjectSchema().title(name).
                            addProperty("@context", new ObjectSchema().$ref(createSchemaRef("Context"))).
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
            String name = options.getListResponseNameFor(type);

            if (responses.containsKey(name)) {
                return success(responses.get(name));
            }

            ApiResponse apiResponse = new ApiResponse().description("OK").content(
                new Content().addMediaType("application/json",
                    new MediaType().schema(
                        new ObjectSchema().title(name).
                            addProperty("@context", new ObjectSchema().$ref(createSchemaRef("Context"))).
                            addProperty("metadata", new ObjectSchema().$ref(createSchemaRef("metadata"))).
                            addProperty("result", new ObjectSchema().
                                addProperty("data", new ArraySchema().items(new Schema().$ref(createSchemaRef(type.getName())))).
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

            operation.setSummary(metadata.getListGet().getSummaryOrDefault(type.getName(), options.getListGet().getSummaryFor(type))) ;
            operation.setDescription(metadata.getListGet().getDescriptionOrDefault(type.getName(), options.getListGet().getDescriptionFor(type))) ;

            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return createListGetParametersFor(type).
                onSuccessDoWithResult(operation::parameters).
                map(() -> success(operation));
        }

        private Response<List<Parameter>> createListGetParametersFor(BrAPIObjectType type) {

            List<Parameter> parameters = new ArrayList<>();

            if (type.getProperties().stream().anyMatch(property -> property.getName().equals("externalReferences"))) {
                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceID"));
                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceId")); // TODO depreciated, remove?
                parameters.add(new Parameter().$ref("#/components/parameters/externalReferenceSource"));
            }

            if (options.getListGet().isPagedFor(type)) {
                parameters.add(new Parameter().$ref("#/components/parameters/page"));
                parameters.add(new Parameter().$ref("#/components/parameters/pageSize"));
                parameters.add(new Parameter().$ref("#/components/parameters/authorizationHeader"));
            }

            if (options.getListGet().hasInputFor(type)) {
                BrAPIClass requestClass = this.brAPIClassMap.get(String.format("%sRequest", type.getName()));

                if (requestClass == null) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Can not find '%sRequest' to create properties for list get endpoint for '%s'", type.getName(), createPathItemName(type))) ;
                }

                if (requestClass instanceof BrAPIObjectType brAPIObjectType) {
                    return brAPIObjectType.getProperties().stream()
                        .filter(property -> options.getListGet().isUsingPropertyFromRequestFor(type, property))
                        .map(this::createListGetParameter)
                        .collect(Response.toList())
                        .onSuccessDoWithResult(result -> parameters.addAll(0, result))
                        .map(() -> success(parameters));
                } else {
                    return fail(Response.ErrorType.VALIDATION, String.format("'%sRequest' must be BrAPIObjectType but was '%s'", type.getName(), type.getClass().getSimpleName())) ;
                }
            }

            return success(parameters) ;
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

            operation.setSummary(metadata.getPost().getSummaryOrDefault(type.getName(), options.getPost().getSummaryFor(type))) ;
            operation.setDescription(metadata.getPost().getDescriptionOrDefault(type.getName(), options.getPost().getDescriptionFor(type))) ;

            operation.addParametersItem(new Parameter().$ref("#/components/parameters/authorizationHeader")) ;

            String requestBodyName = options.isGeneratingNewRequestFor(type) ? options.getNewRequestNameFor(type) : type.getName() ;

            operation.requestBody(
                new RequestBody().content(
                    new Content().addMediaType("application/json",
                        new MediaType().schema(
                            new ArraySchema().$ref(String.format("#/components/schemas/%s", requestBodyName))))));

            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateSingleGetOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getSingleGet().getSummaryOrDefault(type.getName(), options.getSingleGet().getSummaryFor(type))) ;
            operation.setDescription(metadata.getSingleGet().getDescriptionOrDefault(type.getName(), options.getSingleGet().getDescriptionFor(type))) ;

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateSinglePutOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getPut().getSummaryOrDefault(type.getName(), options.getPut().getSummaryFor(type))) ;
            operation.setDescription(metadata.getPut().getDescriptionOrDefault(type.getName(), options.getPut().getDescriptionFor(type))) ;

            operation.addParametersItem(new Parameter().$ref("#/components/parameters/authorizationHeader")) ;

            String requestBodyName = options.isGeneratingNewRequestFor(type) ? options.getNewRequestNameFor(type) : type.getName() ;

            operation.requestBody(
                new RequestBody().content(
                    new Content().addMediaType("application/json",
                        new MediaType().schema(
                            new Schema().$ref(String.format("#/components/schemas/%s", requestBodyName))))));


            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateMultiplePutOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getPut().getSummaryOrDefault(type.getName(), options.getPut().getSummaryFor(type))) ;
            operation.setDescription(metadata.getPut().getDescriptionOrDefault(type.getName(), options.getPut().getDescriptionFor(type))) ;

            operation.addParametersItem(new Parameter().$ref("#/components/parameters/authorizationHeader")) ;

            String requestBodyName = options.isGeneratingNewRequestFor(type) ? options.getNewRequestNameFor(type) : type.getName() ;

            operation.requestBody(
                new RequestBody().content(
                    new Content().addMediaType("application/json",
                        new MediaType().schema(
                            new ArraySchema().$ref(String.format("#/components/schemas/%s", requestBodyName))))));


            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateDeleteOperation(BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getDelete().getSummaryOrDefault(type.getName(), options.getDelete().getSummaryFor(type))) ;
            operation.setDescription(metadata.getDelete().getDescriptionOrDefault(type.getName(), options.getDelete().getDescriptionFor(type))) ;

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateSubPathSingleGetOperation(BrAPIObjectType parentType, BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getSingleGet().getSummaryOrDefault(type.getName(), options.getSingleGet().getSummaryFor(type))) ;
            operation.setDescription(metadata.getSingleGet().getDescriptionOrDefault(type.getName(), options.getSingleGet().getDescriptionFor(type))) ;

            operation.responses(createSingleApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return success(operation);
        }

        private Response<Operation> generateSubPathListGetOperation(BrAPIObjectType parentType, BrAPIObjectType type) {
            Operation operation = new Operation();

            operation.setSummary(metadata.getListGet().getSummaryOrDefault(type.getName(), options.getListGet().getSummaryFor(type))) ;
            operation.setDescription(metadata.getListGet().getDescriptionOrDefault(type.getName(), options.getListGet().getDescriptionFor(type))) ;

            operation.responses(createListApiResponses(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            return createListGetParametersFor(type).
                onSuccessDoWithResult(operation::parameters).
                map(() -> success(operation));
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

        private String createSearchPathItemName(BrAPIObjectType type) {
            return String.format("/search%s", options.getPathItemNameFor(type));
        }

        private String createSearchPathItemWithIdName(BrAPIObjectType type) {
            return String.format("/search%s/{%s}", options.getPathItemNameFor(type), options.getSearch().getSearchIdFieldName());
        }

        public Response<PathItem> createSearchPathItem(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            Operation operation = new Operation();

            operation.setSummary(metadata.getSearch().getSummaryOrDefault(type.getName(), options.getSearch().getSummaryFor(type))) ;
            operation.setDescription(metadata.getSearch().getDescriptionOrDefault(type.getName(), options.getSearch().getSubmitDescriptionFormat(type)));

            operation.responses(createSearchPostResponseRefs(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            pathItem.setPost(operation);

            return generateListResponse(type).map(() -> success(pathItem));
        }

        private ApiResponses createSearchPostResponseRefs(BrAPIObjectType type) {
            return addStandardApiResponses(new ApiResponses().
                addApiResponse("200", new ApiResponse().$ref(String.format("#/components/responses/%sListResponse", type.getName()))).
                addApiResponse("202", new ApiResponse().$ref("#/components/responses/202AcceptedSearchResponse"))) ;
        }

        public Response<PathItem> createSearchPathItemWithId(BrAPIObjectType type) {
            PathItem pathItem = new PathItem();

            Operation operation = new Operation();

            operation.setSummary(metadata.getSearch().getSummaryOrDefault(type.getName(), options.getSearch().getSubmitDescriptionFormat(type)));
            operation.setDescription(metadata.getSearch().getDescriptionOrDefault(type.getName(), options.getSearch().getRetrieveDescriptionFormat(type)));

            operation.responses(createSearchGetResponseRefs(type)) ;
            operation.addTagsItem(options.getTagFor(type)) ;

            pathItem.setGet(operation);

            return generateListResponse(type).map(() -> success(pathItem));
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
            Map<String, Schema> schemas = new TreeMap<>() ;

            return primaryTypes.stream().map(type -> generateSchemasForType(type).onSuccessDoWithResult(schemas::putAll)).collect(Response.toList()).
                merge(nonPrimaryTypes.stream().map(type -> createSchemaForType(type).onSuccessDoWithResult(schema -> schemas.put(type.getName(), schema))).collect(Response.toList())).
                onSuccessDo(() -> schemas.putAll(this.schemas)).
                map(() -> success(schemas)) ;
        }

        private Response<Map<String, Schema>> generateSchemasForType(BrAPIObjectType type) {

            Map<String, Schema> schemas = new TreeMap<>() ;

            boolean creatingNewRequest = options.isGeneratingNewRequestFor(type) ;

            return Response.empty().
                merge(
                    () -> createSchemaForType(type, creatingNewRequest).onSuccessDoWithResult(result -> schemas.put(type.getName(), result))).
                mergeOnCondition(creatingNewRequest,
                    () -> createNewRequestSchemaForType(type).onSuccessDoWithResult(result -> schemas.put(options.getNewRequestNameFor(type), result))).
                mergeOnCondition(options.getSearch().isGeneratingFor(type),
                    () -> createSearchRequestSchemaForType(type).onSuccessDoWithResult(result -> schemas.put(options.getSearchRequestNameFor(type), result))).
                map(() -> success(schemas)) ;
        }

        /**
         * Creates the base schema for a type
         * @param type The BrAPI Object type to start from
         * @param creatingNewRequest {@code true} if there will be a separate new request schema created
         * @return the base schema for a type
         */
        private Response<Schema> createSchemaForType(BrAPIObjectType type, boolean creatingNewRequest) {
            if (creatingNewRequest) {
                String idParameter = options.getProperties().getIdPropertyNameFor(type) ;

                if (type.getProperties().stream().noneMatch(property -> property.getName().equals(idParameter))) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Can not find property '%s' in type '%s'", idParameter, type.getName())) ;
                }

                return createObjectSchema(type, type.getProperties().stream().filter(property -> !property.getName().equals(idParameter)).toList())
                    .onSuccessDoWithResult(result-> result.addRequiredItem(idParameter)) ;
            } else {
                return createObjectSchema(type) ;
            }
        }

        /**
         * Creates the New Request schema for a type. Makes the id property required,
         * of fails if not present
         * @param type The BrAPI Object type to start from
         * @return the New Request schema for a type
         */
        private Response<Schema> createNewRequestSchemaForType(BrAPIObjectType type) {
            String idParameter = options.getProperties().getIdPropertyNameFor(type) ;

            if (type.getProperties().stream().noneMatch(property -> property.getName().equals(idParameter))) {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find property '%s' in type '%s'", idParameter, type.getName())) ;
            }

            return createObjectSchema(type).onSuccessDoWithResult(result-> result.addRequiredItem(idParameter)) ;
        }

        private Response<Schema> createSearchRequestSchemaForType(BrAPIObjectType type) {
            BrAPIClass requestSchema = this.brAPIClassMap.get(String.format("%sRequest", type.getName()));

            String name = options.getSearchRequestNameFor(type) ;

            if (requestSchema == null) {
                return fail(Response.ErrorType.VALIDATION, String.format("Can not find '%sRequest' when creating '%s'", type.getName(), name)) ;
            }

            if (requestSchema instanceof BrAPIObjectType brAPIObjectType) {
                Schema objectSchema = new ObjectSchema()
                    .name(type.getName())
                    .description(type.getDescription()) ;

                return createProperties(objectSchema, type, brAPIObjectType.getProperties().stream().toList())
                    .mapResult(properties -> objectSchema.properties(properties));
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
            return createObjectSchema(type, type.getProperties()) ;
        }

        private Response<Schema> createObjectSchema(BrAPIObjectType type, List<BrAPIObjectProperty> properties) {
            Schema objectSchema = new ObjectSchema()
                .name(type.getName())
                .description(type.getDescription()) ;

            return createProperties(objectSchema, type, properties)
                .mapResult(schema -> objectSchema.properties(schema)) ;
        }

        private Response<Map<String, Schema>> createProperties(Schema objectSchema, BrAPIObjectType parentType, List<BrAPIObjectProperty> properties) {

            Map<String, Schema> schemas = new TreeMap<>() ;

            return properties.stream().map(property -> createProperty(objectSchema, parentType, property).onSuccessDoWithResult(schemas::putAll)).collect(Response.toList()).
                map(() -> success(schemas));
        }

        private Response<Map<String, Schema>> createProperty(Schema objectSchema, BrAPIObjectType parentType, BrAPIObjectProperty property) {
            LinkType linkType = options.getProperties().getLinkTypeFor(parentType, property);

            if (LinkType.SUB_PATH.equals(linkType) || LinkType.NONE.equals(linkType)) {
                return success(Collections.emptyMap()) ;
            }

            BrAPIType type = dereferenceType(property.getType()) ;

            if (type instanceof BrAPIPrimitiveType || type instanceof BrAPIEnumType || type instanceof BrAPIOneOfType)  {
                return createEmbeddedProperty(objectSchema, property, type) ;
            } else if (type instanceof BrAPIObjectType brAPIObjectType) {
                BrAPIRelationshipType relationshipType = property.getRelationshipType() != null ? property.getRelationshipType() : BrAPIRelationshipType.ONE_TO_ONE;

                return switch (relationshipType) {
                    case ONE_TO_ONE, MANY_TO_ONE -> LinkType.ID.equals(linkType) ?
                        createLinkedProperty(objectSchema, property, brAPIObjectType) :
                        createEmbeddedProperty(objectSchema, property, brAPIObjectType) ;
                    case ONE_TO_MANY, MANY_TO_MANY  -> fail(Response.ErrorType.VALIDATION, String.format("Property '%s' has relationshipType '%s', referenced type '%s' is an object",
                        property.getName(), relationshipType, brAPIObjectType.getName()));
                } ;

            } else if (type instanceof BrAPIArrayType brAPIArrayType) {
                BrAPIRelationshipType relationshipType = property.getRelationshipType() != null ? property.getRelationshipType() : BrAPIRelationshipType.ONE_TO_MANY;

                BrAPIType itemType = dereferenceType(brAPIArrayType.getItems());

                return switch (relationshipType) {
                    case ONE_TO_ONE, MANY_TO_ONE -> fail(Response.ErrorType.VALIDATION, String.format("Property '%s' has relationshipType '%s', referenced type '%s' is an array",
                        property.getName(), relationshipType, brAPIArrayType.getName()));
                    case ONE_TO_MANY, MANY_TO_MANY -> LinkType.ID.equals(linkType) ?
                        createArrayOfIdsProperty(objectSchema, property, itemType) :
                        createArrayProperty(objectSchema, property, brAPIArrayType) ;
                } ;
            }
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unsupported type '%s' for property '%s'", type.getClass(), property.getName()));
        }

        private Response<Map<String, Schema>> createEmbeddedProperty(Schema objectSchema, BrAPIObjectProperty property, BrAPIType type) {
            return createSchemaForProperty(property, type)
                .mapResult(schema -> Collections.singletonMap(property.getName(), schema))
                .onSuccessDoOnCondition(property.isRequired(), () -> objectSchema.addRequiredItem(property.getName())) ;
        }

        private Response<Map<String, Schema>> createLinkedProperty(Schema objectSchema, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {

            List<BrAPIObjectProperty> linkProperties = options.getProperties().getLinkPropertiesFor(brAPIObjectType) ;

            if (property.isRequired()) {
                for (BrAPIObjectProperty linkProperty : linkProperties) {
                    if (linkProperty.isRequired()) {
                        objectSchema.addRequiredItem(linkProperty.getName()) ;
                    }
                }
            }

            if (linkProperties.isEmpty()) {
                return createSchemaForProperty(property, brAPIObjectType).mapResult(schema -> Collections.singletonMap(property.getName(), schema));
            } else {
                return createLinkingProperties(linkProperties) ;
            }
        }

        private Response<Map<String, Schema>> createArrayOfIdsProperty(Schema objectSchema, BrAPIObjectProperty property, BrAPIType itemType) {
            return options.getProperties().getIdPropertyFor(itemType)
                .mapResult(BrAPIObjectProperty::getType)
                .mapResultToResponse(this::createArraySchemaForType)
                .mapResult(arraySchema -> Collections.singletonMap(options.getProperties().getIdsPropertyNameFor(property), arraySchema))
                .onSuccessDoOnCondition(property.isRequired(), () -> objectSchema.addRequiredItem(options.getProperties().getIdsPropertyNameFor(property)))
                .or(() -> success(Collections.emptyMap()));
        }

        private Response<Map<String, Schema>> createArrayProperty(Schema objectSchema, BrAPIObjectProperty property, BrAPIArrayType brAPIArrayType) {
            return createArraySchema(brAPIArrayType)
                .mapResult(schema -> Collections.singletonMap(property.getName(), schema))
                .onSuccessDoOnCondition(property.isRequired(), () -> objectSchema.addRequiredItem(options.getProperties().getIdsPropertyNameFor(property)))
                .or(() -> success(Collections.emptyMap()));
        }

        private BrAPIType dereferenceType(BrAPIType type) {
            if (type instanceof BrAPIReferenceType) {
                return brAPIClassMap.get(type.getName()) ;
            } else {
                return type ;
            }
        }

        private Response<Map<String, Schema>> createLinkingProperties(List<BrAPIObjectProperty> linkProperties) {
            Map<String, Schema> schemas =  new HashMap<>() ;

            return linkProperties.stream().map(linkProperty -> createSchemaForType(linkProperty.getType())
                    .onSuccessDoWithResult(schema -> schemas.put(linkProperty.getName(), schema)))
                .collect(Response.toList())
                .merge(() -> success(schemas));
        }

        private Response<Schema> createSchemaForProperty(BrAPIObjectProperty property, BrAPIType type) {
            if (property.getType() instanceof BrAPIReferenceType) {
                return createSchemaForType(property.getType()) ;
            } else {
                return createSchemaForType(type) ;
            }
        }

        private Response<Schema> createOneOfType(BrAPIOneOfType type) {
            return type.getPossibleTypes().stream().map(this::createSchemaForType).collect(Response.toList()).mapResult(
                schema -> new Schema().oneOf(schema).name(type.getName()).description(type.getDescription())) ;
        }

        private Response<Schema> createArraySchema(BrAPIArrayType type) {
            return createSchemaForType(type.getItems()).mapResult(schema -> new ArraySchema().items(schema)) ;
        }

        private Response<Schema> createArraySchemaForType(BrAPIType type) {
            return createSchemaForType(type).mapResult(schema -> new ArraySchema().items(schema)) ;
        }

        private Response<Schema> createReferenceSchema(BrAPIReferenceType type) {
            return success(new Schema().$ref(createSchemaRef(type.getName()))) ;
        }

        private String createSchemaRef(String name) {
            referencedSchemas.add(name) ;
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
