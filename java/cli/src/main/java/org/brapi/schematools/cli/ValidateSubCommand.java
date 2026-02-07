package org.brapi.schematools.cli;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderOptions;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * The Validate Sub-command
 */
@CommandLine.Command(
    name = "validate", mixinStandardHelpOptions = true,
    description = "Validates the BrAPI JSON schema"
)
public class ValidateSubCommand  extends AbstractSubCommand {

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI JSON schema")
    private Path schemaDirectory;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @Override
    protected void execute() throws IOException {
        BrAPISchemaReaderOptions options = optionsPath != null ?
            BrAPISchemaReaderOptions.load(optionsPath) : BrAPISchemaReaderOptions.load();

        BrAPISchemaReader schemaReader = new BrAPISchemaReader(options) ;

        schemaReader.readDirectories(schemaDirectory)
            .onFailDoWithResponse(this::printErrors)
            .onSuccessDo(() -> System.out.println("The BrAPI JSON schema is valid"));
    }

    private void printErrors(Response<List<BrAPIClass>> response) {
        String message ;
        if (response.getAllErrors().size() == 1) {
            System.err.println(message = "There was 1 error validating the JSON Schema");
        } else {
            System.err.println(message = String.format("There were %d errors validating the JSON Schema", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (isThrowExceptionOnFail()) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
    }
}