package org.brapi.schematools.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import org.brapi.schematools.core.GraphQLGenerator;
import org.brapi.schematools.core.Response;
import picocli.CommandLine;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(
  name = "generate", mixinStandardHelpOptions = true
)
public class GenerateSubCommand implements Runnable {

    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.OPEN_API;

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI json schema")
    private Path schemaDirectory;

    @CommandLine.Option(names = { "-o", "--output" }, defaultValue = "GRAPHQL", fallbackValue = "OPEN_API", description = "The format of the Output. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    private OutputFormat outputFormat;

    @CommandLine.Option(names = { "-f", "--file" }, description = "The path of the output file for the result. If omitted the output will be written to the standard out")
    private Path outputPathFile;

    @Override
    public void run() {
        switch (outputFormat) {

            case OPEN_API -> {
                System.out.println("No yet supported!");
            }
            case GRAPHQL -> {
                generateGraphQLSchema() ;
            }
        }

    }

    private void generateGraphQLSchema() {
        GraphQLGenerator graphQLGenerator = new GraphQLGenerator() ;

        Response<GraphQLSchema> response = graphQLGenerator.generate(schemaDirectory, GraphQLGenerator.Options.builder().build()) ;

        response.onSuccessDoWithResult(this::outputIDLSchema).onFailDoWithResponse(this::printErrors) ;
    }

    private void outputIDLSchema(GraphQLSchema schema) {

        try {
            if (outputPathFile != null) {
                Files.createDirectories(outputPathFile.getParent()) ;
            }

            PrintWriter writer  = new PrintWriter(outputPathFile != null ? new FileOutputStream(outputPathFile.toFile()) : System.out);

            writer.print(new SchemaPrinter().print(schema));

            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void outputIntrospectionSchema(GraphQLSchema schema) {

        try {
            if (outputPathFile != null) {
                Files.createDirectories(outputPathFile.getParent()) ;
            }

            PrintWriter writer  = new PrintWriter(outputPathFile != null ? new FileOutputStream(outputPathFile.toFile()) : System.out);

            GraphQL graphQL = GraphQL.newGraphQL(schema).build();
            ExecutionResult executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);

            ObjectMapper mapper= new ObjectMapper() ;

            writer.print(mapper.writeValueAsString(executionResult.toSpecification().get("data")));

            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void printErrors(Response<GraphQLSchema> response) {
        response.getAllErrors().forEach(this::printError);
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}