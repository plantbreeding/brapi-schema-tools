package org.brapi.schematools.cli;

import org.brapi.schematools.core.openapi.ComparisonOutputFormat;
import org.brapi.schematools.core.openapi.OpenAPIComparator;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.PrintWriter;
import java.nio.file.Path;

/**
 * The Compare Sub-command
 */
@CommandLine.Command(
    name = "compare", mixinStandardHelpOptions = true,
    description = "Compares various types of BrAPI, including OpenAPI Specification or GraphQL Schema"
)
public class CompareSubCommand implements Runnable {
    private PrintWriter out ;
    private PrintWriter err ;

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

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over writen.")
    private boolean overwrite = true;

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false;

    @CommandLine.Option(names = {"-s", "--stackTrace"}, description = "If an error is recorded output the stack trace.")
    private boolean stackTrace = false;

    @Override
    public void run() {
        try {
            out = new PrintWriter(System.out) ;
            err = new PrintWriter(System.err) ;

            switch (inputFormat) {
                case OPEN_API -> {
                    OpenAPIComparator openAPIComparator = new OpenAPIComparator();

                    Response<Path> response = openAPIComparator.compare(firstPath, secondPath, outputPath, outputFormat);

                    response.onSuccessDoWithResult(this::outputResponse).onFailDoWithResponse(this::printComparisonErrors);
                }
                case GRAPHQL, OWL -> {
                    String message = String.format("Input format %s not supported", inputFormat) ;
                    err.println(message);

                    if (throwExceptionOnFail) {
                        throw new BrAPICommandException(message) ;
                    }
                }
            }
        } catch (Exception exception) {

            String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage()) ;
            err.println(message);

            if (stackTrace) {
                exception.printStackTrace(err);
            }

            if (throwExceptionOnFail) {
                throw new BrAPICommandException(message, exception) ;
            }
        } finally {
            if (out != null) {
                out.close();
            }
            err.close();
        }
    }

    private void outputResponse(Path path) {
        out.print(String.format("Comparison generated in '%s'", path));
        out.close();
    }

    private void printComparisonErrors(Response<Path> response) {
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the comparison");
        } else {
            err.println(message = String.format("There were %d errors generating comparison", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
    }

    private void printError(Response.Error error) {
        switch (error.getType()) {

            case VALIDATION -> {
                err.print("Validation Error :");
            }
            case PERMISSION, OTHER -> {
                err.print("Error :");
            }
        }
        err.print('\t');

        err.println(error.getMessage());
    }
}