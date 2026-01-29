package org.brapi.schematools.cli;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.examples.ExamplesGenerator;
import org.brapi.schematools.core.examples.ExamplesGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.dflib.DataFrame;
import org.dflib.csv.Csv;
import org.dflib.excel.Excel;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * The Examples Sub-command. Generates JSON examples from tabular data.
 */
@CommandLine.Command(
    name = "examples", mixinStandardHelpOptions = true,
    description = "Generates JSON examples from tabular data"
)
public class ExamplesSubCommand implements Runnable {

    private ObjectWriter writer ;
    private PrintWriter out ;
    private PrintWriter err ;

    private static final FileFormat DEFAULT_INPUT_FORMAT = FileFormat.CSV;

    @CommandLine.Parameters(index = "0", description = "The path to the input file.")
    private Path inputPath;

    @CommandLine.Parameters(index = "1", description = "The path to the output directory.")
    private Path outputPath;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "CSV", fallbackValue = "CSV", description = "The format of the Input. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_INPUT_FORMAT}")
    private FileFormat inputFormat = DEFAULT_INPUT_FORMAT;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over written.")
    private boolean overwrite = true;

    @CommandLine.Option(names = {"-p", "--prettyPrint"}, description = "Pretty print the JSON output if possible. True by default.")
    private boolean prettyPrint = true;

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false;

    @CommandLine.Option(names = {"-t", "--stackTrace"}, description = "If an error is recorded output the stack trace.")
    private boolean stackTrace = false;

    @Override
    public void run() {
        try {
            err = new PrintWriter(System.err);

            if (Files.isRegularFile(outputPath)) {
                err.println("Output file is a regular file and not a directory.");
            } else {

                Files.createDirectories(outputPath);

                if (prettyPrint) {
                    writer = new ObjectMapper().writerWithDefaultPrettyPrinter();
                } else {
                    writer = new ObjectMapper().writer();
                }

                Map<String, DataFrame> dataFrames =
                    switch (inputFormat) {
                        case CSV -> loadCsv(inputPath);
                        case XSLX -> loadExcel(inputPath);
                    };

                ExamplesGeneratorOptions options = optionsPath != null ?
                    ExamplesGeneratorOptions.load(optionsPath) : ExamplesGeneratorOptions.load();

                ExamplesGenerator examplesGenerator = new ExamplesGenerator(options);

                examplesGenerator
                    .generate(dataFrames)
                    .onSuccessDoWithResult(this::outputExamples)
                    .onFailDoWithResponse(this::printErrors);
            }

        } catch (Exception exception) {
            handleError(exception);
        } finally {
            if (out != null) {
                out.close();
            }
            err.close();
        }
    }

    private Map<String, DataFrame> loadCsv(Path path) {
        return Map.of(path.getFileName().toString(), Csv.load(path));
    }

    private Map<String, DataFrame> loadExcel(Path path) {
        return Excel.load(path);
    }

    private boolean openWriter(Path outputPathFile) throws IOException {
        if (outputPathFile != null) {
            Files.createDirectories(outputPathFile.getParent());

            if (!overwrite && Files.exists(outputPathFile)) {
                err.println(String.format("Output file '%s' already exists and was not overwritten", outputPath));
                return false ;
            }

            out = new PrintWriter(new FileOutputStream(outputPathFile.toFile()));
        } else {
            out = new PrintWriter(System.out);
        }

        return true ;
    }

    private void outputExamples(Map<String, JsonNode> examples) {
        examples.entrySet().forEach(this::outputExamples);
    }

    private void outputExamples(Map.Entry<String, JsonNode> entity) {
        try {
            if (openWriter(outputPath.resolve(String.format("%s.json", entity.getKey())))) {
                writer.writeValue(out, entity.getValue());

                out.flush();
                out.close();
            }
        } catch (IOException e) {
            handleError(e) ;
        }

    }

    private void handleError(Exception exception) {
        err.println(exception.getMessage());

        String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage());
        err.println(message);

        if (stackTrace) {
            exception.printStackTrace(err);
        }

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, exception);
        }
    }

    private void printErrors(Response<Map<String, JsonNode>> response) {
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the GraphQL Schema");
        } else {
            err.println(message = String.format("There were %d errors generating the GraphQL Schema", response.getAllErrors().size()));
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
