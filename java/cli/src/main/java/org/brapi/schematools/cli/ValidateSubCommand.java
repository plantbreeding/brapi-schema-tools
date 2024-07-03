package org.brapi.schematools.cli;

import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.nio.file.Path;
import java.util.List;

@CommandLine.Command(
    name = "validate", mixinStandardHelpOptions = true,
    description = "Validates the BrAPI JSON schema"
)
public class ValidateSubCommand implements Runnable {

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI JSON schema")
    private Path schemaDirectory;

    @Override
    public void run() {
        BrAPISchemaReader schemaReader = new BrAPISchemaReader() ;

        schemaReader.readDirectories(schemaDirectory)
                    .onFailDoWithResponse(this::printErrors)
                    .onSuccessDo(() -> System.out.println("The BrAPI JSON schema is valid"));

    }

    private void printErrors(Response<List<BrAPIClass>> response) {
        if (response.getAllErrors().size() == 1) {
            System.err.printf("There was 1 error validating the JSON Schema:%n");
        } else {
            System.err.printf("There were %d errors validating the JSON Schema:%n", response.getAllErrors().size());
        }

        response.getAllErrors().forEach(this::printError);
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