package org.brapi.schematools.core.openapi.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.OpenAPIUtils;
import org.brapi.schematools.core.utils.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.OpenAPIUtils.OUTPUT_FORMAT_YAML;
import static org.brapi.schematools.core.utils.OpenAPIUtils.getPrettyObjectWriter;
import static org.brapi.schematools.core.utils.StringUtils.capitalise;

/**
 * Utility class for writing OpenAPI specifications to file.
 */
@AllArgsConstructor
@Slf4j
public class OpenAPIWriter {

    private static final Pattern ENTITY_PATH_PATTERN = Pattern.compile("/(?:search/)?(\\w+)");
    private static final Pattern RESPONSE_PATTERN = Pattern.compile("#/components/responses/(\\w+)");
    private static final Pattern SCHEMA_PATTERN = Pattern.compile("#/components/schemas/(\\w+)");
    private static final Pattern ENTITY_IN_RESPONSE_PATTERN = Pattern.compile("#/components/responses/(\\w+)(?:List|Single)Response");

    private final Path outputPath;
    private final String outputFormat;
    private final boolean generatingIntoSeparateFiles;
    private final Function<Path, Response<PrintWriter>> pathResponseFunction;
    private final String filePattern;

    public OpenAPIWriter(Path outputPath, String outputFormat, boolean generatingIntoSeparateFiles, Function<Path, Response<PrintWriter>> pathResponseFunction) {
        this.outputPath = outputPath;
        this.outputFormat = outputFormat;
        this.generatingIntoSeparateFiles = generatingIntoSeparateFiles;
        this.pathResponseFunction = pathResponseFunction;
        filePattern = outputFormat.equals(OUTPUT_FORMAT_YAML) ? "%s.yaml" : "%s.json";
    }

    public Response<List<Path>> write(List<OpenAPI> specifications) throws IOException {

        return new Writer(specifications).write();
    }

    private class Writer {
        private final List<OpenAPI> specifications ;
        private final Map<String, String> pathToEntityName;
        private final Map<String, Schema> remainingSchemas;
        private final Map<String, Schema> usedSchemas;
        private final Map<String, ApiResponse> remainingResponses;
        private final Map<String, Parameter> parameters ;
        private final Map<String, SecurityScheme> securitySchemes ;
        private final Map<String, Set<String>> referrencedSchemas ;
        private final Map<String, String> schemaModule ;

        private Writer(List<OpenAPI> specifications) {
            this.specifications = specifications;

            pathToEntityName = new HashMap<>();
            remainingSchemas = new HashMap<>();
            usedSchemas = new HashMap<>();
            remainingResponses = new HashMap<>();
            parameters = new HashMap<>();
            securitySchemes = new HashMap<>();
            referrencedSchemas = new HashMap<>();
            schemaModule = new HashMap<>();

            specifications.stream().flatMap(specification -> specification.getPaths().entrySet().stream())
                .forEach(entry -> {

                    PathItem value = entry.getValue();

                    Matcher matcher = ENTITY_PATH_PATTERN.matcher(entry.getKey());

                    if (matcher.find()) {
                        String pathName = matcher.group(1);

                        if (value.getGet() != null && value.getGet().getResponses() != null &&
                            value.getGet().getResponses().get("200") != null && value.getGet().getResponses().get("200").get$ref() != null) {

                            Matcher entityMatcher = ENTITY_IN_RESPONSE_PATTERN.matcher(value.getGet().getResponses().get("200").get$ref());
                            if (entityMatcher.find()) {
                                pathToEntityName.put(pathName, entityMatcher.group(1));
                            }
                        }
                    }
                });

            this.specifications.forEach(specification -> remainingSchemas.putAll(specification.getComponents().getSchemas())) ;
            this.specifications.forEach(specification -> remainingResponses.putAll(specification.getComponents().getResponses())) ;
            this.specifications.forEach(specification -> parameters.putAll(specification.getComponents().getParameters())) ;
            this.specifications.forEach(specification -> securitySchemes.putAll(specification.getComponents().getSecuritySchemes())) ;
        }

        private Response<List<Path>> write() {

            if (outputPath == null) {
                return fail(Response.ErrorType.VALIDATION, "Output path must be defined!");
            }

            if (specifications.isEmpty()) {
                return fail(Response.ErrorType.VALIDATION, "No specification to to output!");
            } else if (specifications.size() == 1) {
                if (Files.isDirectory(outputPath)) {
                    if (generatingIntoSeparateFiles) {
                        return outputOpenAPISpecificationInSeparateFiles(specifications.getFirst(), outputPath, true);
                    } else {
                        return outputOpenAPISpecificationInSingleFile(specifications.getFirst(), resolveOutputPath(specifications.getFirst()));
                    }
                } else if (generatingIntoSeparateFiles) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Output path '%s' must be a directory if outputting to separate files.", outputPath.toFile()));
                } else {
                    return outputOpenAPISpecificationInSingleFile(specifications.getFirst(), outputPath);
                }
            } else if (Files.isRegularFile(outputPath)) {
                return fail(Response.ErrorType.VALIDATION, String.format("Output path '%s' must be a directory if outputting to separate files.", outputPath.toFile()));
            } else if (generatingIntoSeparateFiles) {
                ObjectWriter objectWriter = getPrettyObjectWriter(specifications.getFirst().getSpecVersion(), outputFormat);

                LinkedList<Path> allPaths = new LinkedList<>();

                return specifications.stream()
                    .map(specification -> outputOpenAPISpecificationInSeparateFiles(specification, createDirectoryPath(outputPath, specification.getInfo().getTitle()), false))
                    .collect(Response.mergeLists())
                    .mapResultToResponse(paths -> addRemainingComponents(specifications.getFirst(), objectWriter)
                        .onSuccessDoWithResult(allPaths::addAll))
                    .map(() -> success(allPaths));
            } else {
                return specifications.stream().map(specification -> outputOpenAPISpecificationInSingleFile(specification, resolveOutputPath(specification))).collect(Response.mergeLists());
            }
        }

        private Path resolveOutputPath(OpenAPI specification) {
            return outputPath != null ? outputPath.resolve(
                String.format(outputFormat.equals(OUTPUT_FORMAT_YAML) ? "%s.yaml" : "%s.json", specification.getInfo().getTitle())) : null;
        }

        private Response<List<Path>> outputOpenAPISpecificationInSingleFile(OpenAPI specification, Path outputPath) {
            try {
                String specificationAsString = OpenAPIUtils.prettyPrint(specification, outputFormat);

                return pathResponseFunction.apply(outputPath)
                    .onSuccessDoWithResult(writer -> writer.print(specificationAsString))
                    .onSuccessDoWithResult(PrintWriter::close)
                    .map(() -> success(List.of(outputPath)));

            } catch (JsonProcessingException ex) {
                return fail(Response.ErrorType.VALIDATION, ex.getMessage());
            }
        }

        private Response<List<Path>> outputOpenAPISpecificationInSeparateFiles(OpenAPI specification, Path outputPath, boolean isLast) {
            ObjectWriter objectWriter = getPrettyObjectWriter(specification.getSpecVersion(), outputFormat);

            LinkedList<Path> allPaths = new LinkedList<>();

            return specification.getPaths().entrySet().stream()
                .map(stringPathItemEntry -> outputOpenAPI(specification, stringPathItemEntry, objectWriter, outputPath)).collect(Response.mergeLists())
                .conditionalMapResultToResponse(isLast, paths -> addRemainingComponents(specification, objectWriter)
                    .onSuccessDoWithResult(allPaths::addAll))
                .map(() -> success(allPaths));
        }

        private Response<List<Path>> outputOpenAPI(OpenAPI specification, Map.Entry<String, PathItem> entry, ObjectWriter objectWriter, Path parentPath) {
            LinkedList<Path> paths = new LinkedList<>();
            String key = entry.getKey();
            PathItem value = entry.getValue();

            String entityPathName  = findEntityPathName(key);
            String entityName = this.pathToEntityName.getOrDefault(entityPathName, StringUtils.toSingular(StringUtils.capitalise(entityPathName)));

            String endpointGroup = null ;

            if (entityPathName != null) {
                schemaModule.put(entityName, specification.getInfo().getTitle()) ;

                OpenAPI openAPI = createOpenAPI(specification) ;
                openAPI.path(key, value);

                Set<String> referrencedResponses = new TreeSet<>() ;

                if (value.getGet() != null) {
                    endpointGroup = findEndpointGroup(value.getGet()) ;

                    if (value.getGet().getResponses().get("200") != null && value.getGet().getResponses().get("200").get$ref() != null ) {
                        Matcher matcher = RESPONSE_PATTERN.matcher(value.getGet().getResponses().get("200").get$ref());

                        if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                            referrencedResponses.add(matcher.group(1));
                        }
                    }
                }

                if (value.getPost() != null) {
                    endpointGroup = endpointGroup != null ? endpointGroup : findEndpointGroup(value.getPost()) ;

                    if (value.getPost().getResponses().get("200") != null && value.getPost().getResponses().get("200").get$ref() != null ) {
                        Matcher matcher = RESPONSE_PATTERN.matcher(value.getPost().getResponses().get("200").get$ref());

                        if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                            referrencedResponses.add(matcher.group(1));
                        }
                    }
                }

                if (value.getPut() != null) {
                    endpointGroup = endpointGroup != null ? endpointGroup : findEndpointGroup(value.getPut()) ;

                    if (value.getPut().getResponses().get("200") != null && value.getPut().getResponses().get("200").get$ref() != null ) {
                        Matcher matcher = RESPONSE_PATTERN.matcher(value.getPut().getResponses().get("200").get$ref());

                        if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                            referrencedResponses.add(matcher.group(1));
                        }
                    }
                }

                endpointGroup = endpointGroup != null ? endpointGroup : StringUtils.toPlural(entityName);

                if (!referrencedResponses.isEmpty()) {
                    openAPI.setComponents(new Components());
                }

                referrencedResponses.forEach(ref -> openAPI.getComponents().addResponses(ref, remainingResponses.remove(ref)));

                Set<String> referrencedSchemas = new TreeSet<>() ;

                if (remainingSchemas.containsKey(entityName)) {
                    referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName))) ;
                }

                if (remainingSchemas.containsKey(entityName + "NewRequest")) {
                    referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName + "NewRequest"))) ;
                }

                if (remainingSchemas.containsKey(entityName + "SearchRequest")) {
                    referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName + "SearchRequest"))) ;
                }

                referrencedSchemas.forEach(referrencedSchema -> {
                    Set<String> fromEntity = this.referrencedSchemas.getOrDefault(referrencedSchema, new TreeSet<>());

                    fromEntity.add(entityName) ;
                    this.referrencedSchemas.put(referrencedSchema, fromEntity) ;
                });

                Path path = createDirectoryPath(parentPath, endpointGroup);

                return prettyPrint(openAPI, objectWriter, path.resolve(String.format(filePattern, createFileName(key, value))))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> addEntitySchemas(specification, objectWriter, path, entityName)).onSuccessDoWithResult(paths::addAll)
                    .map(() -> addEntitySchemas(specification, objectWriter, path, entityName + "NewRequest")).onSuccessDoWithResult(paths::addAll)
                    .map(() -> addEntitySchemas(specification, objectWriter, path, entityName + "SearchRequest")).onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths));
            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("Entity name '%s' not found!", key));
            }
        }

        private String findEndpointGroup(Operation operation) {
            if (operation.getTags() != null && !operation.getTags().isEmpty()) {
                return operation.getTags().getFirst().replace(" ", "");
            }
            return null;
        }

        private List<String> findReferrencedSchemas(Schema schema) {
            if (schema instanceof ObjectSchema objectSchema) {
                List<String> referrencedSchemas = new ArrayList<>();
                objectSchema.getProperties().values().stream().map(this::findReferrencedSchemas).forEach(referrencedSchemas::addAll);
                return referrencedSchemas;
            } else if (schema instanceof ArraySchema arraySchema) {
                return findReferrencedSchemas(arraySchema.getItems()) ;
            } else if (schema != null && schema.get$ref() != null)  {
                Matcher matcher = SCHEMA_PATTERN.matcher(schema.get$ref());

                if (matcher.matches()) {
                    return List.of(matcher.group(1));
                }
            } if (schema != null && schema.getEnum() != null && !schema.getEnum().isEmpty() && schema.getName() != null && !schema.getName().isEmpty())  {
                return List.of(schema.getName());
            }

            return Collections.emptyList();
        }

        private Response<List<Path>> addEntitySchemas(OpenAPI specification, ObjectWriter objectWriter, Path path, String entityName) {
            LinkedList<Path> paths = new LinkedList<>();

            if (remainingSchemas.containsKey(entityName)) {
                Path schemasPath = createDirectoryPath(path, "Schemas");
                OpenAPI openAPI = createOpenAPI(specification);
                Schema schema = remainingSchemas.remove(entityName);
                usedSchemas.put(entityName, schema);
                openAPI.setComponents(new Components().addSchemas(entityName, schema));

                prettyPrint(openAPI, objectWriter, schemasPath.resolve(String.format(filePattern, entityName))) ;
            }

            return success(paths);
        }

        private OpenAPI createOpenAPI(OpenAPI specification) {
            OpenAPI openAPI = new OpenAPI();

            openAPI.setOpenapi(specification.getOpenapi());
            openAPI.setSpecVersion(specification.getSpecVersion());
            openAPI.setInfo(specification.getInfo()) ;

            return openAPI ;
        }

        private Path createDirectoryPath(Path parentPath, String directoryName) {
            Path path = parentPath.resolve(directoryName);

            if (path.toFile().mkdirs()) {
                log.debug("Created directory at {}", path);
            } else {
                log.debug("Used existing directory at {}", path);
            }

            return path;
        }

        private String createFileName(String pathName, PathItem pathItem) {
            StringBuilder fileName = new StringBuilder();

            String[] split = pathName.replaceFirst("/", "").split("/");

            for (String string : split) {
                fileName.append(createFileNamePart(string));
                fileName.append("_");
            }

            List<String> methods = new ArrayList<>();

            if (pathItem.getGet() != null) {
                methods.add("GET");
            }

            if (pathItem.getPost() != null) {
                methods.add("POST");
            }

            if (pathItem.getPut() != null) {
                methods.add("PUT");
            }

            if (pathItem.getDelete() != null) {
                methods.add("DELETE");
            }

            if (pathItem.getPatch() != null) {
                methods.add("PATCH");
            }

            fileName.append(String.join("_", methods));

            return fileName.toString();
        }

        private String createFileNamePart(String pathPart) {
            String fileNamePart = pathPart.replaceAll("[{}]", "");

            return capitalise(fileNamePart);
        }

        private Response<List<Path>> prettyPrint(OpenAPI openAPI, ObjectWriter writer, Path outputPath) {
            try {
                String specificationAsString = writer.writeValueAsString(openAPI);

                return pathResponseFunction.apply(outputPath)
                    .onSuccessDoWithResult(printWriter -> printWriter.print(specificationAsString))
                    .onSuccessDoWithResult(PrintWriter::close)
                    .map(() -> success(List.of(outputPath)));

            } catch (JsonProcessingException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private String findEntityPathName(String path) {
            Matcher matcher = ENTITY_PATH_PATTERN.matcher(path);

            if (matcher.find()) {
                return matcher.group(1) ;
            }

            return null;
        }

        private Response<List<Path>> addRemainingComponents(OpenAPI specification, ObjectWriter objectWriter) {
            Path componentPath = createDirectoryPath(outputPath, "Components");
            LinkedList<Path> allRemainingPaths = new LinkedList<>();

            return addRemainingParameters(specification, objectWriter, componentPath).onSuccessDoWithResult(allRemainingPaths::addAll)
                .map(() -> addRemainingResponses(specification, objectWriter, componentPath).onSuccessDoWithResult(allRemainingPaths::addAll))
                .map(() -> addReferrencedSchemas(specification, objectWriter).onSuccessDoWithResult(allRemainingPaths::addAll))
                .map(() -> addRemainingSchemas(specification, objectWriter, componentPath).onSuccessDoWithResult(allRemainingPaths::addAll))
                .map(() -> addRemainingSecuritySchemas(specification, objectWriter, componentPath).onSuccessDoWithResult(allRemainingPaths::addAll))
                .map(() -> success(allRemainingPaths));
        }

        private Response<List<Path>> addRemainingParameters(OpenAPI specification, ObjectWriter objectWriter, Path componentPath) {
            Path parametersPath = createDirectoryPath(componentPath, "Parameters");

            return parameters.entrySet().stream()
                .map(entry -> {
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addParameters(entry.getKey(), entry.getValue()));

                    return prettyPrint(openAPI, objectWriter, parametersPath.resolve(String.format(filePattern, entry.getKey()))) ;
                }).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> addRemainingResponses(OpenAPI specification, ObjectWriter objectWriter, Path componentPath) {
            Path responsesPath = createDirectoryPath(componentPath, "Responses");

            return remainingResponses.entrySet().stream()
                .map(entry -> {
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addResponses(entry.getKey(), entry.getValue()));

                    return prettyPrint(openAPI, objectWriter, responsesPath.resolve(String.format(filePattern, entry.getKey()))) ;
                }).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> addReferrencedSchemas(OpenAPI specification, ObjectWriter objectWriter) {
            return referrencedSchemas.entrySet().stream().filter(entry -> remainingSchemas.containsKey(entry.getKey()) && entry.getValue().size() == 1)
                .map(entry -> {
                    String key = entry.getKey();
                    Path modulePath = entry.getValue().stream().findFirst().map(schemaModule::get).map(module -> createDirectoryPath(outputPath, module)).orElse(outputPath) ;
                    Path entityPath = entry.getValue().stream().findFirst().map(StringUtils::toPlural).map(entityPlural -> createDirectoryPath(modulePath, entityPlural)).orElse(outputPath) ;
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addSchemas(key, remainingSchemas.remove(key)));
                    return prettyPrint(openAPI, objectWriter, createDirectoryPath(entityPath, "Schemas").resolve(String.format(filePattern, key)));
                }).collect(Response.mergeLists());
        }

        private Response<List<Path>> addRemainingSchemas(OpenAPI specification, ObjectWriter objectWriter, Path componentPath) {
            Path schemasPath = createDirectoryPath(componentPath, "Schemas");
            Path searchRequestSchemasPath = createDirectoryPath(schemasPath, "SearchRequestSchemas");

            return remainingSchemas.entrySet().stream()
                .map(entry -> {
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addSchemas(entry.getKey(), entry.getValue()));

                    if (entry.getKey().startsWith("SearchRequestParameters")) {
                        return prettyPrint(openAPI, objectWriter, searchRequestSchemasPath.resolve(String.format(filePattern, entry.getKey()))) ;
                    } else {
                        return prettyPrint(openAPI, objectWriter, schemasPath.resolve(String.format(filePattern, entry.getKey()))) ;
                    }

                }).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> addRemainingSecuritySchemas(OpenAPI specification, ObjectWriter objectWriter, Path componentPath) {
            Path securitySchemesPath = createDirectoryPath(componentPath, "securitySchemes");

            return securitySchemes.entrySet().stream()
                .map(entry -> {
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addSecuritySchemes(entry.getKey(), entry.getValue()));

                    return prettyPrint(openAPI, objectWriter, securitySchemesPath.resolve(String.format(filePattern, entry.getKey()))) ;
                }).collect(Response.mergeLists()) ;
        }
    }
}
