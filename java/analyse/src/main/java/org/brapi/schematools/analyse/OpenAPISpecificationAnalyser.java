package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.analyse.authorization.AuthorizationProvider;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Analyses BrAPI endpoints against an OpenAPI Specification
 */
@Slf4j
public class OpenAPISpecificationAnalyser {

    private static final int SPECIAL_CASE_ENDPOINTS_INDEX = 0;
    private static final int LIST_ENTITY_INDEX = 10;
    private static final int GET_ENTITY_INDEX = 20;
    private static final int SEARCH_INDEX = 30;
    private static final int SEARCH_RESULTS_INDEX = 40;
    private static final int CREATE_ENTITY_INDEX = 50;
    private static final int UPDATE_ENTITY_INDEX = 60;
    private static final int DELETE_ENTITY_INDEX = 70;
    private static final Pattern REF_PATTERN = Pattern.compile("#/components/schemas/(\\w+)");

    private final String baseURL;
    private final HttpClient client;
    private final AuthorizationProvider authorizationProvider;
    private final AnalysisOptions options;

    private final ObjectMapper objectMapper;

    private final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(\\w+)/\\{(\\w+)\\}");
    private final Pattern ENTITIES_PATH_PATTERN = Pattern.compile("/(\\w+)");
    private final Pattern SEARCH_PATH_PATTERN = Pattern.compile("/search/(\\w+)");
    private final Pattern SEARCH_RESULTS_PATH_PATTERN = Pattern.compile("/search/(\\w+/)\\{(\\w+)\\}");

    private final String COMMON_CROP_NAMES = "commoncropnames";
    private final String COMMON_CROP_NAMES_ENDPOINT = "/" + COMMON_CROP_NAMES;

    private final List<String> SPECIAL_CASE_ENDPOINTS = List.of(COMMON_CROP_NAMES_ENDPOINT);

    /**
     * Create an Analyser
     *
     * @param baseURL               the base URl for the BrAPI server
     * @param client                the HTTP client to use for the execution of requests
     * @param authorizationProvider the authorization provider need for authorization
     */
    public OpenAPISpecificationAnalyser(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider) {
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
    public OpenAPISpecificationAnalyser(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider, AnalysisOptions options) {
        this.baseURL = baseURL;
        this.client = client;
        this.authorizationProvider = authorizationProvider;
        this.options = options;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Analyse all the endpoints in the specification.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse()
            .map(() -> Stream.of(analyser.analyseSpecial(), analyser.analyse()).collect(Response.mergeLists()));
    }

    /**
     * Analyse the endpoints for specific entities in the specification.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @param entityNames   a list of entities to be analysed
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification, List<String> entityNames) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse()
            .map(() -> Stream.of(analyser.analyseSpecial(), analyser.analyse(entityNames)).collect(Response.mergeLists()));
    }

    private class Analyser {

        private final OpenAPI openAPI;
        private final OpenApiInteractionValidator validator;
        private final List<APIRequest> requests = new LinkedList<>();

        private final Set<APIRequest> specialRequests = new TreeSet<>(Comparator.comparingInt(APIRequest::getIndex));

        private final Map<String, List<APIRequest>> requestsByEntity;

        private final List<String> unmatchedEndpoints = new ArrayList<>();
        private final Map<String, VariableValue> variableValues = new HashMap<>();

        public Analyser(String specification) {
            ParseOptions parseOptions = new ParseOptions();

            parseOptions.setResolve(true);

            SwaggerParseResult result = new OpenAPIParser().readContents(specification, null, parseOptions);

            validator =
                OpenApiInteractionValidator.createForInlineApiSpecification(specification).build();

            openAPI = result.getOpenAPI();

            openAPI.getPaths().entrySet().forEach(this::cacheRequests);

            requestsByEntity = new TreeMap<>(requests.stream().collect(Collectors.groupingBy(APIRequest::getEntityName)));
        }

        /**
         * Gets a list of endpoints that are not tested under any situation
         *
         * @return a list of endpoints that are not tested under any situation
         */
        public List<String> getUnmatchedEndpoints() {
            return unmatchedEndpoints;
        }

        private Response<List<AnalysisReport>> analyseSpecial() {
            return specialRequests.stream()
                .map(this::executeAPIRequest).collect(Response.toList());
        }

        private Response<List<AnalysisReport>> analyse() {
            return requestsByEntity.entrySet().stream()
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        private Response<List<AnalysisReport>> analyse(List<String> entityNames) {
            List<String> names = entityNames.stream().map(String::toLowerCase).toList();
            return requestsByEntity.entrySet().stream()
                .filter(entry -> names.contains(entry.getKey()))
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        private void cacheRequests(Map.Entry<String, PathItem> pathItemEntry) {

            String endpoint = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            Matcher matcher = ENTITY_PATH_PATTERN.matcher(endpoint);

            if (pathItem.getGet() != null && SPECIAL_CASE_ENDPOINTS.contains(endpoint)) {
                if (COMMON_CROP_NAMES_ENDPOINT.equals(endpoint)) {
                    specialRequests.add(getAPIRequestBuilder("Get " + COMMON_CROP_NAMES_ENDPOINT,
                        SPECIAL_CASE_ENDPOINTS_INDEX, COMMON_CROP_NAMES, pathItem.getGet())
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
                            .build())
                        .build());
                }

                return;
            } else {
                log.debug(String.format("Ignored special case '%s' Endpoint", endpoint));
            }

            if (matcher.matches()) {
                String entityName = StringUtils.capitalise(StringUtils.toSingular(matcher.group(1)));
                String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                if (pathItem.getGet() != null && options.isAnalysingGetEntity(entityName)) {
                    addAPIRequest(getAPIRequestBuilder("Get Entity", GET_ENTITY_INDEX, matcher.group(1), pathItem.getGet())
                        .validatorRequest(SimpleRequest.Builder
                            .get(endpoint)
                            .build())
                        .pathParameter(Parameter.builder()
                            .parameterName(entityIdPropertyName)
                            .variableName(entityIdPropertyName + "1")
                            .build()));
                }

                if (pathItem.getPost() != null) {
                    log.warn(String.format("Ignored POST '%s' Endpoint", endpoint));
                }

                if (pathItem.getPut() != null && options.isAnalysingUpdateEntity(StringUtils.toSingular(matcher.group(1)))) {
                    addAPIRequest(buildUpdateEntityBody(pathItem.getPut().getRequestBody(),
                        getAPIRequestBuilder("Update Entity", UPDATE_ENTITY_INDEX, matcher.group(1), pathItem.getPut())
                            .validatorRequest(SimpleRequest.Builder
                                .put(endpoint)
                                .build())
                            .pathParameter(Parameter.builder()
                                .parameterName(entityIdPropertyName)
                                .variableName(entityIdPropertyName + "1")
                                .build())));
                }

                if (pathItem.getDelete() != null && options.isAnalysingDeleteEntity(StringUtils.toSingular(matcher.group(1)))) {
                    addAPIRequest(getAPIRequestBuilder("Delete", DELETE_ENTITY_INDEX, matcher.group(1), pathItem.getDelete())
                        .validatorRequest(SimpleRequest.Builder
                            .delete(endpoint)
                            .build())
                        .pathParameter(Parameter.builder()
                            .parameterName(entityIdPropertyName)
                            .variableName(entityIdPropertyName + "1")
                            .build()));
                }
            } else {
                matcher = ENTITIES_PATH_PATTERN.matcher(endpoint);

                if (matcher.matches()) {
                    String entityName = StringUtils.capitalise(StringUtils.toSingular(matcher.group(1)));

                    if (pathItem.getGet() != null && options.isAnalysingListEntity(StringUtils.toSingular(matcher.group(1)))) {

                        String entityIdPropertyName = options.getProperties().getIdPropertyNameFor(entityName);

                        addAPIRequest(getAPIRequestBuilder("List", LIST_ENTITY_INDEX, matcher.group(1), pathItem.getGet())
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())
                            .cacheVariable(Variable.builder()
                                .variableName(entityIdPropertyName + "1")
                                .parameterName(entityIdPropertyName)
                                .jsonPath("$.result.data[0]." + entityIdPropertyName)
                                .build()));

                        // TODO get from options other likely parameters, and extract variables for them
                    }

                    if (pathItem.getPost() != null && options.isAnalysingCreateEntity(StringUtils.toSingular(matcher.group(1)))) {

                        String createEntityName = StringUtils.capitalise(StringUtils.toSingular(matcher.group(1)));

                        addAPIRequest(buildCreateEntitiesBody(pathItem.getPost().getRequestBody(),
                            getAPIRequestBuilder("Create", CREATE_ENTITY_INDEX, matcher.group(1), pathItem.getPost())
                                .validatorRequest(SimpleRequest.Builder
                                    .post(endpoint)
                                    .build()))
                            .cacheVariable(Variable.builder()
                                .variableName("new" + createEntityName)
                                .jsonPath("$.result.data[0]")
                                .build()));
                    }

                    if (pathItem.getPut() != null && options.isAnalysingUpdateEntity(StringUtils.toSingular(matcher.group(1)))) {
                        addAPIRequest(buildUpdateEntitiesBody(pathItem.getPut().getRequestBody(),
                            getAPIRequestBuilder("Update(s)", UPDATE_ENTITY_INDEX, matcher.group(1), pathItem.getPut())
                                .validatorRequest(SimpleRequest.Builder
                                    .put(endpoint)
                                    .build())));
                    }

                    if (pathItem.getDelete() != null) {
                        log.warn(String.format("Ignored DELETE '%s' Endpoint", endpoint));
                    }
                } else {
                    matcher = SEARCH_PATH_PATTERN.matcher(endpoint);

                    if (matcher.matches()) {
                        if (pathItem.getGet() != null) {
                            log.warn(String.format("Ignored GET '%s' Endpoint", endpoint));
                        }

                        if (pathItem.getPost() != null && options.isAnalysingSearchEntity(StringUtils.toSingular(matcher.group(1)))) {
                            addAPIRequest(buildSearchBody(pathItem.getPost().getRequestBody(),
                                getAPIRequestBuilder("Search", SEARCH_INDEX, matcher.group(1), pathItem.getPost())
                                    .validatorRequest(SimpleRequest.Builder
                                        .post(endpoint)
                                        .build())));
                        }

                        if (pathItem.getPut() != null) {
                            log.warn(String.format("Ignored Search PUT '%s' Endpoint", endpoint));
                        }

                        if (pathItem.getDelete() != null) {
                            log.warn(String.format("Ignored Search DELETE '%s' Endpoint", endpoint));
                        }
                    } else {
                        matcher = SEARCH_RESULTS_PATH_PATTERN.matcher(endpoint);

                        if (matcher.matches()) {
                            if (pathItem.getGet() != null && options.isAnalysingSearchEntity(StringUtils.toSingular(matcher.group(1)))) {

                                addAPIRequest(getAPIRequestBuilder("Get Search Results", SEARCH_RESULTS_INDEX, matcher.group(1), pathItem.getGet())
                                    .validatorRequest(SimpleRequest.Builder
                                        .get(endpoint)
                                        .build())
                                    .pathParameter(Parameter.builder()
                                        .parameterName("searchResultsDbId")
                                        .variableName("searchResultsDbId1")
                                        .build()));
                            }

                            if (pathItem.getPost() != null) {
                                log.warn(String.format("Ignored POST '%s' Endpoint", endpoint));
                            }

                            if (pathItem.getPut() != null) {
                                log.warn(String.format("Ignored PUT '%s' Endpoint", endpoint));
                            }

                            if (pathItem.getDelete() != null) {
                                log.warn(String.format("Ignored DELETE '%s' Endpoint", endpoint));
                            }
                        } else {
                            unmatchedEndpoints.add(endpoint);
                            log.warn(String.format("Unmatched path '%s'", endpoint));
                        }
                    }
                }
            }
        }

        private APIRequest.APIRequestBuilder buildUpdateEntityBody(RequestBody body, APIRequest.APIRequestBuilder builder) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildCreateEntitiesBody(RequestBody body, APIRequest.APIRequestBuilder builder) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildUpdateEntitiesBody(RequestBody body, APIRequest.APIRequestBuilder builder) {
            // TODO
            return builder;
        }

        private APIRequest.APIRequestBuilder buildSearchBody(RequestBody requestBody, APIRequest.APIRequestBuilder builder) {
            getSchema(requestBody)
                .onFailDoWithResponse(response -> log.warn(response.getMessagesCombined(", ")))
                .mapResultToResponse(this::createSearchBody)
                .onSuccessDoWithResult(builder::body);

            return builder;
        }

        private Response<Object> createSearchBody(Schema schema) {
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

                return Response.success(map);

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
                        return Response.success(schema);
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
                    return Response.success(schema);
                }
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not find schema '%s'", ref));
        }

        private void addAPIRequest(APIRequest.APIRequestBuilder builder) {
            requests.add(builder.build());
        }

        private APIRequest.APIRequestBuilder getAPIRequestBuilder(String name, int index, String pluralName, Operation operation) {
            String entityName = StringUtils.toSingular(pluralName);

            APIRequest.APIRequestBuilder builder = APIRequest.builder()
                .name(name)
                .index(index)
                .entityName(entityName);

            if (options.isPartitionedByCrop()) {
                findParameter("commonCropName", operation.getParameters()).ifPresent(
                    parameter -> addParameter(builder, parameter, "commonCropName")
                );
            }

            return builder;
        }

        private Optional<io.swagger.v3.oas.models.parameters.Parameter> findParameter(String name, List<io.swagger.v3.oas.models.parameters.Parameter> parameters) {
            return parameters.stream().filter(parameter -> parameter.getName().equals(name)).findFirst();
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

        private Response<List<AnalysisReport>> executeAPIRequests(Map.Entry<String, List<APIRequest>> entry) {
            log.debug("Executing requests for {}", entry.getKey());

            return entry.getValue().stream().sorted(Comparator.comparing(APIRequest::getIndex)).map(this::executeAPIRequest).collect(Response.toList());
        }

        private Response<AnalysisReport> executeAPIRequest(APIRequest request) {
            log.debug("Executing request {} for {}", request.getName(), request.getEntityName());

            try {
                HttpRequest.Builder builder = HttpRequest.newBuilder();

                LocalDateTime startTime = LocalDateTime.now();

                return createBody(request)
                    .mapResult(bodyPublisher -> builder.method(request.getValidatorRequest().getMethod().name(), bodyPublisher))
                    .map(authorizationProvider::getAuthorization)
                    .mapResult(authorization -> builder.header("Authorization", authorization))
                    .merge(() -> createURI(builder, request))
                    .mapResult(HttpRequest.Builder::build)
                    .mapResultToResponse(this::send)
                    .mapResultToResponse(httpResponse -> analyse(request, startTime, httpResponse));
            } catch (Exception e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<HttpRequest.Builder> createURI(HttpRequest.Builder builder, APIRequest request) {
            if (request.getPathParameters().isEmpty()) {
                return Response.success(builder.uri(URI.create(String.format("%s%s", baseURL, request.getValidatorRequest().getPath()))));
            } else {
                return request.getPathParameters().stream()
                    .map(this::getVariableValue).collect(Response.toList())
                    .mapResultToResponse(variableValues -> replaceParametersWithVariableValues(request.getValidatorRequest().getPath(), variableValues))
                    .mapResult(path -> builder.uri(URI.create(String.format("%s%s", baseURL, path))));
            }
        }

        private Response<VariableValue> getVariableValue(Parameter parameter) {
            VariableValue value = variableValues.get(parameter.getVariableName());

            if (value != null) {
                return Response.success(value);
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
                return Response.success(path);
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
                return Response.success(HttpRequest.BodyPublishers.noBody());
            }
        }

        private Response<Object> replaceBodyWithVariableValues(Object body) {
            try {
                return Response.success(replaceWithVariableValues(body)) ;
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

            return Response.success(body);
        }

        private Response<HttpRequest.BodyPublisher> writeBody(Object body) {
            try {
                return Response.success(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)));
            } catch (JsonProcessingException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }


        private Response<AnalysisReport> analyse(APIRequest request, LocalDateTime startTime, HttpResponse<String> httpResponse) {

            if (!request.getCacheVariables().isEmpty()) {
                if (httpResponse.statusCode() >= 200 && httpResponse.statusCode() < 300) {

                    try {
                        DocumentContext documentContext = JsonPath.parse(httpResponse.body());
                        request.getCacheVariables().forEach(variable -> cacheVariableValue(documentContext, variable));
                    } catch (Exception e) {
                        return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
                    }
                } else {
                    Response.fail(Response.ErrorType.VALIDATION, String.format("Can not cache value for variables '%s', return code was '%s'",
                        request.getCacheVariables().stream().map(Variable::getVariableName).collect(Collectors.joining(", ")), httpResponse.statusCode()));
                }
            }

            return Response.success(AnalysisReport.builder()
                .request(request)
                .uri(httpResponse.request().uri().toString())
                .startTime(startTime)
                .statusCode(httpResponse.statusCode())
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
            return VariableValue.builder()
                .variableName(variable.getVariableName())
                .parameterName(variable.getParameterName())
                .value(value)
                .build();
        }

        private Response<HttpResponse<String>> send(HttpRequest request) {
            log.debug(String.format("Sending %s %s", request.method(), request.uri()));
            log.debug(String.format("Response body publisher %s", request.bodyPublisher()));
            try {
                return Response.success(client.send(request, HttpResponse.BodyHandlers.ofString()));
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
}