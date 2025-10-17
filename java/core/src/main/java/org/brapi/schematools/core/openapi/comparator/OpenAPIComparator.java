package org.brapi.schematools.core.openapi.comparator;

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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Compares two OpenAPI Specifications
 */
public class OpenAPIComparator {

    private final OpenAPIComparatorOptions options;

    private final List<Pattern> ignoreMissingEndpoints ;

    private final List<Pattern> ignoreNewEndpoints ;

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
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and returns a diff object
     * for downstream use.
     *
     * @param firstPath the path of the first Specification
     * @param secondPath the path of the second Specification
     * @return a response with the diff output
     */
    public Response<ChangedOpenApi> compare(Path firstPath, Path secondPath) {
        if (Files.isRegularFile(firstPath) && Files.isRegularFile(secondPath)) {
            return Response.success(filterDiff(OpenApiCompare.fromFiles(firstPath.toFile(), secondPath.toFile())));
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
        return findActualOutputPath(outputPath, outputFormat)
            .mapResultToResponse(actualOutputPath -> compare(firstPath, secondPath)
                .mapResultToResponse(diff -> renderOutput(diff, actualOutputPath, outputFormat))) ;
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

    private Response<Path> findActualOutputPath(Path outputPath, ComparisonOutputFormat outputFormat) {
        if (outputPath == null) {

            try {
                outputPath = Files.createTempFile(options.getTempFilePrefix(), getTempFileSuffix(outputFormat)) ;
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Parent directory can not created due to '%s'", e.getMessage()));
            }
        }

        if (outputPath.getParent() != null) {
            try {
                Files.createDirectories(outputPath.getParent());
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Parent directory '%s' can not created", outputPath.getParent()));
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

    private Response<Path> renderHtml(ChangedOpenApi diff, Path outputPath) {
        try {
            HtmlRender htmlRender = new HtmlRender(options.getHtml().getTitle(), options.getHtml().getLinkCss(), options.getHtml().isShowingAllChanges());
            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            htmlRender.render(diff, outputStreamWriter);
        } catch (FileNotFoundException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> renderMarkdown(ChangedOpenApi diff, Path outputPath) {
        try {
            MarkdownRender markdownRender = new MarkdownRender();
            markdownRender.setShowChangedMetadata(options.getMarkdown().isShowingChangedMetadata());
            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            markdownRender.render(diff, outputStreamWriter);
        } catch (FileNotFoundException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }
    private Response<Path> renderAsciidoc(ChangedOpenApi diff, Path outputPath) {
        try {
            AsciidocRender asciidocRender = new AsciidocRender();
            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            asciidocRender.render(diff, outputStreamWriter);
        } catch (FileNotFoundException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }

    private Response<Path> renderJson(ChangedOpenApi diff, Path outputPath) {
        try {
            JsonRender jsonRender = new JsonRender(options.getJson().isPrettyPrinting());
            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            jsonRender.render(diff, outputStreamWriter);
        } catch (FileNotFoundException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
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
