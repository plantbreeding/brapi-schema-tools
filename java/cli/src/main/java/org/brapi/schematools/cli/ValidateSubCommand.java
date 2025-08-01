package org.brapi.schematools.cli;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

/**
 * The Validate Sub-command
 */
@CommandLine.Command(
    name = "validate", mixinStandardHelpOptions = true,
    description = "Validates the BrAPI JSON schema"
)
public class ValidateSubCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI JSON schema")
    private Path schemaDirectory;

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false ;

    @Override
    public void run() {
        BrAPISchemaReader schemaReader = new BrAPISchemaReader() ;

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

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
    }

    private void printError(Response.Error error) {
        switch (error.getType()) {

            case VALIDATION -> {
                System.err.print("Validation Error :");
            }
            case PERMISSION, OTHER -> {
                System.err.print("Error :");
            }
        }
        System.err.print('\t');

        System.err.println(error.getMessage());
    }
}