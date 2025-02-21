package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.OpenApiInteractionValidator;
import com.atlassian.oai.validator.model.Request;
import com.atlassian.oai.validator.model.SimpleRequest;
import com.atlassian.oai.validator.model.SimpleResponse;
import io.swagger.models.HttpMethod;
import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.analyse.query.Endpoint;
import org.brapi.schematools.analyse.query.Endpoints;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Analyses BrAPI endpoints against an OpenAPI Specification
 */
@RequiredArgsConstructor
@Slf4j
public class OpenAPISpecificationAnalyser {

    private final String baseURL;
    private final HttpClient client;
    private final AuthorizationProvider authorizationProvider;

    private final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(\\w+)/\\{(\\w+)\\}");
    private final Pattern ENTITIES_PATH_PATTERN = Pattern.compile("/(\\w+)");
    private final Pattern SEARCH_PATH_PATTERN = Pattern.compile("/search/(\\w+)");
    private final Pattern SEARCH_RESULTS_PATH_PATTERN = Pattern.compile("/search/(\\w+/)\\{(\\w+)\\}");

    /**
     * Analyse all the endpoints in the specification.
     *
     * @param specification the OpenAPI specification to br analysed.
     * @return A response containing a list of AnalysisReports or failure explaining why it failed.
     */
    public Response<List<AnalysisReport>> analyse(String specification) {

        Analyser analyser = new Analyser(specification);

        return analyser.analyse();
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

        return analyser.analyse(entityNames);
    }

    private class Analyser {
        private final OpenAPI openAPI;
        private final OpenApiInteractionValidator validator;
        private final Map<String, Endpoints.EndpointsBuilder> endpointsBuilderMap = new TreeMap<>();
        private Map<String, Endpoints> endpointsMap = new TreeMap<>();

        private final List<String> unmatchedEndpoints = new ArrayList<>() ;

        public Analyser(String specification) {
            ParseOptions parseOptions = new ParseOptions();

            parseOptions.setResolve(true);

            SwaggerParseResult result = new OpenAPIParser().readContents(specification, null, parseOptions);

            validator =
                OpenApiInteractionValidator.createForInlineApiSpecification(specification).build();


            openAPI = result.getOpenAPI();

            openAPI.getPaths().entrySet().forEach(this::cachePath);

            endpointsMap = new TreeMap<>(endpointsBuilderMap.values().stream()
                .map(Endpoints.EndpointsBuilder::build).collect(Collectors.toMap(Endpoints::getEntityName, Function.identity()))) ;
        }

        private Response<List<AnalysisReport>> analyse() {
            return endpointsMap.values().stream()
                .map(this::analyseEndpoints).collect(Response.mergeLists());
        }

        public Response<List<AnalysisReport>> analyse(List<String> entityNames) {
            List<String> names = entityNames.stream().map(String::toLowerCase).toList();
            return endpointsMap.values().stream()
                .filter(endpoints -> names.contains(endpoints.getEntityName()))
                .map(this::analyseEndpoints).collect(Response.mergeLists());
        }

        private void cachePath(Map.Entry<String, PathItem> pathItemEntry) {

            String endpoint = pathItemEntry.getKey();
            PathItem pathItem = pathItemEntry.getValue();

            Matcher matcher = ENTITY_PATH_PATTERN.matcher(endpoint);

            if (matcher.matches()) {
                if (pathItem.getGet() != null) {
                    getEndpointBuilder(matcher.group(1))
                        .singleEndpoint(Endpoint.builder()
                            .path(endpoint)
                            .method(HttpMethod.GET)
                            .build())
                        .idParam(matcher.group(2));
                }

                if (pathItem.getPost() != null) {
                    log.warn(String.format("Ignored POST '%s' Endpoint", endpoint));
                }

                if (pathItem.getPut() != null) {
                    getEndpointBuilder(matcher.group(1))
                        .updateEndpoint(Endpoint.builder()
                            .path(endpoint)
                            .method(HttpMethod.PUT)
                            .build())
                        .idParam(matcher.group(2));
                }

                if (pathItem.getDelete() != null) {
                    getEndpointBuilder(matcher.group(1))
                        .deleteEndpoint(Endpoint.builder()
                            .path(endpoint)
                            .method(HttpMethod.DELETE)
                            .build())
                        .idParam(matcher.group(2));
                }
            } else {
                matcher = ENTITIES_PATH_PATTERN.matcher(endpoint);

                if (matcher.matches()) {
                    if (pathItem.getGet() != null) {
                        getEndpointBuilder(matcher.group(1))
                            .listEndpoint(Endpoint.builder()
                                .path(endpoint)
                                .method(HttpMethod.GET)
                                .build());
                    }

                    if (pathItem.getPost() != null) {
                        getEndpointBuilder(matcher.group(1))
                            .createEndpoint(Endpoint.builder()
                                .path(endpoint)
                                .method(HttpMethod.POST)
                                .build());
                    }

                    if (pathItem.getPut() != null) {
                        getEndpointBuilder(matcher.group(1))
                            .updateEndpoint(Endpoint.builder()
                                .path(endpoint)
                                .method(HttpMethod.PUT)
                                .build());
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

                        if (pathItem.getPost() != null) {
                            getEndpointBuilder(matcher.group(1))
                                .searchEndpoint(Endpoint.builder()
                                    .path(endpoint)
                                    .method(HttpMethod.POST)
                                    .build());
                        }

                        if (pathItem.getPut() != null) {
                            log.warn(String.format("Ignored PUT '%s' Endpoint", endpoint));
                        }

                        if (pathItem.getDelete() != null) {
                            log.warn(String.format("Ignored DELETE '%s' Endpoint", endpoint));
                        }
                    } else {
                        matcher = SEARCH_RESULTS_PATH_PATTERN.matcher(endpoint);

                        if (matcher.matches()) {
                            if (pathItem.getGet() != null) {
                                getEndpointBuilder(matcher.group(1))
                                    .searchResultEndpoint(Endpoint.builder()
                                        .path(endpoint)
                                        .method(HttpMethod.GET)
                                        .build())
                                    .idParam(matcher.group(2));
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

        private Endpoints.EndpointsBuilder getEndpointBuilder(String pluralName) {

            String entityName = StringUtils.toSingular(pluralName);

            Endpoints.EndpointsBuilder builder = this.endpointsBuilderMap.get(entityName);

            if (builder == null) {
                builder = Endpoints.builder().entityName(entityName);
                this.endpointsBuilderMap.put(entityName, builder);
            }

            return builder;
        }

        private Response<List<AnalysisReport>> analyseEndpoints(Endpoints endpoints) {

            List<AnalysisReport> reports = new ArrayList<>();

            if (endpoints.getListEndpoint() != null) {
                return executeEndpoint(endpoints.getListEndpoint())
                    .onSuccessDoWithResult(reports::add)
                    .map(() -> Response.success(reports));

            }

            return Response.success(reports);
        }

        private Request createRequest(Endpoint endpoint) {
            SimpleRequest.Builder builder = switch (endpoint.getMethod()) {
                case DELETE -> SimpleRequest.Builder.delete(endpoint.getPath());
                case GET -> SimpleRequest.Builder.get(endpoint.getPath());
                case HEAD -> SimpleRequest.Builder.head(endpoint.getPath());
                case OPTIONS -> SimpleRequest.Builder.options(endpoint.getPath());
                case PATCH -> SimpleRequest.Builder.patch(endpoint.getPath());
                case POST -> SimpleRequest.Builder.post(endpoint.getPath());
                case PUT -> SimpleRequest.Builder.put(endpoint.getPath());
            };

            return builder.build();
        }

        private Response<AnalysisReport> executeEndpoint(Endpoint endpoint) {

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(String.format("%s%s", baseURL, endpoint.getPath())))
                .method(endpoint.getMethod().name(), HttpRequest.BodyPublishers.noBody()) ;

            LocalDateTime startTime = LocalDateTime.now() ;

            return authorizationProvider.getAuthorization()
                .mapResult(authorization -> builder.header("Authorization", authorization))
                .mapResult(HttpRequest.Builder::build)
                .mapResultToResponse(this::send)
                .mapResult(response -> analyse(endpoint, startTime, response)) ;
        }

        private AnalysisReport analyse(Endpoint endpoint, LocalDateTime startTime, HttpResponse<String> response) {
            return AnalysisReport.builder()
                .endpoint(endpoint)
                .startTime(startTime)
                .statusCode(response.statusCode())
                .validationReport(validator.validate(createRequest(endpoint), createResponse(response)))
                .endTime(LocalDateTime.now())
                .build() ;
        }

        private Response<HttpResponse<String>> send(HttpRequest request) {
            log.debug(String.format("Sending %s %s", request.method(), request.uri()));
            try {
                return Response.success(client.send(request, HttpResponse.BodyHandlers.ofString())) ;
            } catch (IOException | InterruptedException e) {
                return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private com.atlassian.oai.validator.model.Response createResponse(HttpResponse<String> response) {
            log.debug(String.format("Response was %s for %s", response.statusCode(), response.uri()));
            // TODO cache some results
            return SimpleResponse.Builder
                .status(response.statusCode())
                .withContentType(response.headers().firstValue("Content-Type").orElse(null))
                .withBody(response.body())
                .build();
        }
    }
}
