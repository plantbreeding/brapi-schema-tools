package org.brapi.schematools.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import graphql.schema.GraphQLSchema;
import org.brapi.schematools.core.authorization.AuthorizationProvider;
import org.brapi.schematools.core.authorization.BasicAuthorizationProvider;
import org.brapi.schematools.core.authorization.BearerAuthorizationProvider;
import org.brapi.schematools.core.authorization.NoAuthorizationProvider;
import org.brapi.schematools.core.authorization.oauth.OpenIDToken;
import org.brapi.schematools.core.authorization.oauth.SingleSignOn;
import org.brapi.schematools.core.graphql.GraphQLSchemaParser;
import org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator;
import org.brapi.schematools.core.markdown.GraphQLMarkdownGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import picocli.CommandLine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.brapi.schematools.core.utils.StringUtils.readStringFromPath;

/**
 * The Generate Sub-command
 */
@CommandLine.Command(
    name = "markdown", mixinStandardHelpOptions = true,
    description = "Generates Markdown descriptions from a GraphQL Schema"
)
public class MarkdownSubCommand extends AbstractSubCommand {
    private static final List<String> VALID_METHODS = Arrays.asList("GET", "POST");

    private static final InputFormat DEFAULT_FORMAT = InputFormat.GRAPHQL;

    @CommandLine.Parameters(index = "0", description = "The URL or file path of the schema, or the result of an introspection query specification. If the path is a valid URL an introspection query will be sent to it. if the path is a file it will be read, otherwise it will be treated as the result of an introspection query.")
    private String schemaPath;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "GRAPHQL", fallbackValue = "GRAPHQL", description = "The format of the Input. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    private InputFormat inputFormat = DEFAULT_FORMAT;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The path of the output file or directory for the generated result. If omitted the output will be written to the standard out")
    private Path outputPath;

    @CommandLine.Option(names = {"-a", "--oauth"}, description = "The URL of the OAuth access token if used")
    private String oauthURL;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @CommandLine.Option(names = {"-u", "--username"}, description = "The username for authentication if required. If not provided the current system username is used.")
    private String username = System.getProperty("user.name");

    @CommandLine.Option(names = {"-p", "--password"}, interactive = true, arity = "0..1", description = "The password for the supplied username. Will fail if not logged in and the password is not provided. Providing the option without a value make the application as for a value.")
    private String password;

    @CommandLine.Option(names = {"-c", "--client"}, description = "The client id for authentication if required.")
    private String clientId;

    @CommandLine.Option(names = {"-s", "--secret"}, description = "The client secret for authentication if required.")
    private String clientSecret;

    @CommandLine.Option(names = {"-b", "--bearer"}, description = "The bearer token for authentication if required.")
    private String bearer;

    @CommandLine.Option(names = {"-m", "--method"}, description = "If the schema path is an URL provide the HTTP method. The default is GET.")
    private String method;

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over writen.")
    private boolean overwrite = true;

    @Override
    public void execute() throws IOException {
        if (Objects.requireNonNull(inputFormat) == InputFormat.GRAPHQL) {
            generateMarkdown();
        } else {
            printError(String.format("Unsupported input format '%s'" , inputFormat));
        }
    }

    private void generateMarkdown() throws IOException{
        if (outputPath != null) {
            if (Files.isRegularFile(outputPath)) {
                printError("For Markdown generation the output path must be a directory");

                return;
            }

            if (method != null) {
                method = method.toUpperCase();
                if (!VALID_METHODS.contains(method)) {
                    printError(String.format("Unsupported method '%s'", method));

                    return;
                }

            } else {
                method = "GET";
            }

            Files.createDirectories(outputPath);

            GraphQLMarkdownGeneratorOptions options = optionsPath != null ?
                GraphQLMarkdownGeneratorOptions.load(optionsPath) : GraphQLMarkdownGeneratorOptions.load();

            options.setOverwrite(overwrite);

            GraphQLMarkdownGenerator markdownGenerator = GraphQLMarkdownGenerator
                .generator(outputPath).options(options);

            readSchema(schemaPath, options)
                .mapResultToResponse(this::parseJsonSchema)
                .mapResultToResponse(markdownGenerator::generate)
                .onSuccessDoWithResult(this::outputMarkdownPaths)
                .onFailDoWithResponse(this::printMarkdownErrors);
        } else {
            printError("For Markdown generation the output directory must be provided");
        }
    }

    private Response<GraphQLSchema> parseJsonSchema(String schema) {
        GraphQLSchemaParser parser = new GraphQLSchemaParser();

        try {
            return Response.success(parser.parseJsonSchema(schema));
        } catch (JsonProcessingException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unable to parse schema from '%s'", schema));
        }
    }

    private Response<String> readSchema(String schema, GraphQLMarkdownGeneratorOptions options) throws IOException {

        if (schema.startsWith("http")) {
            return authorisation()
                .mapResultToResponse(authorizationProvider -> queryForSchema(authorizationProvider, schema, options));
        } else {
            Path path = Path.of(schema);
            if (Files.isRegularFile(path)) {
                return readStringFromPath(path);
            } else {
                return Response.success(schema);
            }
        }
    }

    private Response<String> queryForSchema(AuthorizationProvider authorizationProvider, String url, GraphQLMarkdownGeneratorOptions options) {

        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .method(method, HttpRequest.BodyPublishers.ofString(
                    new ObjectMapper().writeValueAsString(Collections.singletonMap("query",options.getIntrospectionQuery())))) ;

            if (authorizationProvider.required()) {
                authorizationProvider.getAuthorization().onSuccessDoWithResult(authorization -> builder.header("Authorization", authorization)) ;
            }

            HttpResponse<String> response = HttpClient.newHttpClient().send(builder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                if (options.getIntrospectionQueryJsonPath() != null) {
                    String schema = JsonPath.read(response.body(), options.getIntrospectionQueryJsonPath());

                    if (schema == null || schema.isEmpty()) {
                        return Response.fail(Response.ErrorType.VALIDATION,
                            String.format("Unable to extract schema from '%s', using JSONPath, '%s'", response.body(), options.getIntrospectionQueryJsonPath()));
                    }

                    return Response.success(schema);

                } else {
                    return Response.success(response.body());
                }

            } else {
                return Response.fail(Response.ErrorType.VALIDATION,
                    String.format("Unable to get schema from '%s', status code %d, '%s'", url, response.statusCode(), response.body()));
            }

        } catch (IOException | URISyntaxException | InterruptedException exception) {
            printStackTrace(exception);
            return Response.fail(Response.ErrorType.VALIDATION, exception.getMessage());
        }
    }

    private Response<AuthorizationProvider> authorisation() {
        if (oauthURL != null) {
            SingleSignOn sso = SingleSignOn.builder()
                .url(oauthURL)
                .clientId(clientId)
                .username(username).build();

            return sso.getToken()
                .or(() -> login(sso))
                .merge(() -> Response.success(sso));
        } else if (password != null) {
            return Response.success(BasicAuthorizationProvider.builder().username(username).password(password).build());
        } else if (bearer != null) {
            return Response.success(BearerAuthorizationProvider.builder().token(bearer).build());
        } else {
            return Response.success(new NoAuthorizationProvider());
        }
    }

    private Response<OpenIDToken> login(SingleSignOn sso) {
        if (password != null) {
            return sso.loginWithPassword(password);
        } else {
            if (clientSecret != null) {
                return sso.loginWithClientId(clientSecret);
            } else {
                return Response.fail(Response.ErrorType.PERMISSION, String.format("Not logged in please provide password using option '-p' for user '%s' or client secret for client '%s'", username, clientId));
            }
        }
    }

    private void outputMarkdownPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any markdown files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' markdown file:");
            System.out.println(paths.getFirst().toString());
        } else {
            System.out.printf("Generated '%s' markdown files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printMarkdownErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the Markdown", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the Markdown", response.getAllErrors().size()), response.getAllErrors());
        }
    }
}