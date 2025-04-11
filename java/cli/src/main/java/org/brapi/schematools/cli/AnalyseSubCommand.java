package org.brapi.schematools.cli;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.brapi.schematools.analyse.AnalysisOptions;
import org.brapi.schematools.analyse.AnalysisReport;
import org.brapi.schematools.analyse.Endpoint;
import org.brapi.schematools.analyse.BrAPISpecificationAnalyserFactory;
import org.brapi.schematools.analyse.TabularReportGenerator;
import org.brapi.schematools.analyse.TabularReportWriter;
import org.brapi.schematools.analyse.authorization.AuthorizationProvider;
import org.brapi.schematools.analyse.authorization.BasicAuthorizationProvider;
import org.brapi.schematools.analyse.authorization.NoAuthorizationProvider;
import org.brapi.schematools.analyse.authorization.oauth.OpenIDToken;
import org.brapi.schematools.analyse.authorization.oauth.SingleSignOn;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.validiation.Validation;
import org.dflib.DataFrame;
import org.dflib.Extractor;
import org.slf4j.LoggerFactory;
import picocli.CommandLine;

import java.io.IOException;
import java.io.PrintStream;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;
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
    @CommandLine.Option(names = {"-i", "--individualReportsByEntity"}, description = "Create an individual report for entity")
    private boolean individualReportsByEntity;
    @CommandLine.Option(names = {"-b", "--batchProcess"}, description = "Process the API requests in batches per entity. Use only with the -i option. WARNING the output file will be deleted prior to starting the batch process.")
    private boolean batchProcess;
    @CommandLine.Option(names = {"-d", "--validate"}, description = "Does a dry run on the analyse, validating the options")
    private boolean validate;
    @CommandLine.Option(names = {"-x", "--summariseAcrossReports"}, description = "Add a summary to any reporting")
    private boolean summariseAcrossReports;

    private TabularReportWriter writer;

    /**
     * Creates the command
     */
    public AnalyseSubCommand() {
        writer = TabularReportWriter
            .writer()
            .autoFilterColumns()
            .autoSizeColumns()
            .freezePane();
    }

    @Override
    public void run() {

        if (verbose) {
            final LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            final Logger logger = loggerContext.exists("org.brapi.schematools.analyse");
            if (logger != null) {
                logger.setLevel(Level.DEBUG);
            }
        }

        try {
            AnalysisOptions options = optionsPath != null ?
                AnalysisOptions.load(optionsPath) : AnalysisOptions.load();

            if (validate) {
                validateOptions(options)
                    .onFailDoWithResponse(this::outputValidation)
                    .map(() -> createAnalyser(options))
                    .mapResultToResponse(BrAPISpecificationAnalyserFactory.Analyser::validate)
                    .onSuccessDoWithResult(this::outputEndpointsToOut);
            } else if (batchProcess) {
                if (!individualReportsByEntity) {
                    err.println("Batch process can only be used in conjunction with 'individualReportsByEntity'");
                } else {
                    if (Files.exists(reportPath)) {
                        out.printf("Deleting Report file '%s'!%n", reportPath.toFile().getAbsolutePath());
                        Files.delete(reportPath);
                    }

                    validateOptions(options)
                        .map(() -> createAnalyser(options))
                        .mapResultToResponse(BrAPISpecificationAnalyserFactory.Analyser::validate)
                        .mapResultToResponse(this::batchAnalyse)
                        .onFailDoWithResponse(this::outputError);
                }

            } else {
                validateOptions(options)
                    .map(() -> createAnalyser(options))
                    .mapResultToResponse(BrAPISpecificationAnalyserFactory.Analyser::validate)
                    .mapResultToResponse(this::analyse)
                    .onFailDoWithResponse(this::outputError);
            }

        } catch (Exception exception) {
            outputException(exception);
        }
    }

    private Response<Validation> validateOptions(AnalysisOptions options) {
        return options.validate().asResponse();
    }

    private Response<BrAPISpecificationAnalyserFactory.Analyser> createAnalyser(AnalysisOptions options) {
        if (Files.isRegularFile(specificationPath)) {
            Stream<String> lines;

            try {
                lines = Files.lines(specificationPath);
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not read path '%s'", specificationPath.toFile()));
            }

            String specification = lines.collect(Collectors.joining("\n"));
            lines.close();

            return authorisation()
                .mapResult(sso -> new BrAPISpecificationAnalyserFactory(baseURL, HttpClient.newBuilder().build(), sso, options))
                .mapResult(factory -> factory.analyser(specification));
        }

        return Response.fail(Response.ErrorType.VALIDATION, String.format("Path '%s' is not regular file", specificationPath.toFile()));
    }

    private Response<List<AnalysisReport>> analyse(BrAPISpecificationAnalyserFactory.Analyser analyser) {
        List<String> entityNames = getEntityNames();

        if (entityNames.isEmpty()) {
            return Stream.of(
                    analyser.analyseSpecial(),
                    analyser.analyseAll())
                .collect(Response.mergeLists())
                .onSuccessDoWithResult(this::outputReports)
                .onSuccessDo(() -> outputEndpoints(analyser)) ;
        } else {
            return Stream.of(
                    analyser.analyseSpecial(),
                    analyser.analyseEntities(entityNames))
                .collect(Response.mergeLists())
                .onSuccessDoWithResult(this::outputReports)
                .onSuccessDo(() -> outputEndpoints(analyser)) ;
        }
    }


    private Response<List<AnalysisReport>> batchAnalyse(BrAPISpecificationAnalyserFactory.Analyser analyser) {

        List<AnalysisReport> completedReports = new LinkedList<>();

        final TabularReportGenerator tabularReportGenerator = summariseAcrossReports ?
            TabularReportGenerator.generator().summariseAcrossReports()  :
            TabularReportGenerator.generator();

        if (reportPath != null) {
            if (Files.isDirectory(reportPath)) {
                err.printf("Report file '%s' is directory!%n", reportPath.toFile().getAbsolutePath());
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Report file '%s' is directory", reportPath));
            }

            try {
                Files.createDirectories(reportPath.getParent()) ;
            } catch (IOException e) {
                return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not create report file directory '%s' ", reportPath.getParent()));
            }

            analyser.analyseSpecial().onFailDoWithResponse(this::outputError)
                .onSuccessDoWithResult(reports -> writeReportsToFile(tabularReportGenerator, reports))
                .onSuccessDoWithResult(completedReports::addAll);

            List<String> entityNames = getEntityNames();

            if (entityNames.isEmpty()) {
                analyser.getEntityNames().forEach(entityName -> {
                    analyser.analyseEntity(entityName)
                        .onFailDoWithResponse(this::outputError)
                        .onSuccessDoWithResult(reports -> writeReportsToFile(tabularReportGenerator, reports))
                        .onSuccessDoWithResult(completedReports::addAll);
                });
            } else {
                entityNames.forEach(entityName -> {
                    analyser.analyseEntity(entityName)
                        .onFailDoWithResponse(this::outputError)
                        .onSuccessDoWithResult(reports -> writeReportsToFile(tabularReportGenerator, reports))
                        .onSuccessDoWithResult(completedReports::addAll);
                });
            }

            writeSummaryToFile(tabularReportGenerator);
            writeEndpointsToFile(analyser); ;
        } else {
            analyser.analyseSpecial()
                .onFailDoWithResponse(this::outputError)
                .onSuccessDoWithResult(reports -> outputReportsToOut(tabularReportGenerator, reports))
                .onSuccessDoWithResult(completedReports::addAll);

            analyser.getEntityNames().forEach(entityName -> analyser.analyseEntity(entityName)
                .onFailDoWithResponse(this::outputError)
                .onSuccessDoWithResult(reports -> outputReportsToOut(tabularReportGenerator, reports))
                .onSuccessDoWithResult(completedReports::addAll));

            outputSummaryToOut(tabularReportGenerator);
            outputEndpointsToOut(analyser);
        }

        return Response.success(completedReports);
    }

    private List<String> getEntityNames() {
        if (entityNames != null) {
            // TODO check if a file containing entity names
            return entityNames;
        } else {
            return new LinkedList<>();
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
            return Response.success(BasicAuthorizationProvider.builder().username(username).password(password).build());
        } else {
            return Response.success(new NoAuthorizationProvider());
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
        err.println("Analysis failed due to: ");
        response.getMessages().forEach(err::println);
    }

    private void outputReports(List<AnalysisReport> listResponses) {

        TabularReportGenerator tabularReportGenerator = TabularReportGenerator.generator();

        if (summariseAcrossReports) {
            tabularReportGenerator = tabularReportGenerator.summariseAcrossReports() ;
        }

        if (reportPath != null) {
            if (Files.isDirectory(reportPath)) {
                err.printf("Report file '%s' is directory!%n", reportPath.toFile().getAbsolutePath());
                return;
            }

            try {
                Files.createDirectories(reportPath.getParent()) ;
            } catch (IOException e) {
                err.printf("Can not create report file directory '%s' %n", reportPath.getParent());
                return;
            }

            writeReportsToFile(tabularReportGenerator, listResponses);
            writeSummaryToFile(tabularReportGenerator);
        } else {
            outputReportsToOut(tabularReportGenerator, listResponses);
            outputSummaryToOut(tabularReportGenerator);
        }
    }

    private void outputReportsToOut(TabularReportGenerator tabularReportGenerator, List<AnalysisReport> listResponses) {
        out.println(tabularReportGenerator.generateReportTable(listResponses));
    }

    private void outputSummaryToOut(TabularReportGenerator tabularReportGenerator) {
        if (summariseAcrossReports) {
            out.println(tabularReportGenerator.getSummary());
        }
    }

    private void writeReportsToFile(TabularReportGenerator tabularReportGenerator, List<AnalysisReport> listResponses) {
        try {
            if (individualReportsByEntity) {
                writer.writeToExcel(tabularReportGenerator.generateReportByEntity(listResponses), reportPath);
            } else {
                writer.writeToExcel(tabularReportGenerator.generateReport(listResponses), reportPath);
            }
        } catch (IOException e) {
            outputException(e);
        }
    }

    private void writeSummaryToFile(TabularReportGenerator tabularReportGenerator) {
        if (summariseAcrossReports) {
            try {
                writer.writeToExcel(tabularReportGenerator.getSummary(), reportPath);
            } catch (IOException e) {
                outputException(e);
            }
        }
    }

    private void outputEndpoints(BrAPISpecificationAnalyserFactory.Analyser analyser) {

        if (reportPath != null) {
            if (Files.isDirectory(reportPath)) {
                err.printf("Report file '%s' is directory!%n", reportPath.toFile().getAbsolutePath());
                return;
            }

            writeEndpointsToFile(analyser);
        } else {
            outputEndpointsToOut(analyser);
        }
    }

    private void writeEndpointsToFile(BrAPISpecificationAnalyserFactory.Analyser analyser) {

        DataFrame analysed ;
        DataFrame notAnalysed ;

        if (this.entityNames != null && !this.entityNames.isEmpty()) {
            analysed = DataFrame
                .byRow(
                    Extractor.$col(Endpoint::getEntityName),
                    Extractor.$col(Endpoint::getPath),
                    Extractor.$col(Endpoint::getMethod),
                    Extractor.$col(Endpoint::getCategory),
                    Extractor.$val("Analysed"))
                .columnNames("Entity", "Path", "Method", "Category", "Class")
                .appender()
                .append(analyser.getEndpoints().stream().filter(endpoint -> this.entityNames.contains(endpoint.getEntityName())).toList())
                .toDataFrame();

            notAnalysed = DataFrame
                .byRow(
                    Extractor.$col(Endpoint::getEntityName),
                    Extractor.$col(Endpoint::getPath),
                    Extractor.$col(Endpoint::getMethod),
                    Extractor.$col(Endpoint::getCategory),
                    Extractor.$val("Not Analysed"))
                .columnNames("Entity", "Path", "Method", "Category", "Class")
                .appender()
                .append(analyser.getEndpoints().stream().filter(endpoint -> !this.entityNames.contains(endpoint.getEntityName())).toList())
                .toDataFrame();
        } else {
            analysed = DataFrame
                .byRow(
                    Extractor.$col(Endpoint::getEntityName),
                    Extractor.$col(Endpoint::getPath),
                    Extractor.$col(Endpoint::getMethod),
                    Extractor.$col(Endpoint::getCategory),
                    Extractor.$val("Analysed"))
                .columnNames("Entity", "Path", "Method", "Category", "Class")
                .appender()
                .append(analyser.getEndpoints())
                .toDataFrame();

            notAnalysed = DataFrame
                .byRow(
                    Extractor.$col(Endpoint::getEntityName),
                    Extractor.$col(Endpoint::getPath),
                    Extractor.$col(Endpoint::getMethod),
                    Extractor.$col(Endpoint::getCategory),
                    Extractor.$val("Not Analysed"))
                .columnNames("Entity", "Path", "Method", "Category", "Class")
                .appender()
                .append(new ArrayList<>())
                .toDataFrame();
        }

        DataFrame unmatched = DataFrame
            .byRow(
                Extractor.$col(Endpoint::getEntityName),
                Extractor.$col(Endpoint::getPath),
                Extractor.$col(Endpoint::getMethod),
                Extractor.$col(Endpoint::getCategory),
                Extractor.$val("Unmatched"))
            .columnNames("Entity", "Path", "Method", "Category", "Class")
            .appender()
            .append(analyser.getUnmatchedEndpoints())
            .toDataFrame();

        DataFrame skipped = DataFrame
            .byRow(
                Extractor.$col(Endpoint::getEntityName),
                Extractor.$col(Endpoint::getPath),
                Extractor.$col(Endpoint::getMethod),
                Extractor.$col(Endpoint::getCategory),
                Extractor.$val("Skipped"))
            .columnNames("Entity", "Path", "Method", "Category", "Class")
            .appender()
            .append(analyser.getSkippedEndpoints())
            .toDataFrame();

        DataFrame depreciated = DataFrame
            .byRow(
                Extractor.$col(Endpoint::getEntityName),
                Extractor.$col(Endpoint::getPath),
                Extractor.$col(Endpoint::getMethod),
                Extractor.$col(Endpoint::getCategory),
                Extractor.$val("Depreciated"))
            .columnNames("Entity", "Path", "Method", "Category", "Class")
            .appender()
            .append(analyser.getDeprecatedEndpoints())
            .toDataFrame();

        DataFrame endpoints = analysed.vConcat(notAnalysed, unmatched, skipped, depreciated).as("Endpoints") ;

        try {
            writer.writeToExcel(endpoints, reportPath);
        } catch (IOException e) {
            outputException(e);
        }
    }

    private void outputEndpointsToOut(BrAPISpecificationAnalyserFactory.Analyser analyser) {
        out.println("Analysing Endpoints:");
        analyser.getEndpoints().forEach(out::println);
        out.println();
        out.println("Skipping Endpoints:");
        analyser.getSkippedEndpoints().forEach(out::println);
        out.println();
        out.println("Ignored Endpoints:");
        analyser.getUnmatchedEndpoints().forEach(out::println);
        out.println();
        out.println("Deprecated Endpoints:");
        analyser.getDeprecatedEndpoints().forEach(out::println);
    }


    private void outputValidation(Response<Validation> response) {
        err.println("Validation Errors:");
        response.getMessages().forEach(err::println);
    }

    private void outputException(Exception exception) {
        String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage());
        err.println(message);
    }
}