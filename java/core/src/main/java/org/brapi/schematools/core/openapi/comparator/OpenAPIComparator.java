package org.brapi.schematools.core.openapi.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.spi.json.JacksonJsonNodeJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import org.brapi.schematools.core.openapi.comparator.options.OpenAPIComparatorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.compare.OpenApiDiffOptions;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.model.ChangedOperation;
import org.openapitools.openapidiff.core.model.ChangedSchema;
import org.openapitools.openapidiff.core.model.Endpoint;
import org.openapitools.openapidiff.core.output.AsciidocRender;
import org.openapitools.openapidiff.core.output.HtmlRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Compares two OpenAPI Specifications
 */
public class OpenAPIComparator {

    private static final List<String> DESCRIPTION_FIELDS = List.of("description", "summary");

    private final OpenAPIComparatorOptions options;
    private final ObjectMapper mapper;
    private final List<Pattern> ignoreMissingEndpoints ;
    private final List<Pattern> ignoreNewEndpoints ;
    private final ObjectWriter writer;
    private final Configuration jsonpathConfig;

    /**
     * Creates a Comparator with default options
     */
    public OpenAPIComparator() {
        this(OpenAPIComparatorOptions.load()) ;
    }

    public OpenAPIComparator(OpenAPIComparatorOptions options) {
        this.options = options;

        ignoreMissingEndpoints = options.getIgnoreMissingEndpoints() != null ?
            options.getIgnoreMissingEndpoints().stream().map(Pattern::compile).toList() : new ArrayList<>() ;

        ignoreNewEndpoints = options.getIgnoreNewEndpoints() != null ?
            options.getIgnoreNewEndpoints().stream().map(Pattern::compile).toList() : new ArrayList<>() ;

        mapper = new ObjectMapper();
        if (options.getJson().isPrettyPrinting()) {
            writer = mapper.writerWithDefaultPrettyPrinter();
        } else {
            writer = mapper.writer();
        }

        jsonpathConfig = Configuration.builder()
            .jsonProvider(new JacksonJsonNodeJsonProvider())
            .mappingProvider(new JacksonMappingProvider())
            .build();
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and returns a diff object
     * for downstream use.
     *
     * @param firstPath the path of the first Specification
     * @param secondPath the path of the second Specification
     * @return a response with the diff output
     */
    public Response<ChangedOpenApi> openApiCompare(Path firstPath, Path secondPath) {
        if (Files.isRegularFile(firstPath) && Files.isRegularFile(secondPath)) {
            try {
                if (needsPreprocess()) {
                    String firstContent = preprocessFromPath(firstPath);
                    String secondContent = preprocessFromPath(secondPath);
                    return Response.success(filterDiff(OpenApiCompare.fromContents(firstContent, secondContent, null, createOptions())));
                }
                return Response.success(filterDiff(OpenApiCompare.fromFiles(firstPath.toFile(), secondPath.toFile())));
            } catch (Exception e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Can not compare, Path 1: '%s' Path 2: '%s' due to exception: %s", firstPath, secondPath, e.getMessage()));
            }
        } else {
            if (!Files.isRegularFile(firstPath) && !Files.isRegularFile(secondPath)) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Both input paths need to be regular files, Path 1: '%s' Path 2: '%s'", firstPath, secondPath));
            } else {
                if (!Files.isRegularFile(firstPath)) {
                    return Response.fail(Response.ErrorType.VALIDATION,
                        String.format("First input path is not a regular file, Path 1: '%s'", firstPath));
                } else {
                    return Response.fail(Response.ErrorType.VALIDATION,
                        String.format("Second input path is not a regular file, Path 1: '%s'", secondPath));
                }
            }
        }
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and returns a diff object
     * for downstream use.
     *
     * @param firstPath the path of the first Specification
     * @param secondPath the path of the second Specification
     * @return a response with the diff output
     */
    public Response<JsonNode> jsonDiff(Path firstPath, Path secondPath) {
        if (Files.isRegularFile(firstPath) && Files.isRegularFile(secondPath)) {
            try {
                boolean isYaml = firstPath.getFileName().toString().endsWith(".yaml") || secondPath.getFileName().toString().endsWith(".yaml");
                if (needsPreprocess()) {
                    Path strippedFirst = writePreprocessed(firstPath);
                    Path strippedSecond = writePreprocessed(secondPath);
                    try {
                        // preprocessed files are always written as JSON
                        return Response.success(filterDiff(JsonDiffCompare.fromFilesJSON(strippedFirst, strippedSecond)));
                    } finally {
                        Files.deleteIfExists(strippedFirst);
                        Files.deleteIfExists(strippedSecond);
                    }
                }
                JsonNode diff = isYaml
                    ? JsonDiffCompare.fromFilesYAML(firstPath, secondPath)
                    : JsonDiffCompare.fromFilesJSON(firstPath, secondPath);
                return Response.success(filterDiff(diff));
            } catch (Exception e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Can not compare, Path 1: '%s' Path 2: '%s' due to exception: %s", firstPath, secondPath, e.getMessage()));
            }
        } else {
            if (!Files.isRegularFile(firstPath) && !Files.isRegularFile(secondPath)) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Both input paths need to be regular files, Path 1: '%s' Path 2: '%s'", firstPath, secondPath));
            } else {
                if (!Files.isRegularFile(firstPath)) {
                    return Response.fail(Response.ErrorType.VALIDATION,
                        String.format("First input path is not a regular file, Path 1: '%s'", firstPath));
                } else {
                    return Response.fail(Response.ErrorType.VALIDATION,
                        String.format("Second input path is not a regular file, Path 1: '%s'", secondPath));
                }
            }
        }
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and outputs
     * the result in the provided format.
     * If the output file does not exist, it
     * will be created along with any missing parent directories.
     *
     * @param firstPath the path of the first Specification
     * @param secondPath the path of the second Specification
     * @param outputPath the path of output file.
     * @param outputFormat the format of the output
     * @return a response with the path of the created output
     */
    public Response<Path> compare(Path firstPath, Path secondPath, Path outputPath, ComparisonOutputFormat outputFormat) {
        if (!options.getComparisonAPI().equals("OpenApiCompare") && !options.getComparisonAPI().equals("JsonDiff")) {
            return Response.fail(Response.ErrorType.VALIDATION, "Unsupported comparison API: " + options.getComparisonAPI());
        }

        return findActualOutputPath(outputPath, outputFormat)
            .conditionalMapResultToResponse(options.getComparisonAPI().equals("OpenApiCompare"), actualOutputPath -> openApiCompareAndOutput(firstPath, secondPath, actualOutputPath, outputFormat))
            .conditionalMapResultToResponse(options.getComparisonAPI().equals("JsonDiff"), actualOutputPath -> jsonDiffAndOutput(firstPath, secondPath, actualOutputPath, outputFormat)) ;
    }

    private Response<Path> openApiCompareAndOutput(Path firstPath, Path secondPath, Path outputPath, ComparisonOutputFormat outputFormat) {
        return openApiCompare(firstPath, secondPath).mapResultToResponse(diff -> renderOutput(diff, outputPath, outputFormat)) ;
    }

    private Response<Path> jsonDiffAndOutput(Path firstPath, Path secondPath, Path outputPath, ComparisonOutputFormat outputFormat) {
        return jsonDiff(firstPath, secondPath).mapResultToResponse(diff -> renderOutput(diff, outputPath, outputFormat)) ;
    }

    /**
     * Compares the OpenAPI Specification in a string on a file path and outputs
     * the result in the provided format and returns a diff object
     * for downstream use.
     *
     * @param firstContent the content of the first Specification
     * @param secondContent the content of the second Specification
     * @return a response with the diff output
     */
    public Response<ChangedOpenApi> compare(String firstContent, String secondContent) {
        if (StringUtils.isNotBlank(firstContent) && StringUtils.isNotBlank(secondContent)) {
            try {
                if (needsPreprocess()) {
                    firstContent = preprocessFromString(firstContent);
                    secondContent = preprocessFromString(secondContent);
                }
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION, "Failed to preprocess OpenAPI content: " + e.getMessage());
            }
            return Response.success(filterDiff(OpenApiCompare.fromContents(firstContent, secondContent, null, createOptions()))) ;
        } else {
            if (!StringUtils.isNotBlank(firstContent) && !StringUtils.isNotBlank(secondContent)) {
                return Response.fail(Response.ErrorType.VALIDATION, "Both input content need to be non blank");
            } else {
                if (!StringUtils.isNotBlank(firstContent)) {
                    return Response.fail(Response.ErrorType.VALIDATION, "First input content need to be non blank");
                } else {
                    return Response.fail(Response.ErrorType.VALIDATION, "Second input content need to be non blank");
                }
            }
        }
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and a diff object
     * for downstream use.
     *
     * @param firstContent the content of the first Specification
     * @param secondContent the content of the second Specification
     * @param outputPath the path of output file.
     * @param outputFormat the format of the output
     * @return a response with the path of the created output
     */
    public Response<Path> compare(String firstContent, String secondContent, Path outputPath, ComparisonOutputFormat outputFormat) {
        return findActualOutputPath(outputPath, outputFormat)
            .mapResultToResponse(actualOutputPath -> compare(firstContent, secondContent)
                .mapResultToResponse(diff -> renderOutput(diff, actualOutputPath, outputFormat)));
    }

    private OpenApiDiffOptions createOptions() {
        return OpenApiDiffOptions.builder().build();
    }

    private boolean needsPreprocess() {
        return options.isIgnoringDescriptions() || options.isNormalizingNullableSchemas();
    }

    /**
     * Applies configured in-place pre-processing (description stripping and/or nullable schema
     * normalisation) to a loaded OpenAPI document tree.
     */
    private void preprocess(JsonNode node) {
        if (options.isNormalizingNullableSchemas()) {
            normalizeNullableSchemas(node);
        }
        if (options.isIgnoringDescriptions()) {
            stripDescriptions(node);
        }
    }

    /**
     * Recursively removes all description and summary fields from a JSON node (in-place).
     */
    private void stripDescriptions(JsonNode node) {
        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;
            DESCRIPTION_FIELDS.forEach(obj::remove);
            Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
            while (fields.hasNext()) {
                stripDescriptions(fields.next().getValue());
            }
        } else if (node.isArray()) {
            node.forEach(this::stripDescriptions);
        }
    }

    /**
     * Recursively rewrites equivalent nullable schema encodings to a canonical form so that
     * openapi-diff does not report false type changes such as {@code object -> null}.
     * <p>
     * Canonical form for a simple nullable reference/schema:
     * {@code { "allOf": [ &lt;non-null schema&gt; ], "nullable": true }}
     * <p>
     * Recognised source encodings:
     * <ul>
     *   <li>{@code anyOf}/{@code oneOf}: exactly one non-null member plus a pure {@code {type: null}} member</li>
     *   <li>{@code type: ["object","null"]} (or other single non-null type + null)</li>
     *   <li>bare {@code $ref} with sibling {@code nullable: true} (invalid OpenAPI 3.0, still seen in the wild)</li>
     * </ul>
     * Multi-member unions and genuine non-nullable schemas are left unchanged.
     */
    private void normalizeNullableSchemas(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            ObjectNode obj = (ObjectNode) node;

            // Recurse into children first so nested schemas are canonical before parent rewrite.
            Iterator<Map.Entry<String, JsonNode>> fields = obj.fields();
            List<JsonNode> children = new ArrayList<>();
            while (fields.hasNext()) {
                children.add(fields.next().getValue());
            }
            children.forEach(this::normalizeNullableSchemas);

            rewriteNullableComposedSchema(obj, "anyOf");
            rewriteNullableComposedSchema(obj, "oneOf");
            rewriteNullableTypeArray(obj);
            rewriteBareNullableRef(obj);
        } else if (node.isArray()) {
            node.forEach(this::normalizeNullableSchemas);
        }
    }

    private void rewriteNullableComposedSchema(ObjectNode obj, String composedKeyword) {
        JsonNode composed = obj.get(composedKeyword);
        if (!(composed instanceof ArrayNode arrayNode) || arrayNode.size() < 2) {
            return;
        }

        JsonNode nonNullMember = null;
        int nullMemberCount = 0;
        int nonNullMemberCount = 0;

        for (JsonNode member : arrayNode) {
            if (isPureNullSchema(member)) {
                nullMemberCount++;
            } else {
                nonNullMemberCount++;
                nonNullMember = member;
            }
        }

        if (nullMemberCount != 1 || nonNullMemberCount != 1 || nonNullMember == null) {
            return;
        }

        // Preserve sibling keywords (description, example, etc.) and replace the composed union.
        obj.remove(composedKeyword);
        ArrayNode allOf = obj.putArray("allOf");
        allOf.add(nonNullMember.deepCopy());
        obj.set("nullable", BooleanNode.TRUE);

        // Drop an explicit type:null leftover if present on the parent from partial merges.
        if (obj.has("type") && obj.get("type").isTextual() && "null".equals(obj.get("type").asText())) {
            obj.remove("type");
        }
    }

    private void rewriteNullableTypeArray(ObjectNode obj) {
        JsonNode typeNode = obj.get("type");
        if (!(typeNode instanceof ArrayNode typeArray) || typeArray.size() < 2) {
            return;
        }

        String nonNullType = null;
        boolean hasNull = false;
        int nonNullCount = 0;

        for (JsonNode entry : typeArray) {
            if (!entry.isTextual()) {
                return;
            }
            String value = entry.asText();
            if ("null".equals(value)) {
                hasNull = true;
            } else {
                nonNullCount++;
                nonNullType = value;
            }
        }

        if (!hasNull || nonNullCount != 1 || nonNullType == null) {
            return;
        }

        obj.set("type", TextNode.valueOf(nonNullType));
        obj.set("nullable", BooleanNode.TRUE);
    }

    private void rewriteBareNullableRef(ObjectNode obj) {
        if (!obj.has("$ref") || !obj.path("nullable").asBoolean(false)) {
            return;
        }
        // OpenAPI 3.0 disallows siblings of $ref; wrap so nullable is preserved consistently.
        if (obj.has("allOf") || obj.has("anyOf") || obj.has("oneOf")) {
            return;
        }

        String ref = obj.get("$ref").asText();
        obj.remove("$ref");
        ArrayNode allOf = obj.putArray("allOf");
        ObjectNode refNode = mapper.createObjectNode();
        refNode.put("$ref", ref);
        allOf.add(refNode);
        obj.set("nullable", BooleanNode.TRUE);
    }

    private boolean isPureNullSchema(JsonNode node) {
        if (node == null || !node.isObject()) {
            return false;
        }
        ObjectNode obj = (ObjectNode) node;

        JsonNode typeNode = obj.get("type");
        boolean typeIsNull = typeNode != null && typeNode.isTextual() && "null".equals(typeNode.asText());
        if (!typeIsNull) {
            return false;
        }

        // Allow only null-type marker fields; anything else is a richer schema.
        Iterator<String> fieldNames = obj.fieldNames();
        while (fieldNames.hasNext()) {
            String name = fieldNames.next();
            if (!"type".equals(name) && !"nullable".equals(name) && !"description".equals(name) && !"title".equals(name)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads a JSON or YAML file, applies pre-processing, and returns the JSON string.
     * Used for OpenApiCompare (fromContents) where format conversion is safe.
     */
    private String preprocessFromPath(Path sourcePath) throws IOException {
        JsonNode node = readAsJsonNode(sourcePath);
        preprocess(node);
        return mapper.writeValueAsString(node);
    }

    /**
     * Loads a JSON or YAML file, applies pre-processing, writes to a temp JSON file, and returns its path.
     * Used for JsonDiff (fromFilesJSON) where temp files are needed.
     */
    private Path writePreprocessed(Path sourcePath) throws IOException {
        JsonNode node = readAsJsonNode(sourcePath);
        preprocess(node);
        Path tmp = Files.createTempFile("brapi-compare-", ".json");
        mapper.writeValue(tmp.toFile(), node);
        return tmp;
    }

    private JsonNode readAsJsonNode(Path sourcePath) throws IOException {
        if (sourcePath.getFileName().toString().endsWith(".yaml")) {
            return new com.fasterxml.jackson.dataformat.yaml.YAMLMapper().readTree(sourcePath.toFile());
        } else {
            return mapper.readTree(sourcePath.toFile());
        }
    }

    /**
     * Parses a JSON or YAML string, applies pre-processing, and returns the modified JSON string.
     */
    private String preprocessFromString(String content) throws IOException {
        // YAMLMapper can parse both YAML and JSON
        JsonNode node = new com.fasterxml.jackson.dataformat.yaml.YAMLMapper().readTree(content);
        preprocess(node);
        return mapper.writeValueAsString(node);
    }

    private ChangedOpenApi filterDiff(ChangedOpenApi changedOpenApi) {
        return new ChangedOpenApi(createOptions())
            .setChangedExtensions(changedOpenApi.getChangedExtensions()) // TODO
            .setChangedOperations(changedOpenApi.getChangedOperations().stream().filter(this::keepChangedOperation).toList())
            .setChangedSchemas(changedOpenApi.getChangedSchemas().stream().filter(this::keepChangedSchema).toList())
            .setMissingEndpoints(changedOpenApi.getMissingEndpoints().stream().filter(this::keepMissingEndpoint).toList())
            .setNewEndpoints(changedOpenApi.getNewEndpoints().stream().filter(this::keepNewEndpoint).toList())
            .setNewSpecOpenApi(changedOpenApi.getNewSpecOpenApi())
            .setOldSpecOpenApi(changedOpenApi.getOldSpecOpenApi()) ;
    }

    private JsonNode filterDiff(JsonNode diff) {
        DocumentContext jsonContext = JsonPath.using(jsonpathConfig).parse(diff);
        jsonContext.delete("$.[?(@.op == 'replace' && @.path == '/info/title')]");
        jsonContext.delete("$.[?(@.op == 'replace' && @.path == '/info/version')]");
        return jsonContext.json() ;
    }

    private Response<Path> findActualOutputPath(Path outputPath, ComparisonOutputFormat outputFormat) {
        if (outputPath == null) {

            try {
                outputPath = Files.createTempFile(options.getTempFilePrefix(), getTempFileSuffix(outputFormat)) ;
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Parent directory cannot created due to '%s'", e.getMessage()));
            }
        }

        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Parent directory '%s' cannot be created", outputPath.getParent()));
            }
        }

        return Response.success(outputPath) ;
    }

    private String getTempFileSuffix(ComparisonOutputFormat outputFormat) {
        return switch (outputFormat) {
            case HTML -> "html";
            case MARKDOWN -> "md";
            case ASCIIDOC -> "txt";
            case JSON -> "json";
        } ;
    }

    private Response<Path> renderOutput(ChangedOpenApi diff, Path outputPath, ComparisonOutputFormat outputFormat) {
        return switch (outputFormat) {
            case HTML -> renderHtml(diff, outputPath);
            case MARKDOWN -> renderMarkdown(diff, outputPath);
            case ASCIIDOC -> renderAsciidoc(diff, outputPath);
            case JSON -> renderJson(diff, outputPath);
        } ;
    }

    private Response<Path> renderOutput(JsonNode diff, Path outputPath, ComparisonOutputFormat outputFormat) {
        return switch (outputFormat) {
            case HTML, MARKDOWN, ASCIIDOC -> Response.fail(Response.ErrorType.VALIDATION,
                String.format("Unsupported output format '%s'", outputFormat)) ;
            case JSON -> outputJson(diff, outputPath);
        } ;
    }

    private OutputStreamWriter openWriter(Path outputPath) throws IOException {
        return new OutputStreamWriter(
            Files.newOutputStream(outputPath,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.TRUNCATE_EXISTING,
                java.nio.file.StandardOpenOption.WRITE));
    }

    private Response<Path> renderHtml(ChangedOpenApi diff, Path outputPath) {
        try {
            HtmlRender htmlRender = new HtmlRender(options.getHtml().getTitle(), options.getHtml().getLinkCss(), options.getHtml().isShowingAllChanges());
            htmlRender.render(diff, openWriter(outputPath));
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> renderMarkdown(ChangedOpenApi diff, Path outputPath) {
        try {
            OutputStreamWriter outputStreamWriter = openWriter(outputPath);
            if (options.getMarkdown().isShowingChangedMetadata()) {
                new DetailedMarkdownRender().render(diff, outputStreamWriter);
            } else {
                MarkdownRender markdownRender = new MarkdownRender();
                markdownRender.setShowChangedMetadata(false);
                markdownRender.render(diff, outputStreamWriter);
            }
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> renderAsciidoc(ChangedOpenApi diff, Path outputPath) {
        try {
            AsciidocRender asciidocRender = new AsciidocRender();
            asciidocRender.render(diff, openWriter(outputPath));
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> renderJson(ChangedOpenApi diff, Path outputPath) {
        try {
            JsonRender jsonRender = new JsonRender(options.getJson().isPrettyPrinting());
            jsonRender.render(diff, openWriter(outputPath));
        } catch (IOException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> outputJson(JsonNode diff, Path outputPath) {
        try {
            String prettyJson = writer.writeValueAsString(diff);
            Files.writeString(outputPath, prettyJson) ;
            return Response.success(outputPath) ;
        } catch (IOException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not output to file '%s' due to %s", outputPath, e.getMessage())) ;
        }
    }

    private boolean keepChangedOperation(ChangedOperation changedOperation) {
        return true ;
    }

    private boolean keepChangedSchema(ChangedSchema changedSchema) {
        return true ;
    }

    private boolean keepMissingEndpoint(Endpoint endpoint) {
        if (options.isIgnoringDeprecatedEndpoints() && endpoint.getOperation().getDeprecated() != null && endpoint.getOperation().getDeprecated()) {
            return false ;
        }

        return ignoreMissingEndpoints.stream().noneMatch(pattern -> pattern.matcher(endpoint.getPathUrl()).matches()) ;
    }

    private boolean keepNewEndpoint(Endpoint endpoint) {
        return ignoreNewEndpoints.stream().noneMatch(pattern -> pattern.matcher(endpoint.getPathUrl()).matches()) ;
    }
}
