package org.brapi.schematools.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.brapi.schematools.analyse.AnalysisReport;
import org.brapi.schematools.analyse.AuthorizationProvider;
import org.brapi.schematools.analyse.OpenAPISpecificationAnalyser;
import org.brapi.schematools.analyse.TabularReportGenerator;
import org.brapi.schematools.analyse.oauth.OpenIDToken;
import org.brapi.schematools.analyse.oauth.SingleSignOn;
import org.brapi.schematools.analyse.query.BasicAuthorizationProvider;
import org.brapi.schematools.analyse.query.NoAuthorizationProvider;
import org.brapi.schematools.core.response.Response;
import org.dflib.DataFrame;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.dflib.excel.Excel;

/**
 * Analyse Command
 */
@CommandLine.Command(
    name = "analyse", mixinStandardHelpOptions = true,
    description = "Executes a query pipeline or set of pipelines"
)
public class AnalyseSubCommand implements Runnable {

    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    @CommandLine.Parameters(index = "0", description = "The path to the specification file.")
    private Path specificationPath;
    @CommandLine.Parameters(index = "1", description = "The Base URL to be analysed.")
    private String baseURL;

    @CommandLine.Option(names = {"-e", "--entities"}, description = "A list of entities or a file containing a list of entities on which to run the analysis")
    private List<String> entityNames;
    @CommandLine.Option(names = {"-a", "--oauth"}, description = "The URL of the OAuth access token if used")
    private String oauthURL;
    @CommandLine.Option(names = {"-o", "--output"}, description = "The path to the directory where the output is sent.")
    private Path outputPath;
    @CommandLine.Option(names = {"-r", "--report"}, description = "The path to Excel workbook where the report is sent. If not provided, the standard out is used.")
    private Path reportPath;
    @CommandLine.Option(names = {"-u", "--username"}, description = "The username for authentication if required. If not provided the current system username is used.")
    private String username = System.getProperty("user.name");
    @CommandLine.Option(names = {"-p", "--password"}, interactive = true, arity = "0..1", description = "The password for the supplied username. Will fail if not logged in and the password is not provided. Providing the option without a value make the application as for a value.")
    private String password;
    @CommandLine.Option(names = {"-c", "--client"}, description = "The client id for authentication if required.")
    private String clientId;
    @CommandLine.Option(names = {"-s", "--secret"}, description = "The client secret for authentication if required.")
    private String clientSecret;
    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Provide a verbose output to standard out describing the current step etc.")
    private boolean verbose;

    @Override
    public void run() {

        if (verbose) {
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final Logger logger = loggerContext.exists("org.brapi.schematools.analyse");
            if (logger != null) {
                logger.setLevel(Level.DEBUG);
            }
        }

        execute()
            .onFailDoWithResponse(this::outputError)
            .onSuccessDoWithResult(this::outputResult);
    }

    private Response<List<AnalysisReport>> execute() {
        if (Files.isRegularFile(specificationPath)) {
            return authorisation()
                .mapResult(sso -> new OpenAPISpecificationAnalyser(baseURL, HttpClient.newBuilder().build(), sso))
                .mapResultToResponse(this::analyse);
        }

        return Response.fail(Response.ErrorType.VALIDATION, String.format("Path '%s' is not regular file", specificationPath.toFile()));
    }

    private Response<List<AnalysisReport>> analyse(OpenAPISpecificationAnalyser analyser) {
        Stream<String> lines ;

        try {
            lines = Files.lines(specificationPath);
        } catch (IOException e) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not read path '%s'", specificationPath.toFile()));
        }
        String specification = lines.collect(Collectors.joining("\n"));
        lines.close();

        if (entityNames != null) {
            // TODO check if a file
            return analyser.analyse(specification, entityNames) ;
        } else {
            return analyser.analyse(specification) ;
        }
    }

    private Response<AuthorizationProvider> authorisation() {
        if (oauthURL != null) {
            SingleSignOn sso = SingleSignOn.builder()
                .url(oauthURL)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username).build();

            return sso.getToken()
                .or(() -> login(sso))
                .merge(() -> Response.success(sso));
        } else if (password != null) {
            return Response.success(BasicAuthorizationProvider.builder().username(username).password(password).build()) ;
        } else {
            return Response.success(new NoAuthorizationProvider()) ;
        }
    }

    private Response<OpenIDToken> login(SingleSignOn sso) {
        if (password != null) {
            return sso.login(password);
        } else {
            return Response.fail(Response.ErrorType.PERMISSION, String.format("Not logged please provide password using option '-p' for user '%s' ", username));
        }
    }

    private void outputError(Response<List<AnalysisReport>> response) {
        err.println("Analysis failed due to");
        response.getMessages().forEach(err::println);
    }

    private void outputResult(List<AnalysisReport> listResponses) {

        TabularReportGenerator tabularReportGenerator = new TabularReportGenerator();

        if (outputPath != null && !Files.isDirectory(outputPath)) {
            err.printf("Output path '%s' is not directory!%n", outputPath.toFile().getAbsolutePath());
            return;
        }

        if (reportPath != null) {
            if (Files.isDirectory(reportPath)) {
                err.printf("Report file '%s' is directory!%n", reportPath.toFile().getAbsolutePath());
                return;
            }

            DataFrame report = tabularReportGenerator.generateReport(listResponses);

            Excel.save(Collections.singletonMap(report.getName(), report), reportPath);
        } else {
            out.println(tabularReportGenerator.generateReportTable(listResponses));
        }
    }
}