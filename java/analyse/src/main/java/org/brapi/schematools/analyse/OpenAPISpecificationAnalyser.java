package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
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
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Analyses BrAPI endpoints against an OpenAPI Specification
 */
@Slf4j
public class OpenAPISpecificationAnalyser {

    private final String baseURL;
    private final HttpClient client;
    private final AuthorizationProvider authorizationProvider;
    private final AnalysisOptions options ;

    private final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(\\w+)/\\{(\\w+)\\}");
    private final Pattern ENTITIES_PATH_PATTERN = Pattern.compile("/(\\w+)");
    private final Pattern SEARCH_PATH_PATTERN = Pattern.compile("/search/(\\w+)");
    private final Pattern SEARCH_RESULTS_PATH_PATTERN = Pattern.compile("/search/(\\w+/)\\{(\\w+)\\}");

    /**
     * Create an Analyser
     * @param baseURL the base URl for the BrAPI server
     * @param client the HTTP client to use for the execution of requests
     * @param authorizationProvider the authorization provider need for authorization
     * @param options analysis options ;
     */
    public OpenAPISpecificationAnalyser(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider) {
        this(baseURL, client, authorizationProvider, AnalysisOptions.load()) ;
    }
    /**
     * Create an Analyser
     * @param baseURL the base URl for the BrAPI server
     * @param client the HTTP client to use for the execution of requests
     * @param authorizationProvider the authorization provider need for authorization
     * @param options analysis options ;
     */
    public OpenAPISpecificationAnalyser(String baseURL, HttpClient client, AuthorizationProvider authorizationProvider, AnalysisOptions options) {
        this.baseURL = baseURL;
        this.client = client;
        this.authorizationProvider = authorizationProvider;
        this.options = options;
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
            .map(() -> analyser.analyse());
    }

    /**
     * Analyse the endpoints for specific entities in the specification.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @param entityNames a list of entities to be analysed
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification, List<String> entityNames) {

        Analyser analyser = new Analyser(specification);

        return options.validate().asResponse()
            .map(() -> analyser.analyse(entityNames));
    }

    private class Analyser {
        private final OpenAPI openAPI;
        private final OpenApiInteractionValidator validator;
        private final List<APIRequest> requests = new LinkedList<>();

        private Map<String, List<APIRequest>> requestsByEntity = new TreeMap<>();

        private final List<String> unmatchedEndpoints = new ArrayList<>() ;

        public Analyser(String specification) {
            ParseOptions parseOptions = new ParseOptions();

            parseOptions.setResolve(true);

            SwaggerParseResult result = new OpenAPIParser().readContents(specification, null, parseOptions);

            validator =
                OpenApiInteractionValidator.createForInlineApiSpecification(specification).build();

            openAPI = result.getOpenAPI();

            openAPI.getPaths().entrySet().forEach(this::cacheRequests);

            requestsByEntity = new TreeMap<>(requests.stream().collect(Collectors.groupingBy(APIRequest::getEntityName))) ;
        }

        private Response<List<AnalysisReport>> analyse() {
            return requestsByEntity.entrySet().stream()
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        public Response<List<AnalysisReport>> analyse(List<String> entityNames) {
            List<String> names = entityNames.stream().map(String::toLowerCase).toList();
            return requestsByEntity.entrySet().stream()
                .filter(entry -> names.contains(entry.getKey()))
                .map(this::executeAPIRequests).collect(Response.mergeLists());
        }

        private void cacheRequests(Map.Entry<String, PathItem> pathItemEntry) {

            String endpoint = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            Matcher matcher = ENTITY_PATH_PATTERN.matcher(endpoint);

            if (matcher.matches()) {
                if (pathItem.getGet() != null && options.isAnalysingGetEntity(StringUtils.toSingular(matcher.group(1)))) {
                    addAPIRequest(getAPIRequestBuilder("Get", matcher.group(1))
                        .validatorRequest(SimpleRequest.Builder
                            .get(endpoint)
                            .build())
                        .pathParameter(matcher.group(2))) ;
                }

                if (pathItem.getPost() != null) {
                    log.warn(String.format("Ignored POST '%s' Endpoint", endpoint));
                }

                if (pathItem.getPut() != null && options.isAnalysingUpdateEntity(StringUtils.toSingular(matcher.group(1)))) {
                    addAPIRequest(getAPIRequestBuilder("Update", matcher.group(1))
                        .validatorRequest(SimpleRequest.Builder
                            .put(endpoint)
                            .build())
                        .pathParameter(matcher.group(2))) ;
                }

                if (pathItem.getDelete() != null && options.isAnalysingDeleteEntity(StringUtils.toSingular(matcher.group(1)))) {
                    addAPIRequest(getAPIRequestBuilder("Delete", matcher.group(1))
                        .validatorRequest(SimpleRequest.Builder
                            .delete(endpoint)
                            .build())
                        .pathParameter(matcher.group(2))) ;
                }
            } else {
                matcher = ENTITIES_PATH_PATTERN.matcher(endpoint);

                if (matcher.matches()) {
                    if (pathItem.getGet() != null && options.isAnalysingListEntity(StringUtils.toSingular(matcher.group(1)))) {
                        addAPIRequest(getAPIRequestBuilder("List", matcher.group(1))
                            .validatorRequest(SimpleRequest.Builder
                                .get(endpoint)
                                .build())) ;
                    }

                    if (pathItem.getPost() != null && options.isAnalysingCreateEntity(StringUtils.toSingular(matcher.group(1)))) {
                        addAPIRequest(getAPIRequestBuilder("Create", matcher.group(1))
                            .validatorRequest(SimpleRequest.Builder
                                .post(endpoint)
                                .build())) ;
                    }

                    if (pathItem.getPut() != null && options.isAnalysingUpdateEntity(StringUtils.toSingular(matcher.group(1)))) {
                        addAPIRequest(getAPIRequestBuilder("Update(s)", matcher.group(1))
                            .validatorRequest(SimpleRequest.Builder
                                .put(endpoint)
                                .build())) ;
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
                            addAPIRequest(getAPIRequestBuilder("Search", matcher.group(1))
                                .validatorRequest(SimpleRequest.Builder
                                    .post(endpoint)
                                    .build())) ;
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
                                addAPIRequest(getAPIRequestBuilder("Get Search Results", matcher.group(1))
                                    .validatorRequest(SimpleRequest.Builder
                                        .get(endpoint)
                                        .build())
                                    .pathParameter(matcher.group(2))) ;
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

        private void addAPIRequest(APIRequest.APIRequestBuilder builder) {
            requests.add(builder.build()) ;
        }

        private APIRequest.APIRequestBuilder getAPIRequestBuilder(String name, String pluralName) {

            String entityName = StringUtils.toSingular(pluralName);

            return APIRequest.builder()
                .name(name)
                .entityName(entityName) ;
        }

        private Response<List<AnalysisReport>> executeAPIRequests(Map.Entry<String, List<APIRequest>> entry) {
            log.debug("Executing requests for {}", entry.getKey());

            // TODO order and filter

            return entry.getValue().stream().map(this::executeAPIRequest).collect(Response.toList()) ;
        }

        private Response<AnalysisReport> executeAPIRequest(APIRequest request) {
            log.debug("Executing request {} for {}", request.getName(), request.getEntityName());
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s%s", baseURL, createPath(request))))
                .method(request.getValidatorRequest().getMethod().name(), HttpRequest.BodyPublishers.noBody()) ;

            LocalDateTime startTime = LocalDateTime.now() ;

            return authorizationProvider.getAuthorization()
                .mapResult(authorization -> builder.header("Authorization", authorization))
                .mapResult(HttpRequest.Builder::build)
                .mapResultToResponse(this::send)
                .mapResult(response -> analyse(request, startTime, response)) ;
        }

        private String createPath(APIRequest request) {
            return request.getValidatorRequest().getPath() ;
        }

        private AnalysisReport analyse(APIRequest request, LocalDateTime startTime, HttpResponse<String> response) {
            return AnalysisReport.builder()
                .request(request)
                .startTime(startTime)
                .statusCode(response.statusCode())
                .validationReport(validator.validate(request.getValidatorRequest(), createResponse(response)))
                .endTime(LocalDateTime.now())
                .build() ;
        }

        private Response<HttpResponse<String>> send(HttpRequest request) {
            log.debug(String.format("Sending %s %s", request.method(), request.uri()));
            log.debug(String.format("Response body publisher %s", request.bodyPublisher()));
            try {
                return Response.success(client.send(request, HttpResponse.BodyHandlers.ofString())) ;
            } catch (IOException | InterruptedException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private com.atlassian.oai.validator.model.Response createResponse(HttpResponse<String> response) {
            log.debug(String.format("Response was %s for %s", response.statusCode(), response.uri()));
            log.debug(String.format("Response body %s", response.body()));
            // TODO cache some results
            return SimpleResponse.Builder
                .status(response.statusCode())
                .withContentType(response.headers().firstValue("Content-Type").orElse(null))
                .withBody(response.body())
                .build();
        }
    }
}
