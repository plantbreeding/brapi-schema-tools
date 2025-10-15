package org.brapi.schematools.cli;

import org.brapi.schematools.core.openapi.comparator.ComparisonOutputFormat;
import org.brapi.schematools.core.openapi.comparator.OpenAPIComparator;
import org.brapi.schematools.core.openapi.comparator.options.OpenAPIComparatorOptions;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * The Compare Sub-command
 */
@CommandLine.Command(
    name = "compare", mixinStandardHelpOptions = true,
    description = "Compares various types of BrAPI, including OpenAPI Specification or GraphQL Schema"
)
public class CompareSubCommand extends AbstractSubCommand {
    private PrintWriter out ;

    private static final InputFormat DEFAULT_INPUT_FORMAT = InputFormat.OPEN_API;
    private static final ComparisonOutputFormat DEFAULT_OUTPUT_FORMAT = ComparisonOutputFormat.MARKDOWN;

    @CommandLine.Parameters(index = "0", description = "The file containing the first BrAPI Specification/Schema")
    private Path firstPath;

    @CommandLine.Parameters(index = "1", description = "The file containing the second BrAPI Specification/Schema")
    private Path secondPath;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "GRAPHQL", fallbackValue = "GRAPHQL", description = "The format of the Input. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_INPUT_FORMAT}")
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

    @Override
    public void execute() throws IOException {
            out = new PrintWriter(System.out) ;

            switch (inputFormat) {
                case OPEN_API -> {

                    OpenAPIComparatorOptions options = optionsPath != null ?
                        OpenAPIComparatorOptions.load(optionsPath) : OpenAPIComparatorOptions.load() ;
                    OpenAPIComparator openAPIComparator = new OpenAPIComparator(options);

                    Response<Path> response = openAPIComparator.compare(firstPath, secondPath, outputPath, outputFormat);

                    response.onSuccessDoWithResult(this::outputResponse).onFailDoWithResponse(this::printComparisonErrors);
                }
                case GRAPHQL, OWL -> handleError(String.format("Input format %s not supported", inputFormat) );
            }
    }

    private void outputResponse(Path path) {
        out.print(String.format("Comparison generated in '%s'", path.toAbsolutePath()));
        out.close();
    }

    private void printComparisonErrors(Response<Path> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the comparison", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating comparison", response.getAllErrors().size()), response.getAllErrors());
        }
    }
}