package org.brapi.schematools.core.openapi;

import lombok.AllArgsConstructor;
import org.brapi.schematools.core.openapi.options.OpenAPIComparatorOptions;
import org.brapi.schematools.core.response.Response;

import org.openapitools.openapidiff.core.OpenApiCompare;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.openapitools.openapidiff.core.output.AsciidocRender;
import org.openapitools.openapidiff.core.output.HtmlRender;
import org.openapitools.openapidiff.core.output.MarkdownRender;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Compares two OpenAPI Specifications
 */
@AllArgsConstructor
public class OpenAPIComparator {

    private final OpenAPIComparatorOptions options;

    /**
     * Creates a Comparator with default options
     */
    public OpenAPIComparator() {
        this(OpenAPIComparatorOptions.load()) ;
    }

    /**
     * Compares the OpenAPI Specification in files on a file path and outputs
     * the result in the provided format. If the output file does not exist it
     * will be created along with any missing parent directories.
     *
     * @param firstPath the path of the first Specification
     * @param secondPath the path of the second Specification
     * @param outputPath the path of output file.
     * @param outputFormat the format of the output
     * @return a response with the path of the created output
     */
    public Response<Path> compare(Path firstPath, Path secondPath, Path outputPath, ComparisonOutputFormat outputFormat) {
        if (Files.isRegularFile(firstPath) && Files.isRegularFile(secondPath)) {
            ChangedOpenApi diff = OpenApiCompare.fromFiles(firstPath.toFile(), secondPath.toFile());

            if (outputPath.getParent() != null) {
                try {
                    Files.createDirectories(outputPath.getParent()) ;
                } catch (IOException e) {
                    return Response.fail(Response.ErrorType.VALIDATION,
                        String.format("Parent directory '%s' can not created", outputPath.getParent())) ;
                }
            }
            return switch (outputFormat) {
                case HTML -> renderHtml(diff, outputPath) ;
                case MARKDOWN -> renderMarkdown(diff, outputPath) ;
                case ASCIIDOC -> renderAsciidoc(diff, outputPath) ;
                case JSON -> renderJson(diff, outputPath) ;
            } ;
        } else {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Both input paths need to be regular files, Path 1: '%s' Path 2: '%s'", firstPath, secondPath)) ;
        }
    }

    private Response<Path> renderHtml(ChangedOpenApi diff, Path outputPath) {
        try {
            HtmlRender htmlRender = new HtmlRender("Changelog", "http://deepoove.com/swagger-diff/stylesheets/demo.css");
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
            JsonRender jsonRender = new JsonRender(options.isPrettyPrinting());
            FileOutputStream outputStream = new FileOutputStream(outputPath.toFile());
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            jsonRender.render(diff, outputStreamWriter);
        } catch (FileNotFoundException exception) {
            return Response.fail(Response.ErrorType.VALIDATION,
                String.format("Can not create or use output file '%s'", outputPath)) ;
        }
        return Response.success(outputPath) ;
    }
}
