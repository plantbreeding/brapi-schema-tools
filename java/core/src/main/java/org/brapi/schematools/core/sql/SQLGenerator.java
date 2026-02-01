package org.brapi.schematools.core.sql;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
/**
 * Generates SQL files for type and their fields from a BrAPI JSON Schema.
 */
@Slf4j
@AllArgsConstructor
public class SQLGenerator {
    private final BrAPISchemaReader schemaReader ;
    private final SQLGeneratorOptions options ;
    private final Path outputPath ;

    public static final String COMMENT_PREFIX = "-- " ;
    public static final String COMMENT_START = "/* " ;
    public static final String COMMENT_END = " */" ;

    /**
     * Creates a SQLGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link SQLGeneratorOptions}.
     * @param outputPath the path of the output file or directory
     */
    public SQLGenerator(Path outputPath) {
        this(new BrAPISchemaReader(), SQLGeneratorOptions.load(), outputPath) ;
    }

    /**
     * Creates a SQLGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link SQLGeneratorOptions}.
     * @param options The options to be used in the generation.
     * @param outputPath the path of the output file or directory
     */
    public SQLGenerator(SQLGeneratorOptions options, Path outputPath) {
        this(new BrAPISchemaReader(), options, outputPath) ;
    }

    /**
     * Generates SQL files for type and their field descriptions
     * from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI JSON schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the paths of the Markdown files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory, SQLGeneratorMetadata metadata) {
        return schemaReader.readDirectories(schemaDirectory)
            .mapResultToResponse(brAPISchemas -> new Generator(brAPISchemas, metadata).generate()) ;
    }

    private class Generator {
        private final CreateTableDDLGenerator createTableDDLGenerator ;
        private final List<BrAPIClass> brAPIClasses;

        public Generator(List<BrAPIClass> brAPIClasses, SQLGeneratorMetadata metadata) {
            // TODO other dialects
            createTableDDLGenerator = new ANSICreateTableDDLGenerator(options, metadata, brAPIClasses) ;
            this.brAPIClasses = brAPIClasses.stream().filter(this::isGenerating).collect(Collectors.toList());
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(outputPath) ;
                return generateSQLFiles(brAPIClasses) ;
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass.getMetadata() != null &&
                brAPIClass.getMetadata().isPrimaryModel() && options.isGeneratingFor(brAPIClass);
        }

        private Response<List<Path>> generateSQLFiles(List<BrAPIClass> brAPIClasses) {
            return brAPIClasses.stream()
                .map(this::generateSQL)
                .collect(Response.toList());
        }

        private Response<Path> generateSQL(BrAPIClass brAPIClass) {
            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                return createTableDDLGenerator.generateDDLForObjectType(brAPIObjectType)
                    .mapResultToResponse(sql -> writeToFile(outputPath.resolve(String.format("%s.sql", brAPIClass.getName())), brAPIObjectType, sql));
            }

            return fail(Response.ErrorType.VALIDATION, String.format("Unknown supported type '%s'", brAPIClass.getName()));
        }

        private Response<Path> writeToFile(Path path, BrAPIObjectType brAPIObjectType, String text) {
            try {
                if (!options.isOverwritingExistingFiles() && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return Response.empty() ;
                } else {
                    Files.createDirectories(path.getParent()) ;

                    PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset()));

                    printWriter.print(COMMENT_PREFIX) ;
                    printWriter.println(brAPIObjectType.getName());

                    if (options.isAddingDropTable()) {
                        printWriter.println(COMMENT_START);

                        if (brAPIObjectType.getDescription() != null) {
                            printWriter.println(brAPIObjectType.getDescription());
                        } else {
                            printWriter.println(options.getDescriptionFor(brAPIObjectType));
                        }

                        printWriter.println(COMMENT_END);
                    }

                    printWriter.println(text);

                    if (options.isAddingGeneratorComments()) {
                        printWriter.println();
                        printWriter.print(COMMENT_PREFIX) ;
                        printWriter.println("Generated by Schema Tools " + this.getClass().getSimpleName() + " Version: '" + options.getSchemaToolsVersion() +"'");
                    }

                    printWriter.close();
                    return success(path) ;
                }
            } catch (IOException exception){
                return fail(Response.ErrorType.VALIDATION, path, String.format("Can not write to file due to %s", exception.getMessage())) ;
            }
        }
    }
}
