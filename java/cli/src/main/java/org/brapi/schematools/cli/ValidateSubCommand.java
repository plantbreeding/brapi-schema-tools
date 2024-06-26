package org.brapi.schematools.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.brapischema.BrAPISchemaReaderException;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.openapi.OpenAPIGenerator;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
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

        try {
            schemaReader.readDirectories(schemaDirectory)
                    .onFailDoWithResponse(this::printErrors)
                    .onSuccessDo(() -> System.out.println("The BrAPI JSON schema is valid"));
        } catch (BrAPISchemaReaderException e) {
            System.out.println(e.getMessage());
        }

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