package org.brapi.schematools.core.openapi.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.MediaType;
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
    private static final Pattern ENTITY_IN_RESPONSE_PATTERN = Pattern.compile("(\\w+)(?:List|Single)Response");

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
        private final Map<String, Schema> remainingSchemas;
        private final Map<String, Schema> usedSchemas;
        private final Map<String, ApiResponse> remainingResponses;
        private final Map<String, Parameter> parameters ;
        private final Map<String, SecurityScheme> securitySchemes ;
        private final Map<String, Set<String>> referrencedSchemas ;
        private final Map<String, String> schemaModule ;

        private Writer(List<OpenAPI> specifications) {
            this.specifications = specifications;
            
            remainingSchemas = new TreeMap<>();
            usedSchemas = new HashMap<>();
            remainingResponses = new HashMap<>();
            parameters = new HashMap<>();
            securitySchemes = new HashMap<>();
            referrencedSchemas = new TreeMap<>();
            schemaModule = new HashMap<>();

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
            
            String endpointGroup = null ;
            String entityName = null ;
            
            OpenAPI openAPI = createOpenAPI(specification) ;
            openAPI.path(key, value);

            Set<String> referrencedSchemas = new TreeSet<>() ;

            if (value.getGet() != null) {
                endpointGroup = findEndpointGroup(value.getGet()) ;
                entityName = findEntityName(value.getGet()) ;
                
                referrencedSchemas.addAll(addReferrencedSchemas(value.getGet())) ;
            }

            if (value.getPost() != null) {
                endpointGroup = endpointGroup != null ? endpointGroup : findEndpointGroup(value.getPost()) ;
                entityName = entityName != null ? entityName : findEntityName(value.getPost()) ;
                
                referrencedSchemas.addAll(addReferrencedSchemas(value.getPost())) ;
            }

            if (value.getPut() != null) {
                endpointGroup = endpointGroup != null ? endpointGroup : findEndpointGroup(value.getPut()) ;
                entityName = entityName != null ? entityName : findEntityName(value.getPut()) ;
                
                referrencedSchemas.addAll(addReferrencedSchemas(value.getPut())) ;
            }

            if (value.getDelete() != null) {
                endpointGroup = endpointGroup != null ? endpointGroup : findEndpointGroup(value.getDelete()) ;
                entityName = entityName != null ? entityName : findEntityName(value.getDelete()) ;

                referrencedSchemas.addAll(addReferrencedSchemas(value.getDelete())) ;
            }

            if (!referrencedSchemas.isEmpty()) {
                openAPI.setComponents(new Components());
            }

            referrencedSchemas.forEach(ref -> openAPI.getComponents().addResponses(ref, remainingResponses.remove(ref)));

            if (entityName == null ) {
                return fail(Response.ErrorType.VALIDATION, String.format("Entity name not found for path '%s' !", key));
            }

            if (remainingSchemas.containsKey(entityName)) {
                referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName))) ;
            }

            if (remainingSchemas.containsKey(entityName + "NewRequest")) {
                referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName + "NewRequest"))) ;
            }

            if (remainingSchemas.containsKey(entityName + "SearchRequest")) {
                referrencedSchemas.addAll(findReferrencedSchemas(remainingSchemas.get(entityName + "SearchRequest"))) ;
            }
            
            final String group = endpointGroup != null ? endpointGroup : StringUtils.toPlural(entityName);
            final String name = entityName;
            
            schemaModule.put(group, specification.getInfo().getTitle()) ;

            referrencedSchemas.forEach(referrencedSchema -> addReferrencedSchema(group, referrencedSchema));

            Path path = createDirectoryPath(parentPath, endpointGroup);

            return prettyPrint(openAPI, objectWriter, path.resolve(String.format(filePattern, createFileName(key, value))))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> addEntitySchemas(specification, objectWriter, path, name)).onSuccessDoWithResult(paths::addAll)
                .map(() -> addEntitySchemas(specification, objectWriter, path, name + "NewRequest")).onSuccessDoWithResult(paths::addAll)
                .map(() -> addEntitySchemas(specification, objectWriter, path, name + "SearchRequest")).onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }
        
        private void addReferrencedSchema(String group, String referrencedSchema) {
            Set<String> fromEntity = this.referrencedSchemas.getOrDefault(referrencedSchema, new TreeSet<>());

            fromEntity.add(group) ;
            this.referrencedSchemas.put(referrencedSchema, fromEntity) ;
        }

        private Set<String> addReferrencedSchemas(Operation operation) {
            Set<String> referrencedSchemas = new TreeSet<>() ;

            if (operation.getResponses().get("200") != null) {
                ApiResponse successfulResponse = operation.getResponses().get("200");
                if (successfulResponse.get$ref() != null ) {
                    Matcher matcher = RESPONSE_PATTERN.matcher(successfulResponse.get$ref());

                    if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                        referrencedSchemas.add(matcher.group(1));
                    }
                }

                if (successfulResponse.getContent() != null && successfulResponse.getContent().get("application/json") != null) {
                    Schema schema = successfulResponse.getContent().get("application/json").getSchema();

                    if (schema != null) {
                        if (schema.get$ref() != null) {
                            Matcher matcher = SCHEMA_PATTERN.matcher(schema.get$ref());

                            if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                                referrencedSchemas.add(matcher.group(1));
                            }
                        } else if (schema.getType().equals("object") || schema.getTypes().contains("object")) {
                            if (schema.getProperties().get("result") instanceof Schema resultSchema) {
                                if (resultSchema.get$ref() != null) {
                                    Matcher matcher = SCHEMA_PATTERN.matcher(resultSchema.get$ref());

                                    if (matcher.matches() && remainingSchemas.containsKey(matcher.group(1))) {
                                        referrencedSchemas.add(matcher.group(1));
                                    }
                                } else if (resultSchema.getType().equals("object") || resultSchema.getTypes().contains("object")) {
                                    if (resultSchema.getProperties().get("data") instanceof Schema dataSchema) {
                                        if (dataSchema.get$ref() != null) {
                                            Matcher matcher = SCHEMA_PATTERN.matcher(resultSchema.get$ref());

                                            if (matcher.matches() && remainingSchemas.containsKey(matcher.group(1))) {
                                                referrencedSchemas.add(matcher.group(1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return referrencedSchemas;
        }

        private String findEndpointGroup(Operation operation) {
            if (operation.getTags() != null && !operation.getTags().isEmpty()) {
                return operation.getTags().getFirst().replace(" ", "");
            }
            return null;
        }

        private String findEntityName(Operation operation) {
            String entityName = null;

            if (operation.getResponses() != null && operation.getResponses().get("200") != null) {
                ApiResponse successfulResponse = operation.getResponses().get("200");

                if (successfulResponse.get$ref() != null) {
                    Matcher matcher = RESPONSE_PATTERN.matcher(successfulResponse.get$ref());

                    if (matcher.matches()) {
                        entityName = findEntityNameInResponse(matcher.group(1));
                    }
                }

                if (successfulResponse.getContent() != null && successfulResponse.getContent().get("application/json") != null) {
                    Schema schema = successfulResponse.getContent().get("application/json").getSchema();

                    if (schema != null) {
                        if (schema.get$ref() != null) {
                            Matcher matcher = SCHEMA_PATTERN.matcher(schema.get$ref());

                            if (matcher.matches() && remainingResponses.containsKey(matcher.group(1))) {
                                entityName = findEntityNameInResponse(matcher.group(1));
                            }
                        } else if (schema.getType().equals("object") || schema.getTypes().contains("object")) {
                            if (schema.getProperties().get("result") instanceof Schema resultSchema) {
                                if (resultSchema.get$ref() != null) {
                                    Matcher matcher = SCHEMA_PATTERN.matcher(resultSchema.get$ref());

                                    if (matcher.matches() && remainingSchemas.containsKey(matcher.group(1))) {
                                        entityName = findEntityNameInResponse(matcher.group(1));
                                    }
                                } else if (resultSchema.getType().equals("object") || resultSchema.getTypes().contains("object")) {
                                    if (resultSchema.getProperties().get("data") instanceof Schema dataSchema) {
                                        if (dataSchema.get$ref() != null) {
                                            Matcher matcher = SCHEMA_PATTERN.matcher(resultSchema.get$ref());

                                            if (matcher.matches() && remainingSchemas.containsKey(matcher.group(1))) {
                                                entityName = findEntityNameInResponse(matcher.group(1));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return entityName;
        }

        private String findEntityNameInResponse(String responseEntityName) {
            Matcher matcher = ENTITY_IN_RESPONSE_PATTERN.matcher(responseEntityName);

            if (matcher.matches()) {
                return matcher.group(1);
            }

            return responseEntityName ;
        }

        private List<String> findReferrencedSchemas(Schema schema) {
            List<String> referrencedSchemas = new ArrayList<>();
            if (schema == null) {
                return referrencedSchemas;
            } else if (schema.getAllOf() != null) {
                for (Object child : schema.getAllOf()) {
                    if (child instanceof Schema<?> childSchema) {
                        referrencedSchemas.addAll(findReferrencedSchemas(childSchema)) ;
                    }
                }
            } else if (schema.getOneOf() != null) {
                for (Object child : schema.getOneOf()) {
                    if (child instanceof Schema<?> childSchema) {
                        referrencedSchemas.addAll(findReferrencedSchemas(childSchema)) ;
                    }
                }
            } else if (schema instanceof ObjectSchema objectSchema) {
                objectSchema.getProperties().values().stream().map(this::findReferrencedSchemas).forEach(referrencedSchemas::addAll);
            } else if (schema instanceof ArraySchema arraySchema) {
                referrencedSchemas.addAll(findReferrencedSchemas(arraySchema.getItems())) ;
            } else if (schema.get$ref() != null)  {
                Matcher matcher = SCHEMA_PATTERN.matcher(schema.get$ref());

                if (matcher.matches()) {
                    referrencedSchemas.add(matcher.group(1));
                    referrencedSchemas.addAll(findReferrencedSchemas(this.remainingSchemas.get(matcher.group(1)))) ;
                }
            } if (schema.getEnum() != null && !schema.getEnum().isEmpty() && schema.getName() != null && !schema.getName().isEmpty())  {
                referrencedSchemas.add(schema.getName());
            }

            return referrencedSchemas;
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
                    openAPI.setPaths(new Paths());
                    openAPI.setComponents(new Components().addResponses(entry.getKey(), entry.getValue()));

                    return prettyPrint(openAPI, objectWriter, responsesPath.resolve(String.format(filePattern, entry.getKey()))) ;
                }).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> addReferrencedSchemas(OpenAPI specification, ObjectWriter objectWriter) {
            return referrencedSchemas.entrySet().stream().filter(entry -> remainingSchemas.containsKey(entry.getKey()) && entry.getValue().size() == 1)
                .map(entry -> {
                    String key = entry.getKey();
                    Path modulePath = entry.getValue().stream().findFirst().map(schemaModule::get).map(module -> createDirectoryPath(outputPath, module)).orElse(outputPath) ;
                    Path groupPath = entry.getValue().stream().findFirst().map(path -> createDirectoryPath(modulePath, path)).orElse(outputPath) ;
                    OpenAPI openAPI = createOpenAPI(specification);
                    openAPI.setComponents(new Components().addSchemas(key, remainingSchemas.remove(key)));
                    return prettyPrint(openAPI, objectWriter, createDirectoryPath(groupPath, "Schemas").resolve(String.format(filePattern, key)));
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
