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

        return analyser.getErrors().merge(options.validate().asResponse())
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

        return analyser.getErrors().merge(options.validate().asResponse())
            .map(() -> Stream.of(analyser.analyseSpecial(), analyser.analyseEntities(entityNames)).collect(Response.mergeLists()));
    }

    /**
     * Creates a new analyser and validates the options against the specification.
     * Shortcut for {@link AnalysisOptions#validate()}
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<Validation> validate(String specification) {

        Analyser analyser = new Analyser(specification);

        return analyser.getErrors().merge(options.validate().asResponse());
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
        return new Analyser(specification);
    }

    /**
     * Analyser provides direct access to the analysis functions.
     */
    public class Analyser {

        private final OpenAPI openAPI;
        private final OpenApiInteractionValidator validator;
        private final Map<String, APIRequest> requests = new HashMap<>();

        private final Set<APIRequest> specialRequests = new TreeSet<>(Comparator.comparingInt(APIRequest::getIndex));

        private final Map<String, List<APIRequest>> requestsByEntity;

        private final List<Endpoint> endpoints = new ArrayList<>();
        private final List<Endpoint> unmatchedEndpoints = new ArrayList<>();
        private final List<Endpoint> skippedEndpoints = new ArrayList<>();
        private final List<Endpoint> deprecatedEndpoints = new ArrayList<>();
        private final Map<String, VariableValue> variableValues = new HashMap<>();

        private static final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(\\w+)(?:/)?(\\w+)?/\\{(\\w+)\\}"); // 3 groups
        private static final Pattern ENTITIES_PATH_PATTERN = Pattern.compile("/(\\w+)(?:/)?(\\w+)?(?:/)?(\\w+)?"); // 3 groups, ignore last
        private static final Pattern SEARCH_PATH_PATTERN = Pattern.compile("/search/(\\w+)(/attributes|/attributevalues)?"); // 2 groups
        private static final Pattern SEARCH_RESULTS_PATH_PATTERN = Pattern.compile("/search/(\\w+)(/attributes|/attributevalues)?/\\{(\\w+)\\}"); // 3 groups, ignore last
        private static final Pattern TABLE_PATH_PATTERN = Pattern.compile("/(\\w+)/table"); // 1 group
        private static final Pattern ENTITY_SUB_PATH_PATTERN = Pattern.compile("/(\\w+)(?:/)?(\\w+)?/\\{(\\w+)\\}/(\\w+)"); // 3 groups, ignore 3rd
        private final List<PathMatcher> PATH_PATTERN_MATCHERS = Arrays.asList(
            new PathMatcher(TABLE_PATH_PATTERN, this::cacheTablePath),
            new PathMatcher(SEARCH_PATH_PATTERN, this::cacheSearchPath),
            new PathMatcher(SEARCH_RESULTS_PATH_PATTERN, this::cacheSearchResultPath),
            new PathMatcher(ENTITIES_PATH_PATTERN, this::cacheEntitiesPath),
            new PathMatcher(ENTITY_PATH_PATTERN, this::cacheEntityPath),
            new PathMatcher(ENTITY_SUB_PATH_PATTERN, this::cacheSubPath));

        private final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(\\w+)\\}");
        private final Response<List<APIRequest>> errors;

        private static final List<String> PRIMITIVES = Arrays.asList("string", "boolean");

        /**
         * Create an Analysis based on a OpenAPI specification
         *
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
                .onFailDoWithResponse(Response::getAllErrors);

            requestsByEntity = new TreeMap<>(requests.values().stream().collect(Collectors.groupingBy(APIRequest::getEntityName)));
        }

        /**
         * Get the list of entities available on the server
         *
         * @return list of entities available on the server
         */
        public List<String> getEntityNames() {
            return new ArrayList<>(requestsByEntity.keySet());
        }

        /**
         * Gets a list of endpoints that are to be tested.
         *
         * @return a list of endpoints that are to be tested.
         */
        public List<Endpoint> getEndpoints() {
            return endpoints;
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
         * Gets a list of deprecated endpoints that will be skipped.
         *
         * @return a list of deprecated endpoints that will be skipped.
         */
        public List<Endpoint> getDeprecatedEndpoints() {
            return deprecatedEndpoints;
        }


        /**
         * Analyse all the endpoints in the specification.
         * Does not call {@link AnalysisOptions#validate()} or {@link #analyseSpecial()}.
         *
         * @return A response containing a list of AnalysisReports or failure explaining why it failed.
         */
        public Response<List<AnalysisReport>> analyseAll() {
            if (errors.hasErrors()) {
                return errors.merge(Response.empty());
            }

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
            if (errors.hasErrors()) {
                return errors.merge(Response.empty());
            }

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
            if (errors.hasErrors()) {
                return errors.merge(Response.empty());
            }

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
            if (errors.hasErrors()) {
                return errors.merge(Response.empty());
            }

            return specialRequests.stream()
                .map(this::executeAPIRequest).collect(Response.toList());
        }

        /**
         * Validates the options against the specification.
         *
         * @return A response containing this Analyser or failure explaining why it failed.
         */
        public Response<Analyser> validate() {
            if (errors.hasErrors()) {
                return errors.merge(Response.empty());
            }
            return success(this);
        }

        /**
         * Get any errors found in the specification
         *
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
                .map(pathMatcher -> pathMatcher.match(endpoint))
                .filter(PathMatcher::matches)
                .findFirst()
                .map(pathMatcher -> pathMatcher.createRequest(pathItem))
                .orElse(null);
        }

        private Response<APIRequest> cacheEntityPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2));
                String entityIdPropertyName = getIdPropertyNameFor(entityName);

                if (options.isAnalysingGetForEntity(entityName) && isAnalysingOperation(pathItem.getGet())) {
                    return getAPIRequestBuilder("Get Entity", GET_ENTITY_INDEX, entityName, pathItem.getGet(), options.getGetEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build()))
                        .mapResultToResponse(builder -> enrichWithParameter(builder,
                            entityIdPropertyName,
                            entityIdPropertyName + "1",
                            pathItem.getGet().getParameters()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, pathItem.getGet(), endpoint, "Get Entity");
                }
            }

            if (pathItem.getPost() != null) {
                unmatchedEndpoint(Request.Method.POST, endpoint, "Entity");
            }

            if (pathItem.getPut() != null) {
                String entityName = findEntityName(pathItem.getPut(), matcher.group(1), matcher.group(2));
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingUpdateForEntity(entityName) && isAnalysingOperation(pathItem.getPut())) {
                    return getAPIRequestBuilder("Update Entity", UPDATE_ENTITY_INDEX, entityName, pathItem.getPut(), options.getUpdateEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .put(endpoint)
                                .build()))
                        .mapResultToResponse(builder -> enrichWithParameter(builder,
                            entityIdPropertyName,
                            entityIdPropertyName + "1",
                            pathItem.getGet().getParameters()))
                        .mapResult(builder -> buildUpdateEntityBody(builder, pathItem.getPut().getRequestBody(), options.getUpdateEntity()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.PUT, pathItem.getPut(), endpoint, "Update Entity");
                }
            }

            if (pathItem.getDelete() != null) {
                String entityName = findEntityName(pathItem.getDelete(), matcher.group(1), matcher.group(2));
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (options.isAnalysingDeleteForEntity(entityName) && isAnalysingOperation(pathItem.getDelete())) {
                    return getAPIRequestBuilder("Delete Entity", DELETE_ENTITY_INDEX, entityName, pathItem.getDelete(), options.getDeleteEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .delete(endpoint)
                                .build()))
                        .mapResultToResponse(builder -> enrichWithParameter(builder,
                            entityIdPropertyName,
                            entityIdPropertyName + "1",
                            pathItem.getGet().getParameters()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.DELETE, pathItem.getDelete(), endpoint, "Delete Entity");
                }
            }

            return Response.empty();
        }

        private Response<APIRequest> cacheEntitiesPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2), matcher.group(3));

                String entityIdPropertyName = getIdPropertyNameFor(entityName);

                if (options.isAnalysingListForEntity(entityName) && isAnalysingOperation(pathItem.getGet())) {
                    return getAPIRequestBuilder("List Entities", LIST_ENTITY_INDEX, entityName, pathItem.getGet(), options.getListEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build()))
                        .onSuccessDoWithResultOnCondition(entityIdPropertyName != null, builder -> builder
                            .cacheVariable(Variable.builder()
                                .variableName(entityIdPropertyName + "1")
                                .parameterName(entityIdPropertyName)
                                .jsonPath("$.result.data[0]." + entityIdPropertyName)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName(entityIdPropertyName + "s1")
                                .parameterName(entityIdPropertyName + "s")
                                .jsonPath("$.result.data[0:10]." + entityIdPropertyName)
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, pathItem.getGet(), endpoint, "List Entities");
                }
            }

            if (pathItem.getPost() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2), matcher.group(3));

                if (options.isAnalysingCreateForEntity(entityName) && isAnalysingOperation(pathItem.getPost())) {
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
                    skippedEndpoint(Request.Method.POST, pathItem.getPost(), endpoint, "Create Entities");
                }
            }

            if (pathItem.getPut() != null) {
                String entityName = findEntityName(pathItem.getPut(), matcher.group(1), matcher.group(2), matcher.group(3));

                if (options.isAnalysingUpdateForEntity(entityName) && isAnalysingOperation(pathItem.getPut())) {
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
                    skippedEndpoint(Request.Method.PUT, pathItem.getPut(), endpoint, "Update Entities");
                }
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Entities");
            }

            return Response.empty();
        }

        private Response<APIRequest> cacheSearchPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {
                unmatchedEndpoint(Request.Method.GET, endpoint, "Search Entities");
            }

            if (pathItem.getPost() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2));

                if (options.isAnalysingSearchForEntity(entityName) && isAnalysingOperation(pathItem.getPost())) {
                    return getAPIRequestBuilder("Search Entities", SEARCH_INDEX, entityName, pathItem.getPost(), options.getSearch())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build()))
                        .mapResult(builder -> buildSearchBody(builder, entityName, pathItem.getPost().getRequestBody(), options.getSearch()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.POST, pathItem.getPost(), endpoint, "Search Entities");
                }
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT,  endpoint, "Search Entities");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Search Entities");
            }

            return Response.empty();
        }

        private Response<APIRequest> cacheSearchResultPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1), matcher.group(2));

                if (options.isAnalysingSearchResultForEntity(entityName) && isAnalysingOperation(pathItem.getGet())) {
                    return getAPIRequestBuilder("Search Results", SEARCH_RESULTS_INDEX, entityName, pathItem.getGet(), options.getSearchResult())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build()))
                        .mapResultToResponse(builder -> enrichWithParameter(builder,
                            "searchResultsDbId",
                            "searchResultsDbId1",
                            pathItem.getGet().getParameters()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, pathItem.getGet(), endpoint, "Search Results");
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

            return Response.empty();
        }

        private Response<APIRequest> cacheTablePath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {
                String entityName = findEntityName(pathItem.getPost(), matcher.group(1));

                if (options.isAnalysingTableForEntity(entityName) && isAnalysingOperation(pathItem.getGet())) {
                    return getAPIRequestBuilder("Get Table", TABLE_INDEX, entityName, pathItem.getGet(), options.getTable())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, pathItem.getPost(), endpoint, "Get Table");
                }
            }

            if (pathItem.getPost() != null) {

                String entityName = findEntityName(pathItem.getPost(), matcher.group(1));

                if (options.isAnalysingTableForEntity(entityName) && isAnalysingOperation(pathItem.getPost())) {
                    return getAPIRequestBuilder("Search Table", TABLE_INDEX, entityName, pathItem.getPost(), options.getTable())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build()))
                        .mapResult(builder -> buildTableBody(builder, entityName, pathItem.getPost().getRequestBody(), options.getTable()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.POST, pathItem.getPost(), endpoint, "Search Table");
                }
            }

            if (pathItem.getPut() != null) {
                unmatchedEndpoint(Request.Method.PUT, endpoint, "Table");
            }

            if (pathItem.getDelete() != null) {
                unmatchedEndpoint(Request.Method.DELETE, endpoint, "Table");
            }

            return Response.empty();
        }

        private Response<APIRequest> cacheSubPath(PathItem pathItem, Matcher matcher) {
            String endpoint = matcher.group();

            if (pathItem.getGet() != null) {

                String entityName = findEntityName(pathItem.getGet(), matcher.group(1), matcher.group(2), matcher.group(3));

                String entityIdPropertyName = matcher.group(3);

                if (options.isAnalysingListForEntity(entityName) && isAnalysingOperation(pathItem.getGet())) {
                    return getAPIRequestBuilder("List Sub Entities", LIST_ENTITY_INDEX, entityName, pathItem.getGet(), options.getListEntity())
                        .onSuccessDoWithResult(builder -> builder
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .prerequisite("/" + matcher.group(1)))
                        .mapResultToResponse(builder -> enrichWithParameter(builder,
                            entityIdPropertyName,
                            entityIdPropertyName + "1",
                            pathItem.getGet().getParameters()))
                        .mapResult(APIRequest.APIRequestBuilder::build)
                        .onSuccessDoWithResult(this::addRequest);
                } else {
                    skippedEndpoint(Request.Method.GET, pathItem.getGet(), endpoint, "List Sub Entities");
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

            return Response.empty();
        }

        private boolean isAnalysingOperation(Operation operation) {
            if (isDeprecated(operation)) {
                return options.isAnalysingDepreciated() ;
            }
            return true ;
        }

        private boolean isDeprecated(Operation operation) {
            return operation.getDeprecated() != null && operation.getDeprecated() ;
        }

        private Response<APIRequest.APIRequestBuilder> enrichWithParameter(APIRequest.APIRequestBuilder builder, String parameterName, String variableName,
                                                                           List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            if (parameterName != null) {
                return findParameterLinkAsResponse(parameterName, variableName, parameters)
                    .onSuccessDoWithResult(builder::pathParameter)
                    .map(() -> success(builder));
            } else {
                return success(builder);
            }
        }

        private String getIdPropertyNameFor(String entityName) {
            if (entityName == null || PRIMITIVES.contains(entityName.toLowerCase())) {
                return null;
            } else {
                return options.getProperties().getIdPropertyNameFor(entityName);
            }
        }

        private void addRequest(APIRequest request) {
            requests.put(request.getValidatorRequest().getPath(), request);

            endpoints.add(Endpoint.builder()
                .path(request.getValidatorRequest().getPath())
                .method(request.getValidatorRequest().getMethod())
                .category(request.getName())
                .build());
        }

        private void unmatchedEndpoint(Request.Method method, String path, String category) {
            unmatchedEndpoints.add(Endpoint.builder()
                .path(path)
                .method(method)
                .category(category)
                .build());
            log.warn(String.format("Unmatched endpoint %s '%s'", method, path));
        }

        private void skippedEndpoint(Request.Method method, Operation operation, String path, String category) {
            if (isDeprecated(operation)) {
                deprecatedEndpoint(method, path, category) ;
            } else {
                skippedEndpoint(method, path, category);
            }
        }

        private void skippedEndpoint(Request.Method method, String path, String category) {
            skippedEndpoints.add(Endpoint.builder()
                .path(path)
                .method(method)
                .category(category)
                .build());
            log.debug(String.format("Skipped endpoint %s '%s'", method, path));
        }

        private void deprecatedEndpoint(Request.Method method, String path, String category) {
            deprecatedEndpoints.add(Endpoint.builder()
                .path(path)
                .method(method)
                .category(category)
                .build());
            log.debug(String.format("Skipped deprecated endpoint %s '%s'", method, path));
        }

        private String findEntityName(Operation operation, String... pathElements) {

            if (operation != null && operation.getResponses() != null) {
                ApiResponse response = operation.getResponses().get("200");

                if (response != null) {
                    MediaType content = response.getContent().get("application/json");

                    if (content != null && content.getSchema() != null) {
                        Schema schema = content.getSchema();

                        if (schema.get$ref() != null) {
                            schema = findSchemaFromRef(schema.get$ref()).orElseResult(null);
                        }

                        if (schema != null && schema.getTitle() != null) {
                            String entityName = schema.getTitle() != null ? schema.getTitle() : schema.getName();

                            if (entityName.startsWith("200")) {
                                return Stream.of(pathElements)
                                    .filter(Objects::nonNull)
                                    .map(pathElement -> StringUtils.capitalise(StringUtils.toSingular(pathElement)))
                                    .collect(Collectors.joining());
                            } else if (entityName.endsWith("ListResponse")) {
                                return findChildSchema(schema, "result")
                                    .mapResultToResponse(s -> findChildSchema(s, "data"))
                                    .mapResultToResponse(this::findEntityName)
                                    .getResultIfPresentOrElseResult(entityName.substring(0, entityName.length() - 12));
                            } else if (entityName.endsWith("SingleResponse")) {
                                return findChildSchema(schema, "result")
                                    .mapResultToResponse(this::findEntityName)
                                    .getResultIfPresentOrElseResult(entityName.substring(0, entityName.length() - 14));
                            } else if (entityName.endsWith("Response")) {
                                return findChildSchema(schema, "result")
                                    .mapResultToResponse(this::findEntityName)
                                    .getResultIfPresentOrElseResult(entityName.substring(0, entityName.length() - 8));
                            } else {
                                return StringUtils.capitalise(StringUtils.toSingular(entityName));
                            }
                        }
                    }
                }
            }

            return Stream.of(pathElements)
                .filter(Objects::nonNull)
                .map(pathElement -> StringUtils.capitalise(StringUtils.toSingular(pathElement)))
                .collect(Collectors.joining());
        }

        private Response<String> findEntityName(Schema schema) {
            if (schema.getType() != null && PRIMITIVES.contains(schema.getType().toLowerCase())) {
                return Response.empty() ;
            }

            if (schema instanceof ObjectSchema) {
                if (schema.getName() != null) {
                    return success(schema.getName());
                } else if (schema.getTitle() != null) {
                    return success(schema.getTitle().replace(" ", ""));
                }
                if (schema.getExtensions() != null // fall back on x-brapi-metadata
                    && schema.getExtensions().containsKey("x-brapi-metadata")
                    && ((Map<String, String>) schema.getExtensions().get("x-brapi-metadata")).containsKey("title")) {
                    return success(((Map<String, String>) schema.getExtensions().get("x-brapi-metadata")).get("title").replace(" ", ""));
                } else {
                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find name in '%s'", schema));
                }
            } else {
                return success(schema.getType());
            }
        }

        private Response<Schema> findChildSchema(Schema schema, String propertyName) {
            if (schema != null && schema.get$ref() != null) {
                schema = findSchemaFromRef(schema.get$ref()).orElseResult(null);
            }

            if (schema != null) {
                if (schema.getProperties() != null) {
                    schema = (Schema) schema.getProperties().get(propertyName);

                    if (schema == null) {
                        return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find child schema in property '%s'", propertyName));
                    }

                    return findChildSchema(schema);
                } else {
                    return success(schema);
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find child schema in property '%s'", propertyName));
        }

        private Response<Schema> findChildSchema(Schema schema) {
            if (schema != null && schema.get$ref() != null) {
                schema = findSchemaFromRef(schema.get$ref()).orElseResult(null);
            }

            if (schema != null) {
                if (schema.getItems() != null) {
                    return findChildSchema(schema.getItems());
                } else {
                    return success(schema);
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, "Can not find child schema");
        }

        private Response<Schema> derefSchema(Schema schema) {
            if (schema.get$ref() != null) {
                return findSchemaFromRef(schema.get$ref());
            }
            return success(schema);
        }

        private Response<Schema> findSchemaFromRef(String ref) {
            Matcher matcher = REF_PATTERN.matcher(ref);

            if (matcher.matches()) {

                Schema schema = openAPI.getComponents().getSchemas().get(matcher.group(1));

                if (schema != null) {
                    if (schema.getName() == null) {
                        schema.setName(matcher.group(1)); // make sure the name is set
                    }
                    return success(schema);
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema '%s'", ref));
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

        private APIRequest.APIRequestBuilder buildSearchBody(APIRequest.APIRequestBuilder builder, String entityName, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            getSchema(requestBody)
                .onFailDoWithResponse(response -> log.warn(response.getMessagesCombined(", ")))
                .mapResultToResponse(schema -> buildBody(entityName, schema, apiRequestOptions))
                .onSuccessDoWithResult(builder::body);

            return builder;
        }

        private APIRequest.APIRequestBuilder buildTableBody(APIRequest.APIRequestBuilder builder, String entityName, RequestBody requestBody, APIRequestOptions apiRequestOptions) {
            getSchema(requestBody)
                .onFailDoWithResponse(response -> log.warn(response.getMessagesCombined(", ")))
                .mapResultToResponse(schema -> buildBody(entityName, schema, apiRequestOptions))
                .onSuccessDoWithResult(builder::body);

            return builder;
        }

        private Response<Object> buildBody(String entityName, Schema schema, APIRequestOptions apiRequestOptions) {
            if (schema instanceof ObjectSchema objectSchema) {
                Map<String, Object> map = new HashMap<>();

                return apiRequestOptions.getRequiredParametersFor(entityName).stream()
                    .filter(parameter -> parameter.getIn().equals("body"))
                    .map(parameter -> findSchemaProperty(objectSchema, parameter.getParameterName())
                        .onSuccessDoWithResult(propertySchema -> map.put(parameter.getParameterName(),
                            PropertyLink.builder()
                                .propertyName(parameter.getParameterName())
                                .schema(propertySchema)
                                .variableName(parameter.getVariableName())
                                .build())))
                    .collect(Response.toList())
                    .mapOnCondition(options.isPartitionedByCrop(), () -> findSchemaProperty(objectSchema, "commonCropNames")
                        .onSuccessDoWithResult(propertySchema -> map.put("commonCropNames",
                            PropertyLink.builder()
                                .propertyName("commonCropNames")
                                .schema(propertySchema)
                                .variableName("commonCropNames")
                                .build())))
                    .map(() -> success(map));

            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Schema must be object %s", schema));
            }
        }

        private Response<Schema> findSchemaProperty(ObjectSchema objectSchema, String propertyName) {
            Schema schema = objectSchema.getProperties().get(propertyName);

            if (schema != null) {
                return success(schema) ;
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema for property %s", propertyName));
            }
        }

        private Response<Schema> getSchema(RequestBody body) {
            if (body.get$ref() != null) {
                return findSchemaFromRef(body.get$ref());
            } else {
                if (body.getContent().containsKey("application/json")) {
                    Schema schema = body.getContent().get("application/json").getSchema();

                    if (schema != null && schema.get$ref() != null) {
                        return findSchemaFromRef(schema.get$ref());
                    } else {
                        return success(schema);
                    }
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema for %s", body));
        }

        private Response<APIRequest.APIRequestBuilder> getAPIRequestBuilder(String name, int index, String entityName, Operation operation, APIRequestOptions apiRequestOptions) {

            APIRequest.APIRequestBuilder builder = APIRequest.builder()
                .name(name)
                .index(index)
                .entityName(entityName);

            if (options.isPartitionedByCrop()) {
                findParameter("commonCropName", operation.getParameters()).ifPresent(
                    parameter -> addParameterLink(builder, ParameterLink.builder()
                        .parameter(parameter)
                        .variableName("commonCropName")
                        .build())) ;
            }

            apiRequestOptions.getPrerequisitesFor(entityName).forEach(builder::prerequisite);

            return findQueryParameterLinksFor(operation.getParameters(), entityName, apiRequestOptions)
                .onSuccessDoWithResult(p -> addParameterLinks(builder, p))
                .map(() -> success(builder));
        }

        private Response<List<ParameterLink>> findQueryParameterLinksFor(List<io.swagger.v3.oas.models.parameters.Parameter> parameters, String entityName,
                                                                                                     APIRequestOptions apiRequestOptions) {
            return apiRequestOptions.getRequiredParametersFor(entityName).stream()
                .filter(parameter -> parameter.getIn().equals("query"))
                .map(parameter -> findParameterLinkAsResponse(parameter, parameters))
                .collect(Response.toList());
        }

        private Response<ParameterLink> findParameterLinkAsResponse(Parameter parameter, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return findParameterLinkAsResponse(parameter.getParameterName(), parameter.getVariableName(), parameters) ;
        }

        private Response<ParameterLink> findParameterLinkAsResponse(String parameterName, String variableName, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return findParameter(parameterName, parameters)
                .map(p -> ParameterLink.builder()
                    .parameter(p)
                    .variableName(variableName)
                    .build())
                .map(Response::success)
                .orElse(fail(Response.ErrorType.VALIDATION, String.format("Can not find parameter '%s' for variable '%s' in '%s'", parameterName, variableName, parameters)));
        }

        private Optional<io.swagger.v3.oas.models.parameters.Parameter> findParameter(String name, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return parameters.stream().filter(parameter -> parameter.getName().equals(name)).findFirst();
        }

        private void addParameterLink(APIRequest.APIRequestBuilder builder, ParameterLink parameterLink) {
            if (parameterLink.getParameter().getIn().equals("path")) {
                builder.pathParameter(parameterLink);
            } else {
                if (parameterLink.getParameter().getIn().equals("query")) {
                    builder.queryParameter(parameterLink);
                }
            }
        }

        private void addParameterLinks(APIRequest.APIRequestBuilder builder, List<ParameterLink> parameterLinks) {
            parameterLinks.forEach(parameterLink -> addParameterLink(builder, parameterLink));
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
                return executeAPIRequest(request);
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find prerequisite '%s'", prerequisite));
            }
        }

        private Response<HttpRequest.Builder> createURI(HttpRequest.Builder builder, APIRequest request) {
            return createPath(request)
                .mapResult(path -> builder.uri(URI.create(path)));
        }

        private Response<String> createPath(APIRequest request) {
            String path = String.format("%s%s", baseURL, request.getValidatorRequest().getPath());

            if (request.getPathParameters().isEmpty()) {
                Matcher matcher = PARAMETER_PATTERN.matcher(path);

                // check to make sure there is no missing parameters
                if (matcher.find()) {
                    Response<String> response = fail(Response.ErrorType.VALIDATION,
                        String.format("Did not replace parameter '%s' in URI '%s'", matcher.group(1), path));

                    while (matcher.find()) {
                        response.addError(Response.ErrorType.VALIDATION, "",
                            String.format("Did not replace parameter '%s' in URI '%s'", matcher.group(1), path));
                    }

                    return response;
                }

                return appendQuery(path, request) ;
            } else {
                return request.getPathParameters().stream()
                    .map(this::getVariableValue).collect(Response.toList())
                    .mapResultToResponse(variableValues -> replaceParametersWithVariableValues(path, variableValues))
                    .mapResultToResponse(p -> appendQuery(p, request));
            }
        }

        private Response<String> appendQuery(String path, APIRequest request) {
            if (request.getQueryParameters().isEmpty()) {
                return success(path) ;
            } else {
                return request.getQueryParameters().stream()
                    .map(this::createQueryParam)
                    .collect(Response.toList())
                    .mapResult(params -> String.join("&", params))
                    .mapResult(p -> String.format("%s?%s", path, p));
            }
        }

        private Response<String> createQueryParam(ParameterLink parameterLink) {
            return getVariableValue(parameterLink)
                .mapResultToResponse(variableValue -> writeValueAsStringAsResource(variableValue.getValue()))
                .mapResult(value -> parameterLink.getParameterName() + "=" + value);
        }

        private Response<VariableValue> getVariableValue(ParameterLink parameter) {
            VariableValue value = variableValues.get(parameter.getVariableName());

            if (value != null) {
                return success(value);
            } else {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find variable '%s' for '%s", parameter.getVariableName(), parameter.getParameter().getName()));
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

        private String writeValueAsString(Object value) throws JsonProcessingException {
            if (value instanceof String stringValue) {
                return stringValue;
            } else {
                return objectMapper.writeValueAsString(value);
            }
        }

        private Response<String> writeValueAsStringAsResource(Object value) {
            try {
                return success(writeValueAsString(value));
            } catch (JsonProcessingException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
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
                return success(replaceWithVariableValues(body));
            } catch (RuntimeException runtimeException) {
                return Response.fail(Response.ErrorType.VALIDATION, runtimeException.getMessage());
            }
        }

        private Object replaceWithVariableValues(Object body) throws RuntimeException {
            if (body instanceof List<?> list) {
                return list.stream().map(this::replaceBodyWithVariableValues).toList();
            } else if (body instanceof Map map) {

                HashMap<Object, Object> newMap = new HashMap<>();

                map.forEach((key, value) -> newMap.put(key, replaceWithVariableValues(value)));

                return newMap;

            } else if (body instanceof PropertyLink propertyLink) {
                VariableValue value = variableValues.get(propertyLink.getVariableName());

                if (value != null) {
                    return value.getValue();
                } else {
                    throw new RuntimeException(
                        String.format("Can not find value for variable '%s in property '%s;", propertyLink.getVariableName(), propertyLink.getPropertyName()));
                }
            }

            return body;
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
                        builder.errorLevel(ValidationReport.Level.WARN);
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


    private static class PathMatcher {
        private final Pattern pattern;
        private final BiFunction<PathItem, Matcher, Response<APIRequest>> function;

        private Matcher matcher;

        private PathMatcher(Pattern pattern, BiFunction<PathItem, Matcher, Response<APIRequest>> function) {
            this.pattern = pattern;
            this.function = function;
        }

        public PathMatcher match(String input) {
            matcher = pattern.matcher(input);
            return this;
        }

        public boolean matches() {
            return matcher.matches();
        }

        public Response<APIRequest> createRequest(PathItem pathItem) {
            return function.apply(pathItem, matcher);
        }
    }
}