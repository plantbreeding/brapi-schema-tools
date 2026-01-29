package org.brapi.schematools.cli;

import org.brapi.schematools.core.openapi.comparator.ComparisonOutputFormat;
import org.brapi.schematools.core.openapi.comparator.OpenAPIComparator;
import org.brapi.schematools.core.openapi.comparator.options.OpenAPIComparatorOptions;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The Compare Sub-command
 */
@CommandLine.Command(
    name = "compare", mixinStandardHelpOptions = true,
    description = "Compares various types of BrAPI, including OpenAPI Specification or GraphQL Schema"
)
public class CompareSubCommand extends AbstractSubCommand {
    private static final List<String> IGNORE_PATHS = List.of("BrAPI-Schema", "OpenAPI-Components", "Generated", "swaggerMetaData.yaml");

    private PrintWriter out ;

    private static final InputFormat DEFAULT_INPUT_FORMAT = InputFormat.OPEN_API;
    private static final ComparisonOutputFormat DEFAULT_OUTPUT_FORMAT = ComparisonOutputFormat.MARKDOWN;

    @CommandLine.Parameters(index = "0", description = "The file or directory containing the first BrAPI Specification/Schema")
    private Path firstPath;

    @CommandLine.Parameters(index = "1", description = "The file or directory containing the second BrAPI Specification/Schema")
    private Path secondPath;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "OPEN_API", fallbackValue = "OPEN_API", description = "The format of the Input. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_INPUT_FORMAT}")
    private InputFormat inputFormat = DEFAULT_INPUT_FORMAT;

    @CommandLine.Option(names = {"-w", "--output"}, defaultValue = "MARKDOWN", fallbackValue = "MARKDOWN", description = "The format of the Output. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_OUTPUT_FORMAT}")
    private ComparisonOutputFormat outputFormat = DEFAULT_OUTPUT_FORMAT;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The path of the output file or directory for the generated result. If omitted the output will be written to the standard out")
    private Path outputPath;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over writen.")
    private boolean overwrite = true;

    @CommandLine.Option(names = {"-p", "--prettyprint"}, description = "Pretty print the JSON output if possible. True by default.")
    private boolean prettyprint = true;

    @CommandLine.Option(names = {"-k", "--comparisonAPI"}, description = "Comparison API to use. Options are 'OpenApiCompare' and 'JsonDiff' Default is OpenApiCompare.")
    private String comparisonAPI = "OpenApiCompare";

    @CommandLine.Option(names = {"-c", "--components"}, description = "The directory containing the OpenAPI Components, required for the OPEN_API input format")
    private Path componentsDirectory;

    @Override
    public void execute() throws IOException {
            out = new PrintWriter(System.out) ;

            switch (inputFormat) {
                case OPEN_API -> {

                    if (componentsDirectory == null || !Files.isDirectory(componentsDirectory)) {
                        handleError("The directory containing the OpenAPI Components must be provided for the OPEN_API input format.") ;
                    }

                    OpenAPIComparatorOptions options = optionsPath != null ?
                        OpenAPIComparatorOptions.load(optionsPath) : OpenAPIComparatorOptions.load() ;
                    OpenAPIComparator openAPIComparator = new OpenAPIComparator(options.setComparisonAPI(comparisonAPI));

                    if (Files.isDirectory(firstPath) && Files.isDirectory(secondPath)) {
                        if (comparisonAPI.equals("OpenApiCompare")) {
                            handleError("OpenApiCompare can only be used on a single file and not separate files.") ;
                        } if (Files.isRegularFile(outputPath)) {
                            handleError(String.format("Output path %s must not be a regular file, if inputs are directories", outputPath)) ;
                        } else {
                            Files.createDirectories(outputPath);
                            compare(openAPIComparator, firstPath, secondPath, outputPath) ;
                        }
                    } else if (Files.isRegularFile(firstPath) && Files.isRegularFile(secondPath)) {
                        if (Files.isDirectory(outputPath)) {
                            handleError(String.format("Output path %s must not be a directory", outputPath)) ;
                        } else {
                            openAPIComparator.compare(firstPath, secondPath, outputPath, outputFormat)
                                .onSuccessDoWithResult(this::outputResponse).onFailDoWithResponse(this::printComparisonErrors);
                        }
                    } else {
                        handleError(String.format("First path %s and second path %s not must either both be directories or both regular files", firstPath, secondPath)) ;
                    }
                }
                case GRAPHQL, OWL -> handleError(String.format("Input format %s not supported", inputFormat));
            }

        out.close();
    }

    private void compare(OpenAPIComparator openAPIComparator, Path firstPath, Path secondPath, Path outputPath) {
        try {
            Files.list(firstPath).forEach(child -> {

                Path sibling = secondPath.resolve(child.getFileName());

                if (Files.isDirectory(child)) {
                    if (compareDirectory(child)) {
                        compare(openAPIComparator, child, sibling, outputPath);
                    }
                } else if (Files.isRegularFile(child)) {
                    if (child.getFileName().toString().endsWith(".yaml") || child.getFileName().toString().endsWith(".json")) {
                        if (Files.isRegularFile(sibling)) {
                            outputMessage(String.format("Comparing %s with %s", child, sibling));
                            openAPIComparator.compare(child, sibling, outputPath.resolve(child.getFileName() + getFileExtension(outputFormat)), outputFormat)
                                .onSuccessDoWithResult(this::outputResponse).onFailDoWithResponse(this::printComparisonErrors);
                        } else {
                            if (Files.isRegularFile(sibling)) {
                                outputMessage(String.format("Comparing %s with %s", child, sibling));
                                openAPIComparator.compare(child, sibling, outputPath.resolve(child.getFileName() + getFileExtension(outputFormat)), outputFormat)
                                    .onSuccessDoWithResult(this::outputResponse).onFailDoWithResponse(this::printComparisonErrors);
                            } else {
                                Path siblingAgain = componentsDirectory.resolve(child.getFileName());

                                printError(String.format("No matching file for %s found at %s or %s", child, sibling, siblingAgain)) ;
                            }

                            printError(String.format("No matching file for %s found at %s", child, sibling)) ;
                        }
                    } else {
                        outputMessage(String.format("Skipping path %s", child)) ;
                    }
                }
            });
        } catch (IOException e) {
            printException(e);
        }
    }

    private boolean compareDirectory(Path directory) {
        return !IGNORE_PATHS.contains(directory.getFileName().toString());
    }

    private String getFileExtension(ComparisonOutputFormat outputFormat) {
        return switch (outputFormat) {
            case HTML -> ".html";
            case MARKDOWN -> ".md";
            case ASCIIDOC -> ".txt";
            case JSON -> ".json";
        } ;
    }

    private void outputResponse(Path path) {
        out.println(String.format("Comparison generated in '%s'", path.toAbsolutePath()));
        out.flush();
    }

    private void outputMessage(String message) {
        out.println(message);
        out.flush();
    }

    private void printComparisonErrors(Response<Path> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the comparison", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating comparison", response.getAllErrors().size()), response.getAllErrors());
        }
    }
}