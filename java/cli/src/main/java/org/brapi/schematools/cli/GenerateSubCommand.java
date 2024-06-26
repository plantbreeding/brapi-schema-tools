package org.brapi.schematools.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
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
    name = "generate", mixinStandardHelpOptions = true,
    description = "Generates the OpenAPI Specification or GraphQL Schema from a BrAPI JSON schema"
)
public class GenerateSubCommand implements Runnable {

    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.OPEN_API;

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI JSON schema")
    private Path schemaDirectory;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "GRAPHQL", fallbackValue = "OPEN_API", description = "The format of the Output. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    private OutputFormat outputFormat;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The path of the output file for the result. If omitted the output will be written to the standard out")
    private Path outputPathFile;

    @CommandLine.Option(names = {"-c", "--components"}, description = "The directory containing the OpenAPI Components")
    private Path componentsDirectory;

    @Override
    public void run() {
        switch (outputFormat) {

            case OPEN_API -> {
                OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();
                generateOpenAPISpecification(options);
            }
            case GRAPHQL -> {
                GraphQLGeneratorOptions options = GraphQLGeneratorOptions.load();
                generateGraphQLSchema(options);
            }
        }

    }

    private void generateGraphQLSchema(GraphQLGeneratorOptions options) {
        GraphQLGenerator graphQLGenerator = new GraphQLGenerator(options);

        Response<GraphQLSchema> response = graphQLGenerator.generate(schemaDirectory);

        response.onSuccessDoWithResult(this::outputIDLSchema).onFailDoWithResponse(this::printGraphQLSchemaErrors);
    }

    private void outputIDLSchema(GraphQLSchema schema) {

        try {
            if (outputPathFile != null) {
                Files.createDirectories(outputPathFile.getParent());
            }

            PrintWriter writer = new PrintWriter(outputPathFile != null ? new FileOutputStream(outputPathFile.toFile()) : System.out);

            writer.print(new SchemaPrinter().print(schema));

            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void outputIntrospectionSchema(GraphQLSchema schema) {

        try {
            if (outputPathFile != null) {
                Files.createDirectories(outputPathFile.getParent());
            }

            PrintWriter writer = new PrintWriter(outputPathFile != null ? new FileOutputStream(outputPathFile.toFile()) : System.out);

            GraphQL graphQL = GraphQL.newGraphQL(schema).build();
            ExecutionResult executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);

            ObjectMapper mapper = new ObjectMapper();

            writer.print(mapper.writeValueAsString(executionResult.toSpecification().get("data")));

            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printGraphQLSchemaErrors(Response<GraphQLSchema> response) {
        if (response.getAllErrors().size() == 1) {
            System.err.printf("There was 1 error generating the GraphQL Schema:%n");
        } else {
            System.err.printf("There were %d errors generating the GraphQL Schema:%n", response.getAllErrors().size());
        }

        response.getAllErrors().forEach(this::printError);
    }

    private void generateOpenAPISpecification(OpenAPIGeneratorOptions options) {
        OpenAPIGenerator openAPIGenerator = new OpenAPIGenerator(options);

        Response<List<OpenAPI>> response = openAPIGenerator.generate(schemaDirectory, componentsDirectory);

        response.onSuccessDoWithResult(this::outputOpenAPISpecification).onFailDoWithResponse(this::printOpenAPISpecificationErrors);
    }

    private void outputOpenAPISpecification(List<OpenAPI> specifications) {
        try {
            if (outputPathFile != null) {
                Files.createDirectories(outputPathFile.getParent());
            }

            if (specifications.size() == 1) {
                outputOpenAPISpecification(specifications.get(0), outputPathFile);
            } else {
                if (outputPathFile != null) {
                    Files.createDirectories(outputPathFile);
                }

                specifications.forEach(specification -> outputOpenAPISpecification(specification,
                    outputPathFile != null ? outputPathFile.resolve(String.format("%s.json", specification.getInfo().getTitle())) : null));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void outputOpenAPISpecification(OpenAPI specification, Path outputPathFile) {
        try {
            PrintWriter writer = new PrintWriter(outputPathFile != null ? new FileOutputStream(outputPathFile.toFile()) : System.out);

            writer.print(Json31.pretty(specification));

            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    private void printOpenAPISpecificationErrors(Response<List<OpenAPI>> response) {
        if (response.getAllErrors().size() == 1) {
            System.err.printf("There was 1 error generating the OpenAPI Specification:%n");
        } else {
            System.err.printf("There were %d errors generating the OpenAPI Specification:%n", response.getAllErrors().size());
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