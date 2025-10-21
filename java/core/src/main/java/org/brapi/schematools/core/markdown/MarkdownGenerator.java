package org.brapi.schematools.core.markdown;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPIClassCacheUtil;
import org.brapi.schematools.core.xlsx.XSSFWorkbookGenerator;
import org.brapi.schematools.core.xlsx.options.XSSFWorkbookGeneratorOptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
/**
 * Generates a Markdown files for type and their field descriptions from a BrAPI Json Schema.
 */
@Slf4j
@AllArgsConstructor
public class MarkdownGenerator {
    private final BrAPISchemaReader schemaReader ;
    private final MarkdownGeneratorOptions options ;
    private final Path outputPath ;

    /**
     * Creates a MarkdownGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link MarkdownGeneratorOptions}.
     * @param outputPath the path of the output file or directory
     */
    public MarkdownGenerator(Path outputPath) {
        this(new BrAPISchemaReader(), MarkdownGeneratorOptions.load(), outputPath) ;
    }

    /**
     * Creates a XSSFWorkbookGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link MarkdownGeneratorOptions}.
     * @param options The options to be used in the generation.
     * @param outputPath the path of the output file or directory
     */
    public MarkdownGenerator(MarkdownGeneratorOptions options, Path outputPath) {
        this(new BrAPISchemaReader(), options, outputPath) ;
    }

    /**
     * Generates Markdown files for type and their field descriptions
     * from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI Json schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the paths of the Markdown files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory) {
        return schemaReader.readDirectories(schemaDirectory)
            .mapResultToResponse(brAPISchemas -> new MarkdownGenerator.Generator(brAPISchemas).generate()) ;
    }

    private class Generator {

        private final Map<String, BrAPIClass> brAPIClasses ;
        private final Path descriptionsPath ;
        private final Path fieldsPath ;
        public Generator(List<BrAPIClass> brAPIClasses) {
            this.brAPIClasses = new BrAPIClassCacheUtil(this::isGenerating).createMap(brAPIClasses) ;
            this.descriptionsPath = outputPath.resolve("descriptions") ;
            this.fieldsPath = outputPath.resolve("fields") ;
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(descriptionsPath) ;
                Files.createDirectories(fieldsPath) ;
                return generateMarkdownFiles(new ArrayList<>(brAPIClasses.values())) ;
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass.getMetadata() == null || !(brAPIClass.getMetadata().isRequest() || brAPIClass.getMetadata().isParameters());
        }

        private Response<List<Path>> generateMarkdownFiles(List<BrAPIClass> brAPIClasses) {
            return brAPIClasses.stream()
                .map(this::generateMarkdown)
                .collect(Response.mergeLists());
        }

        private Response<List<Path>> generateMarkdown(BrAPIClass brAPIClass) {
            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                return generateMarkdownForObjectType(brAPIObjectType) ;
            } else if (brAPIClass instanceof BrAPIOneOfType brAPIOneOfType) {
                return generateMarkdownForOneOfType(brAPIOneOfType) ;
            } else if (brAPIClass instanceof BrAPIEnumType brAPIEnumType) {
                return generateMarkdownForEnumType(brAPIEnumType) ;
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", brAPIClass.getName()));
        }

        private Response<List<Path>> generateMarkdownForObjectType(BrAPIObjectType brAPIObjectType) {
            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = descriptionsPath.resolve(String.format("%s.md", brAPIObjectType.getName()));
            Path fieldsPath = this.fieldsPath.resolve(brAPIObjectType.getName());

            try {
                Files.createDirectories(fieldsPath) ;

                return writeToFile(descriptionPath, brAPIObjectType.getDescription())
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> generateMarkdownForProperties(fieldsPath, brAPIObjectType.getProperties()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths)) ;
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateMarkdownForProperties(Path fieldsPath, List<BrAPIObjectProperty> properties) {
            return properties.stream().map(property -> generateMarkdownForProperty(fieldsPath, property)).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> generateMarkdownForProperty(Path path, BrAPIObjectProperty property) {
            Path fieldPath = path.resolve(String.format("%s.md", property.getName()));

            return writeToFile(fieldPath, property.getDescription()) ;
        }

        private Response<List<Path>> generateMarkdownForOneOfType(BrAPIOneOfType brAPIOneOfType) {
            return success(Collections.emptyList()) ;
        }

        private Response<List<Path>> generateMarkdownForEnumType(BrAPIEnumType brAPIEnumType) {

            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = descriptionsPath.resolve(String.format("%s.md", brAPIEnumType.getName()));

            return writeToFile(descriptionPath, createDescription(brAPIEnumType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths)) ;
        }

        private String createDescription(BrAPIEnumType brAPIEnumType) {
            StringBuilder description = new StringBuilder(brAPIEnumType.getDescription() != null ? brAPIEnumType.getDescription() : "");

            description.append("\n\n Possible values are: \n");

            for (BrAPIEnumValue value : brAPIEnumType.getValues()) {
                description
                    .append("* ")
                    .append(value.getName())
                    .append("\n");
            }

            return description.toString();
        }

        private Response<List<Path>> writeToFile(Path path, String text) {
            try {
                if (options.isOverwritingExistingFiles() && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return success(Collections.emptyList()) ;
                } else {
                    PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset()));
                    printWriter.println(text != null ? text : "TODO description");

                    if (options.isAddingGeneratorComments()) {
                        printWriter.println();
                        printWriter.println("<!-- Generated by Schema Tools " + this.getClass().getSimpleName() + " Version: '" + options.getSchemaToolsVersion() + "' ->");
                    }

                    printWriter.close();
                    return success(Collections.singletonList(path)) ;
                }
            } catch (IOException exception){
                return fail(Response.ErrorType.VALIDATION, path, String.format("Can not write to file due to %s", exception.getMessage())) ;
            }
        }
    }
}
