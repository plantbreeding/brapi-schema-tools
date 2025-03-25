package org.brapi.schematools.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.schema.GraphQLSchema;
import org.brapi.schematools.core.graphql.GraphQLSchemaParser;
import org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator;
import org.brapi.schematools.core.markdown.GraphQLMarkdownGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Generate Sub-command
 */
@CommandLine.Command(
    name = "markdown", mixinStandardHelpOptions = true,
    description = "Generates Markdown descriptions from a GraphQL Schema"
)
public class MarkdownSubCommand implements Runnable {
    private PrintWriter err;

    private static final InputFormat DEFAULT_FORMAT = InputFormat.GRAPHQL;

    @CommandLine.Parameters(index = "0", description = "The URL or file path of the schema, or the result of an introspection query specification. If the path is a valid URL an introspection query will be sent to it. if the path is a file it will be read, otherwise it will be treated as the result of an introspection query.")
    private String schemaPath;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "GRAPHQL", fallbackValue = "OPEN_API", description = "The format of the Input. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    private InputFormat inputFormat = DEFAULT_FORMAT;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The path of the output file or directory for the generated result. If omitted the output will be written to the standard out")
    private Path outputPath;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over writen.")
    private boolean overwrite = true;

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false;

    @CommandLine.Option(names = {"-s", "--stackTrace"}, description = "If an error is recorded output the stack trace.")
    private boolean stackTrace = false;

    @Override
    public void run() {
        try {
            err = new PrintWriter(System.err);

            if (Objects.requireNonNull(inputFormat) == InputFormat.GRAPHQL) {
                generateMarkdown();
            }
        } catch (Exception exception) {

            String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage());
            err.println(message);

            if (stackTrace) {
                exception.printStackTrace(err);
            }

            if (throwExceptionOnFail) {
                throw new BrAPICommandException(message, exception);
            }
        } finally {
            err.close();
        }
    }

    private void generateMarkdown() {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    err.println("For Markdown generation the output path must be a directory");
                }

                Files.createDirectories(outputPath);

                GraphQLMarkdownGeneratorOptions options = GraphQLMarkdownGeneratorOptions.load().setOverwrite(overwrite);

                GraphQLMarkdownGenerator markdownGenerator = GraphQLMarkdownGenerator
                    .generator(outputPath).options(options);

                readSchema(schemaPath, options)
                    .mapResultToResponse(this::parseJsonSchema)
                    .mapResultToResponse(markdownGenerator::generate)
                    .onSuccessDoWithResult(this::outputMarkdownPaths)
                    .onFailDoWithResponse(this::printMarkdownErrors);
            } else {
                err.println("For Markdown generation the output directory must be provided");
            }
        } catch (IOException exception) {
            err.println(exception.getMessage());
        }
    }

    private Response<GraphQLSchema> parseJsonSchema(String schema) {
        GraphQLSchemaParser parser = new GraphQLSchemaParser();

        try {
            return Response.success(parser.parseJsonSchema(schema));
        } catch (JsonProcessingException e) {
            String message = String.format("Unable to parse schema from '%s'", schema);
            err.println(message);
            return Response.fail(Response.ErrorType.VALIDATION, message);
        }
    }

    private Response<String> readSchema(String schema, GraphQLMarkdownGeneratorOptions options) throws IOException {

        if (schema.startsWith("http")) {
            return queryForSchema(schema, options);
        } else {
            Path path = Path.of(schema);
            if (Files.isRegularFile(path)) {
                return readFromFile(path);
            } else {
                return Response.success(schema);
            }
        }
    }

    private Response<String> queryForSchema(String url, GraphQLMarkdownGeneratorOptions options) {

        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI(url))
                .POST(HttpRequest.BodyPublishers.ofString(
                    new ObjectMapper().writeValueAsString(Collections.singletonMap("query",options.getIntrospectionQuery()))))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return Response.success(response.body());
            } else {
                String message = String.format("Unable to get schema from '%s', status code %d, '%s'", url, response.statusCode(), response.body());
                err.println(message);
                return Response.fail(Response.ErrorType.VALIDATION, message);
            }

        } catch (IOException | URISyntaxException | InterruptedException exception) {
            err.println(exception.getMessage());
            return Response.fail(Response.ErrorType.VALIDATION, exception.getMessage());
        }
    }

    private Response<String> readFromFile(Path path) {
        try {
            Stream<String> lines = Files.lines(path);
            String data = lines.collect(Collectors.joining("\n"));
            lines.close();

            return Response.success(data);
        } catch (IOException exception) {
            err.println(exception.getMessage());
            return Response.fail(Response.ErrorType.VALIDATION, exception.getMessage());
        }
    }

    private void outputMarkdownPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any markdown files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' markdown file:");
            System.out.println(paths.get(0).toString());
        } else {
            System.out.printf("Generated '%s' markdown files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printMarkdownErrors(Response<List<Path>> response) {
        String message;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the Markdown");
        } else {
            err.println(message = String.format("There were %d errors generating the Markdown", response.getAllErrors().size()));
            ;
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors());
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