package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.atlassian.oai.validator.report.ValidationReport;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.analyse.authorization.AuthorizationProvider;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;


/**
 * Analyses BrAPI endpoints against an OpenAPI Specification
 */
@Slf4j
public class OpenAPISpecificationAnalyserFactory {

    private static final int SPECIAL_CASE_ENDPOINTS_INDEX = 0;
    private static final int LIST_ENTITY_INDEX = 10;
    private static final int GET_ENTITY_INDEX = 20;
    private static final int SEARCH_INDEX = 30;
    private static final int SEARCH_RESULTS_INDEX = 40;

    private static final int TABLE_INDEX = 50;
    private static final int CREATE_ENTITY_INDEX = 60;
    private static final int UPDATE_ENTITY_INDEX = 70;
    private static final int DELETE_ENTITY_INDEX = 80;

    private static final Pattern REF_PATTERN = Pattern.compile("#/components/schemas/(\\w+)");

    private final String baseURL;
    private final HttpClient client;
    private final AuthorizationProvider authorizationProvider;
    private final AnalysisOptions options;

    private final ObjectMapper objectMapper;

    private final String COMMON_CROP_NAMES = "commoncropnames";
    private final String COMMON_CROP_NAMES_ENDPOINT = "/" + COMMON_CROP_NAMES;

    private final List<String> SPECIAL_CASE_ENDPOINTS = List.of(COMMON_CROP_NAMES_ENDPOINT);

    /**
     * Create an Analyser Factory
     *
     * @param baseURL               the base URl for the BrAPI server
     * @param client                the HTTP client to use for the execution of requests
     * @param authorizationProvider the authorization provider need for authorization
     */
    public OpenAPISpecificationAnalyserFactory(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider) {
        this(baseURL, client, authorizationProvider, AnalysisOptions.load());
    }

    /**
     * Create an Analyser
     *
     * @param baseURL               the base URl for the BrAPI server
     * @param client                the HTTP client to use for the execution of requests
     * @param authorizationProvider the authorization provider need for authorization
     * @param options               analysis options ;
     */
    public OpenAPISpecificationAnalyserFactory(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider, AnalysisOptions options) {
        this.baseURL = baseURL;
        this.client = client;
        this.authorizationProvider = authorizationProvider;
        this.options = options;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a new analyser and analyses all the endpoints in the specification.
     * Shortcut for {@link AnalysisOptions#validate()}, {@link Analyser#analyseSpecial()} and {@link Analyser#analyseAll()}.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse()
            .map(() -> Stream.of(analyser.analyseSpecial(), analyser.analyseAll()).collect(Response.mergeLists()));
    }

    /**
     * Creates a new analyser and analyses the endpoints for specific entities in the specification.
     * Shortcut for {@link AnalysisOptions#validate()}, {@link Analyser#analyseSpecial()} and {@link Analyser#analyseEntities(List)}.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @param entityNames   a list of entities to be analysed
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification, List<String> entityNames) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse()
            .map(() -> Stream.of(analyser.analyseSpecial(), analyser.analyseEntities(entityNames)).collect(Response.mergeLists()));
    }

    /**
     * Creates a new analyser and validates the options.
     * Shortcut for {@link AnalysisOptions#validate()}
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<Validation> validate(String specification) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse() ;
    }

    /**
     * Creates a new analyser for a specification. Used for fine control over the analysis.
     * It is recommended to use the factory directly {@link OpenAPISpecificationAnalyserFactory#analyse(String)} or
     * {@link OpenAPISpecificationAnalyserFactory#analyse(String, List)} which handles option validation
     * and the pre-processing steps, like calling {@link Analyser#analyseSpecial()}
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Analyser analyser(String specification) {
        return new Analyser(specification) ;
    }

    /**
     * Analyser provides direct access to the analysis functions.
     */
    public class Analyser {

        private final OpenAPI openAPI;
        private final OpenApiInteractionValidator validator;
        private final Map<String, APIRequest> requests = new HashMap<>();

        private final Set<APIRequest> specialRequests = new TreeSet<>(Comparator.comparingInt(APIRequest::getIndex));

        private final Map<String, List<APIRequest>> requestsByEntity ;

        private final List<Endpoint> unmatchedEndpoints = new ArrayList<>();

        private final List<Endpoint> skippedEndpoints = new ArrayList<>();
        private final Map<String, VariableValue> variableValues = new HashMap<>();

        private final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(\\w+)(?:/)?(\\w+)?/\\{(\\w+)\\}"); // 3 groups
        private final Pattern ENTITIES_PATH_PATTERN = Pattern.compile(	"/(\\w+)(?:/)?(\\w+)?(?:/)?(\\w+)?"); // 3 groups, ignore last
        private final Pattern SEARCH_PATH_PATTERN = Pattern.compile("/search/(\\w+)(/attributes|/attributevalues)?"); // 2 groups
        private final Pattern SEARCH_RESULTS_PATH_PATTERN = Pattern.compile("/search/(\\w+)(/attributes|/attributevalues)?/\\{(\\w+)\\}"); // 3 groups, ignore last
        private final Pattern TABLE_PATH_PATTERN = Pattern.compile("/(\\w+)/table"); // 1 group
        private final Pattern ENTITY_SUB_PATH_PATTERN = Pattern.compile("/(\\w+)(?:/)?(\\w+)?/\\{(\\w+)\\}/(\\w+)"); // 3 groups, ignore 3rd
        private final List<PatternMatcher> PATH_PATTERN_MATCHERS = Arrays.asList(
            new PatternMatcher(SEARCH_PATH_PATTERN, this::cacheSearchPath),
            new PatternMatcher(SEARCH_RESULTS_PATH_PATTERN, this::cacheSearchResultPath),
            new PatternMatcher(ENTITIES_PATH_PATTERN, this::cacheEntitiesPath),
            new PatternMatcher(ENTITY_PATH_PATTERN, this::cacheEntityPath),
            new PatternMatcher(TABLE_PATH_PATTERN, this::cacheTablePath),
            new PatternMatcher(ENTITY_SUB_PATH_PATTERN, this::cacheSubPath)) ;

        private final Pattern PARAMETER_PATTERN = Pattern.compile(	"\\{(\\w+)\\}") ;
        private final Response<List<APIRequest>> errors;

        /**
         * Create an Analysis based on a OpenAPI specification
         * @param specification on a OpenAPI specification
         */
        private Analyser(String specification) {
            ParseOptions parseOptions = new ParseOptions();

            parseOptions.setResolve(true);

            SwaggerParseResult result = new OpenAPIParser().readContents(specification, null, parseOptions);

            validator =
                OpenApiInteractionValidator.createForInlineApiSpecification(specification).build();

            openAPI = result.getOpenAPI();

            errors = openAPI.getPaths().entrySet().stream()
                .map(this::cacheRequest)
                .collect(Response.toList())
                .onFailDoWithResponse(Response::getAllErrors) ;

            requestsByEntity = new TreeMap<>(requests.values().stream().collect(Collectors.groupingBy(APIRequest::getEntityName)));
        }

        /**
         * Get the list of entities available on the server
         * @return list of entities available on the server
         */
        public List<String> getEntityNames() {
            return new ArrayList<>(requestsByEntity.keySet())  ;
        }

        /**
         * Gets a list of endpoints that are not tested under any situation.
         *
         * @return a list of endpoints that are not tested under any situation
         */
        public List<Endpoint> getUnmatchedEndpoints() {
            return unmatchedEndpoints;
        }

        /**
         * Gets a list of endpoints that will not be tested under due to the current option settings.
         *
         * @return a list of endpoints that will not be tested under due to the current option settings
         */
        public List<Endpoint> getSkippedEndpoints() {
            return skippedEndpoints;
        }

        /**
         * Analyse all the endpoints in the specification.
         * Does not call {@link AnalysisOptions#validate()} or {@link #analyseSpecial()}.
         *
         * @return A response containing a list of AnalysisReports or failure explaining why it failed.
         */
        public Response<List<AnalysisReport>> analyseAll() {
            return requestsByEntity.entrySet().stream()
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        /**
         * Analyse the endpoints for specific entities in the specification.
         * Does not call {@link AnalysisOptions#validate()} or {@link #analyseSpecial()}.
         *
         * @param entityNames a list of entities to be analysed
         * @return A response containing a list of AnalysisReports or failure explaining why it failed.
         */
        public Response<List<AnalysisReport>> analyseEntities(List<String> entityNames) {
            return requestsByEntity.entrySet().stream()
                .filter(entry -> entityNames.contains(entry.getKey()))
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        /**
         * Analyse the endpoints for specific entity in the specification. Does not call {@link #analyseSpecial()}.
         *
         * @param entityName an entity to be analysed
         * @return A response containing a list of AnalysisReports or failure explaining why it failed.
         */
        public Response<List<AnalysisReport>> analyseEntity(String entityName) {
            return requestsByEntity.entrySet().stream()
                .filter(entry -> entityName.equals(entry.getKey()))
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        /**
         * Analyse the special endpoints that do not fit the regular entity endpoints. For example the /commoncropnames endpoint.
         *
         * @return A response containing a list of AnalysisReports or failure explaining why it failed.
         */
        public Response<List<AnalysisReport>> analyseSpecial() {
            return specialRequests.stream()
                .map(this::executeAPIRequest).collect(Response.toList());
        }

        /**
         * Get any errors found in the specification
         * @return any errors found in the specification
         */
        public Response<List<APIRequest>> getErrors() {
            return errors;
        }

        private Response<APIRequest> cacheRequest(Map.Entry<String, PathItem> pathItemEntry) {

            String endpoint = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            if (pathItem.getGet() != null && SPECIAL_CASE_ENDPOINTS.contains(endpoint)) {
                if (COMMON_CROP_NAMES_ENDPOINT.equals(endpoint)) {
                    return getAPIRequestBuilder("Get " + COMMON_CROP_NAMES_ENDPOINT,
                        SPECIAL_CASE_ENDPOINTS_INDEX, COMMON_CROP_NAMES, pathItem.getGet(), options.getGetEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName("commonCropName")
                                .parameterName("commonCropName")
                                .jsonPath("$.result.data[0]")
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName("commonCropNames")
                                .parameterName("commonCropNames")
                                .jsonPath("$.result.data[0]")
                                .convertToList(true)
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(specialRequests::add);
                }

                return success(null);
            }

            return PATH_PATTERN_MATCHERS.stream()
                .map(patternMatcher -> patternMatcher.match(endpoint))
                .filter(PatternMatcher::matches)
                .findFirst()
                .map(patternMatcher -> patternMatcher.execute(pathItem))
                .orElse(null) ;
        }

        private Response<APIRequest> cacheEntityPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2));
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingGetForEntity(entityName)) {
                    return getAPIRequestBuilder("Get Entity", GET_ENTITY_INDEX, entityName, pathItem.getGet(), options.getGetEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .pathParameter(Parameter.builder()
                                .parameterName(entityIdPropertyName)
                                .variableName(entityIdPropertyName + "1")
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, endpoint, "Get Entity");
                }
            }

            if (pathItem.getPost() != null) {
                unmatchedEndpoint(Request.Method.POST, endpoint, "Entity") ;
            }

            if (pathItem.getPut() != null) {
                String entityName = findEntityName(pathItem.getPut(), matcher.group(1), matcher.group(2)) ;
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingUpdateForEntity(entityName)) {
                    return getAPIRequestBuilder("Update Entity", UPDATE_ENTITY_INDEX, entityName, pathItem.getPut(), options.getUpdateEntity())
                        .onSuccessDoWithResult(builder -> builder
                        .validatorRequest(SimpleRequest.Builder
                            .put(endpoint)
                            .build())
                        .pathParameter(Parameter.builder()
                            .parameterName(entityIdPropertyName)
                            .variableName(entityIdPropertyName + "1")
                            .build()))
                        .mapResult(builder -> buildUpdateEntityBody(builder, pathItem.getPut().getRequestBody(), options.getUpdateEntity()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.PUT, endpoint, "Update Entity") ;
                }
            }

            if (pathItem.getDelete() != null) {
                String entityName = findEntityName(pathItem.getDelete(), matcher.group(1), matcher.group(2)) ;
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingDeleteForEntity(entityName)) {
                    return getAPIRequestBuilder("Delete Entity", DELETE_ENTITY_INDEX, entityName, pathItem.getDelete(), options.getDeleteEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .delete(endpoint)
                                .build())
                            .pathParameter(Parameter.builder()
                                .parameterName(entityIdPropertyName)
                                .variableName(entityIdPropertyName + "1")
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.DELETE, endpoint, "Delete Entity") ;
                }
            }

            return success(null);
        }

        private Response<APIRequest> cacheEntitiesPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2), matcher.group(3));

                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingListForEntity(entityName)) {
                    return getAPIRequestBuilder("List Entities", LIST_ENTITY_INDEX, entityName, pathItem.getGet(), options.getListEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName(entityIdPropertyName + "1")
                                .parameterName(entityIdPropertyName)
                                .jsonPath("$.result.data[0]." + entityIdPropertyName)
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, endpoint, "List Entities") ;
                }

                // TODO get from options other likely parameters, and extract variables for them
            }

            if (pathItem.getPost() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2), matcher.group(3));

                if (options.isAnalysingCreateForEntity(StringUtils.toSingular(entityName))) {
                    return getAPIRequestBuilder("Create Entities", CREATE_ENTITY_INDEX, entityName, pathItem.getPost(), options.getCreateEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName("new" + entityName)
                                .jsonPath("$.result.data[0]")
                                .build()))
                        .mapResult(builder -> buildCreateEntitiesBody(builder, pathItem.getPost().getRequestBody(), options.getCreateEntity()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.POST, endpoint, "Create Entities");
                }
            }

            if (pathItem.getPut() != null) {
                String entityName = findEntityName(pathItem.getPut(), matcher.group(1), matcher.group(2), matcher.group(3));

                if (options.isAnalysingUpdateForEntity(StringUtils.toSingular(entityName))) {
                    return getAPIRequestBuilder("Update Entities", UPDATE_ENTITY_INDEX, entityName, pathItem.getPut(), options.getUpdateEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .put(endpoint)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName("new" + entityName)
                                .jsonPath("$.result.data[0]")
                                .build()))
                        .mapResult(builder -> buildUpdateEntitiesBody(builder, pathItem.getPut().getRequestBody(), options.getUpdateEntity()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.PUT, endpoint, "Update Entities") ;
                }
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Entities");
            }

            return success(null);
        }

        private Response<APIRequest> cacheSearchPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {
                unmatchedEndpoint(Request.Method.GET, endpoint, "Search Entities");
            }

            if (pathItem.getPost() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2));

                if (options.isAnalysingSearchForEntity(entityName)) {
                    return getAPIRequestBuilder("Search", SEARCH_INDEX, entityName, pathItem.getPost(), options.getSearch())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build()))
                        .mapResult(builder -> buildSearchBody(builder, pathItem.getPost().getRequestBody(), options.getSearch()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.POST, endpoint, "Search Entities");
                }
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT, endpoint, "Search Entities");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Search Entities");
            }

            return success(null);
        }

        private Response<APIRequest> cacheSearchResultPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2));

                if (options.isAnalysingSearchResultForEntity(entityName)) {
                    return getAPIRequestBuilder("Search Results", SEARCH_RESULTS_INDEX, entityName, pathItem.getGet(), options.getSearchResult())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .pathParameter(Parameter.builder()
                                .parameterName("searchResultsDbId")
                                .variableName("searchResultsDbId1")
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, endpoint, "Search Results") ;
                }
            }

            if (pathItem.getPost() != null) {
                unmatchedEndpoint(Request.Method.POST, endpoint, "Search Results");
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT, endpoint, "Search Results");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Search Results");
            }

            return success(null);
        }

        private Response<APIRequest> cacheTablePath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1));

                if (options.isAnalysingTableForEntity(entityName)) {
                    return getAPIRequestBuilder("Get Table", TABLE_INDEX, entityName, pathItem.getGet(), options.getTable())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, endpoint, "Get Table");
                }
            }

            if (pathItem.getPost() != null) {

                String entityName = findEntityName(pathItem.getPost(), matcher.group(1));

                if (options.isAnalysingTableForEntity(entityName)) {
                    return getAPIRequestBuilder("Search Table", TABLE_INDEX, entityName, pathItem.getPost(), options.getTable())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build()))
                        .mapResult(builder -> buildTableBody(builder, pathItem.getPost().getRequestBody(), options.getTable()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.POST, endpoint, "Search Table");
                }
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT, endpoint, "Table");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Table");
            }

            return success(null);
        }

        private Response<APIRequest> cacheSubPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group() ;

            if (pathItem.getGet() != null) {

                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2), matcher.group(3));

                String entityIdPropertyName = matcher.group(3) ;

                if (options.isAnalysingListForEntity(entityName)) {
                    return getAPIRequestBuilder("List Sub Entities", LIST_ENTITY_INDEX, entityName, pathItem.getGet(), options.getListEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .pathParameter(Parameter.builder()
                                .parameterName(entityIdPropertyName)
                                .variableName(entityIdPropertyName + "1")
                                .build())
                            .prerequisite("/"+matcher.group(1)))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, endpoint, "List Sub Entities") ;
                }
            }

            if (pathItem.getPost() != null) {
                unmatchedEndpoint(Request.Method.POST, endpoint, "Sub Entities");
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT, endpoint, "Sub Entities");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Sub Entities");
            }

            return success(null);
        }

        private void addRequest(APIRequest request) {
            requests.put(request.getValidatorRequest().getPath(), request) ;
        }


        private void unmatchedEndpoint(Request.Method method, String path, String category) {
            unmatchedEndpoints.add(Endpoint.builder()
                .path(path)
                .method(method)
                .category(category)
                .build());
            log.warn(String.format("Unmatched endpoint %s '%s'", method, path));
        }

        private void skippedEndpoint(Request.Method method, String path, String category) {
            skippedEndpoints.add(Endpoint.builder()
                .path(path)
                .method(method)
                .category(category)
                .build());
            log.debug(String.format("Skipped endpoint %s '%s'", method, path));
        }

        private String findEntityName(Operation operation, String... pathElements) {

            if (operation != null && operation.getResponses() != null) {
                ApiResponse response = operation.getResponses().get("200");

                if (response != null) {
                    MediaType content = response.getContent().get("application/json");

                    if (content != null && content.getSchema() != null) {
                        Schema schema = content.getSchema();

                        if (schema.get$ref() != null) {
                            schema = findSchema(schema.get$ref()).orElseResult(null);
                        }

                        if (schema != null && schema.getTitle() != null) {
                            String entityName = schema.getTitle() ;

                            if (entityName.endsWith("ListResponse")) {
                                entityName = schema.getTitle().substring(0, entityName.length() - 12);
                            } else if (entityName.endsWith("SingleResponse")) {
                                entityName = entityName.substring(0, entityName.length() - 14);
                            } else if (entityName.endsWith("Response")) {
                                entityName = entityName.substring(0, entityName.length() - 8);
                            }

                            return StringUtils.capitalise(StringUtils.toSingular(entityName)) ;
                        }
                    }
                }
            }

            return StringUtils.capitalise(StringUtils.toSingular(String.join("", pathElements)));
        }

        private String guessEntityName(String pathElement) {
            return StringUtils.capitalise(StringUtils.toSingular(pathElement));
        }
        
        private APIRequest.APIRequestBuilder buildUpdateEntityBody(APIRequest.APIRequestBuilder builder, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildCreateEntitiesBody(APIRequest.APIRequestBuilder builder, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildUpdateEntitiesBody(APIRequest.APIRequestBuilder builder, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildSearchBody(APIRequest.APIRequestBuilder builder, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            getSchema(requestBody)
                .onFailDoWithResponse(response -> log.warn(response.getMessagesCombined(", ")))
                .mapResultToResponse(this::createBody)
                .onSuccessDoWithResult(builder::body);

            return builder;
        }

        private APIRequest.APIRequestBuilder buildTableBody(APIRequest.APIRequestBuilder builder, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            getSchema(requestBody)
                .onFailDoWithResponse(response -> log.warn(response.getMessagesCombined(", ")))
                .mapResultToResponse(this::createBody)
                .onSuccessDoWithResult(builder::body);

            return builder;
        }

        private Response<Object> createBody(Schema schema) {
            if (schema instanceof ObjectSchema objectSchema) {
                Map<String, Object> map = new HashMap<>();

                objectSchema.getProperties().forEach((key, value) -> {
                    if (options.isPartitionedByCrop() && key.equals("commonCropNames")) {
                        map.put("commonCropNames",
                            Parameter.builder()
                                .parameterName("commonCropNames")
                                .variableName("commonCropNames")
                                .build());
                    }

                    // TODO get from options other likely parameters

                });

                return success(map);

            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Schema must be object %s", schema));
            }
        }

        private Response<Schema> getSchema(RequestBody body) {
            if (body.get$ref() != null) {
                return findSchema(body.get$ref());
            } else {
                if (body.getContent().containsKey("application/json")) {
                    Schema schema = body.getContent().get("application/json").getSchema();

                    if (schema != null && schema.get$ref() != null) {
                        return findSchema(schema.get$ref());
                    } else {
                        return success(schema);
                    }
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema for %s", body));
        }

        private Response<Schema> findSchema(String ref) {
            Matcher matcher = REF_PATTERN.matcher(ref);

            if (matcher.matches()) {

                Schema schema = openAPI.getComponents().getSchemas().get(matcher.group(1));

                if (schema != null) {
                    return success(schema);
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema '%s'", ref));
        }

        private Response<APIRequest.APIRequestBuilder> getAPIRequestBuilder(String name, int index, String entityName, Operation operation, APIRequestOptions apiRequestOptions) {

            APIRequest.APIRequestBuilder builder = APIRequest.builder()
                .name(name)
                .index(index)
                .entityName(entityName);

            if (options.isPartitionedByCrop()) {
                findParameter("commonCropName", operation.getParameters()).ifPresent(
                    parameter -> addParameter(builder, parameter, "commonCropName")
                );
            }

            return findQueryParametersFor(operation.getParameters(), entityName, apiRequestOptions)
                .onSuccessDoWithResult(p -> addParameters(builder, p))
                .map(() -> success(builder));
        }

        private Response<List<io.swagger.v3.oas.models.parameters.Parameter>> findQueryParametersFor(List<io.swagger.v3.oas.models.parameters.Parameter> parameters, String entityName,
                                                                 APIRequestOptions apiRequestOptions) {
            return apiRequestOptions.getRequiredParametersFor(entityName).stream()
                .map(parameterName -> findParameterAsResponse(parameterName, parameters))
                .collect(Response.toList());
        }

        private Optional<io.swagger.v3.oas.models.parameters.Parameter> findParameter(String name, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return parameters.stream().filter(parameter -> parameter.getName().equals(name)).findFirst();
        }

        private Response<io.swagger.v3.oas.models.parameters.Parameter> findParameterAsResponse(String name, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return findParameter(name, parameters)
                .map(Response::success)
                .orElse(fail(Response.ErrorType.VALIDATION, String.format("Can not find parameter '%s", name))) ;
        }

        private void addParameter(APIRequest.APIRequestBuilder builder, io.swagger.v3.oas.models.parameters.Parameter parameter, String variableName) {

            Parameter.ParameterBuilder parameterBuilder = Parameter.builder()
                .parameterName(parameter.getName())
                .variableName(variableName);

            if (parameter.getIn().equals("path")) {
                builder.pathParameter(parameterBuilder.build());
            } else {
                if (parameter.getIn().equals("query")) {
                    builder.queryParameter(parameterBuilder.build());
                }
            }
        }

        private void addParameters(APIRequest.APIRequestBuilder builder, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            parameters.forEach(parameter -> addParameter(builder, parameter, parameter.getName()));
        }

        private Response<List<AnalysisReport>> executeAPIRequests(Map.Entry<String, List<APIRequest>> entry) {
            log.debug("Executing requests for {}", entry.getKey());

            return entry.getValue().stream().sorted(Comparator.comparing(APIRequest::getIndex)).map(this::executeAPIRequest).collect(Response.toList());
        }

        private Response<AnalysisReport> executeAPIRequest(APIRequest request) {
            log.debug("Executing request {} for {}", request.getName(), request.getEntityName());

            log.debug("Full request {}", request);

            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder();

                LocalDateTime startTime = LocalDateTime.now();

                return request.getPrerequisites().stream()
                    .map(this::executePrerequisite).collect(Response.toList())
                    .map(() -> createBody(request)
                        .mapResult(bodyPublisher -> builder.method(request.getValidatorRequest().getMethod().name(), bodyPublisher))
                        .map(authorizationProvider::getAuthorization)
                        .mapResult(authorization -> builder.header("Authorization", authorization))
                        .merge(() -> createURI(builder, request))
                        .mapResult(HttpRequest.Builder::build)
                        .mapResultToResponse(this::send)
                        .mapResultToResponse(httpResponse -> analyse(request, startTime, httpResponse))
                        .or(response -> success(AnalysisReport.builder()
                            .request(request)
                            .startTime(startTime)
                            .endTime(LocalDateTime.now())
                            .errorKey("Pre-Execution")
                            .errorLevel(ValidationReport.Level.WARN)
                            .errorMessage(response.getMessagesCombined(", "))
                            .build())));

            } catch (Exception e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<AnalysisReport> executePrerequisite(String prerequisite) {
            APIRequest request = requests.get(prerequisite);

            if (request != null) {
                return executeAPIRequest(request) ;
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find prerequisite '%s'", prerequisite));
            }
        }

        private Response<HttpRequest.Builder> createURI(HttpRequest.Builder builder, APIRequest request) {
            String path = request.getValidatorRequest().getPath() ;

            if (request.getPathParameters().isEmpty()) {
                Matcher matcher = PARAMETER_PATTERN.matcher(path) ;

                // check to make sure there is no missing parameters
                if (matcher.find()) {
                    Response<HttpRequest.Builder> response = fail(Response.ErrorType.VALIDATION,
                        String.format("Did not replace parameter '%s' in URI '%s'", matcher.group(1), path));

                    while (matcher.find()) {
                        response.addError(Response.ErrorType.VALIDATION, "",
                            String.format("Did not replace parameter '%s' in URI '%s'", matcher.group(1), path));
                    }

                    return response ;
                }

                return success(builder.uri(URI.create(String.format("%s%s", baseURL, path))));
            } else {
                return request.getPathParameters().stream()
                    .map(this::getVariableValue).collect(Response.toList())
                    .mapResultToResponse(variableValues -> replaceParametersWithVariableValues(path, variableValues))
                    .mapResult(p -> builder.uri(URI.create(String.format("%s%s", baseURL, p))));
            }
        }

        private Response<VariableValue> getVariableValue(Parameter parameter) {
            VariableValue value = variableValues.get(parameter.getVariableName());

            if (value != null) {
                return success(value);
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find variable '%s' for '%s", parameter.getVariableName(), parameter.getParameterName()));
            }
        }

        private Response<String> replaceParametersWithVariableValues(String path, List<VariableValue> variableValues) {
            try {
                for (VariableValue variableValue : variableValues) {
                    path = path.replace(String.format("{%s}",
                        variableValue.getParameterName() != null ? variableValue.getParameterName() : variableValue.getVariableName()),
                        writeValueAsString(variableValue.getValue()));
                }

                return success(path);
            } catch (JsonProcessingException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private CharSequence writeValueAsString(Object value) throws JsonProcessingException {
            if (value instanceof String stringValue) {
                return stringValue;
            } else {
                return objectMapper.writeValueAsString(value);
            }
        }

        private Response<HttpRequest.BodyPublisher> createBody(APIRequest request) {
            if (request.getBody() != null) {
                return replaceBodyWithVariableValues(request.getBody())
                    .mapResultToResponse(this::writeBody);
            } else {
                return success(HttpRequest.BodyPublishers.noBody());
            }
        }

        private Response<Object> replaceBodyWithVariableValues(Object body) {
            try {
                return success(replaceWithVariableValues(body)) ;
            } catch (RuntimeException runtimeException) {
                return Response.fail(Response.ErrorType.VALIDATION, runtimeException.getMessage()) ;
            }
        }

        private Object replaceWithVariableValues(Object body) throws RuntimeException {
            if (body instanceof List<?> list) {
                return list.stream().map(this::replaceBodyWithVariableValues).toList() ;
            } else if (body instanceof Map map) {

                HashMap newMap = new HashMap<>();

                map.forEach((key, value) -> newMap.put(key, replaceWithVariableValues(value))) ;

                return newMap ;

            } else if (body instanceof Parameter parameter) {
                VariableValue value = variableValues.get(parameter.getVariableName());

                if (value != null) {
                    return value.getValue() ;
                } else {
                    throw new RuntimeException(
                        String.format("Can not find value for variable '%s in parameter '%s;", parameter.getVariableName(), parameter.getParameterName()));
                }
            }

            return success(body);
        }

        private Response<HttpRequest.BodyPublisher> writeBody(Object body) {
            try {
                return success(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            } catch (JsonProcessingException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }


        private Response<AnalysisReport> analyse(APIRequest request, LocalDateTime startTime, HttpResponse<String> httpResponse) {

            AnalysisReport.AnalysisReportBuilder builder = AnalysisReport.builder()
                .request(request)
                .uri(httpResponse.request().uri().toString())
                .startTime(startTime)
                .statusCode(httpResponse.statusCode())
                .endTime(LocalDateTime.now());

            if (!request.getCacheVariables().isEmpty()) {
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {

                    try {
                        DocumentContext documentContext = JsonPath.parse(httpResponse.body());
                        request.getCacheVariables().forEach(variable -> cacheVariableValue(documentContext, variable));
                    } catch (Exception e) {
                        builder.errorKey(e.getClass().getSimpleName());
                        builder.errorLevel(ValidationReport.Level.WARN) ;
                        builder.errorMessage(e.getMessage());
                    }
                } else {
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cache value for variables '%s', return code was '%s'",
                        request.getCacheVariables().stream().map(Variable::getVariableName).collect(Collectors.joining(", ")), httpResponse.statusCode()));
                }
            }

            return success(builder
                .request(request)
                .validationReport(validator.validate(request.getValidatorRequest(), createResponse(httpResponse)))
                .endTime(LocalDateTime.now())
                .build());
        }

        private void cacheVariableValue(DocumentContext documentContext, Variable variable) {
            if (variable.getJsonPath() != null) {
                variableValues.put(variable.getVariableName(), createVariableValue(variable, documentContext.read(variable.getJsonPath())));
            }
        }

        private VariableValue createVariableValue(Variable variable, Object value) {
            if (variable.isConvertToList()) {
                return VariableValue.builder()
                    .variableName(variable.getVariableName())
                    .parameterName(variable.getParameterName())
                    .value(Collections.singletonList(value))
                    .build();
            } else {
                return VariableValue.builder()
                    .variableName(variable.getVariableName())
                    .parameterName(variable.getParameterName())
                    .value(value)
                    .build();
            }
        }

        private Response<HttpResponse<String>> send(HttpRequest request) {
            log.debug(String.format("Sending %s %s", request.method(), request.uri()));
            log.debug(String.format("Request body publisher %s", request.bodyPublisher().orElse(null)));
            try {
                return success(client.send(request, HttpResponse.BodyHandlers.ofString()));
            } catch (IOException | InterruptedException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private com.atlassian.oai.validator.model.Response createResponse(HttpResponse<String> response) {
            log.debug(String.format("Response was %s for %s", response.statusCode(), response.uri()));
            log.debug(String.format("Response body %s", response.body()));
            return SimpleResponse.Builder
                .status(response.statusCode())
                .withContentType(response.headers().firstValue("Content-Type").orElse(null))
                .withBody(response.body())
                .build();
        }
    }


    private static class PatternMatcher {
        private final Pattern pattern ;
        private final BiFunction<PathItem, Matcher, Response<APIRequest>> function ;

        private Matcher matcher ;

        private PatternMatcher(Pattern pattern, BiFunction<PathItem, Matcher, Response<APIRequest>> function) {
            this.pattern = pattern;
            this.function = function;
        }

        public PatternMatcher match(String input) {
            matcher = pattern.matcher(input) ;
            return this ;
        }

        public boolean matches() {
            return matcher.matches() ;
        }

        public Response<APIRequest> execute(PathItem pathItem) {
            return function.apply(pathItem, matcher);
        }
    }
}