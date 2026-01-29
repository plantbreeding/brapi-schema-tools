package org.brapi.schematools.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.ontapi.model.OntModel;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.markdown.MarkdownGenerator;
import org.brapi.schematools.core.markdown.options.MarkdownGeneratorOptions;
import org.brapi.schematools.core.ontmodel.OntModelGenerator;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.openapi.generator.OpenAPIGenerator;
import org.brapi.schematools.core.openapi.generator.OpenAPIWriter;
import org.brapi.schematools.core.openapi.generator.metadata.OpenAPIGeneratorMetadata;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.r.generator.RGenerator;
import org.brapi.schematools.core.r.metadata.RGeneratorMetadata;
import org.brapi.schematools.core.r.options.RGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.SQLGenerator;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.xlsx.XSSFWorkbookGenerator;
import org.brapi.schematools.core.xlsx.options.XSSFWorkbookGeneratorOptions;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.brapi.schematools.cli.OutputFormat.GRAPHQL;
import static org.brapi.schematools.cli.OutputFormat.GRAPHQL_INTROSPECTION;
import static org.brapi.schematools.core.utils.OpenAPIUtils.OUTPUT_FORMAT_JSON;
import static org.brapi.schematools.core.utils.OpenAPIUtils.OUTPUT_FORMAT_YAML;

/**
 * The Generate Sub-command
 */
@Slf4j
@CommandLine.Command(
    name = "generate", mixinStandardHelpOptions = true,
    description = "Generates Various outputs from a BrAPI JSON schema, including OpenAPI Specification or GraphQL Schema"
)
public class GenerateSubCommand extends AbstractSubCommand {
    //private PrintWriter out ;

    private static final OutputFormat DEFAULT_FORMAT = OutputFormat.OPEN_API;

    @CommandLine.Parameters(index = "0", description = "The directory containing the BrAPI JSON schema")
    private Path schemaDirectory;

    @CommandLine.Option(names = {"-l", "--language"}, defaultValue = "GRAPHQL", fallbackValue = "OPEN_API", description = "The format of the Output. Possible options are: ${COMPLETION-CANDIDATES}. Default is ${DEFAULT_FORMAT}")
    private OutputFormat outputFormat = DEFAULT_FORMAT;

    @CommandLine.Option(names = {"-f", "--file"}, description = "The path of the output file or directory for the generated result. If omitted the output will be written to the standard out")
    private Path outputPath;

    @CommandLine.Option(names = {"-c", "--components"}, description = "The directory containing the OpenAPI Components, required for the OPEN_API output format")
    private Path componentsDirectory;

    @CommandLine.Option(names = {"-o", "--options"}, description = "The path of the options file. If not provided the default options for the specified output format will be used.")
    private Path optionsPath;

    @CommandLine.Option(names = {"-m", "--metadata"}, description = "The path of the metadata file. If not provided the default metadata for the specified output format will be used.")
    private Path metadataPath;

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over written.")
    private Boolean overwrite;

    @CommandLine.Option(names = {"-y", "--separate"}, description = "Output into separate files if possible instead of a single file.")
    private Boolean separate;

    @Override
    public void execute() throws IOException {
        switch (outputFormat) {
            case OPEN_API, OPEN_API_JSON -> {
                OpenAPIGeneratorOptions options = optionsPath != null ?
                    OpenAPIGeneratorOptions.load(optionsPath) : OpenAPIGeneratorOptions.load();
                OpenAPIGeneratorMetadata metadata = metadataPath != null ?
                    OpenAPIGeneratorMetadata.load(metadataPath) : OpenAPIGeneratorMetadata.load();
                generateOpenAPISpecification(options, metadata);
            }
            case GRAPHQL, GRAPHQL_INTROSPECTION -> {
                if (isGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for GraphQL schema.");
                }
                GraphQLGeneratorOptions options = optionsPath != null ?
                    GraphQLGeneratorOptions.load(optionsPath) : GraphQLGeneratorOptions.load();
                GraphQLGeneratorMetadata metadata = metadataPath != null ?
                    GraphQLGeneratorMetadata.load(metadataPath) : GraphQLGeneratorMetadata.load();
                generateGraphQLSchema(options, metadata);
            }
            case OWL -> {
                if (isGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for OWL specifications.");
                }
                OntModelGeneratorOptions options = optionsPath != null ?
                    OntModelGeneratorOptions.load(optionsPath) : OntModelGeneratorOptions.load();
                OntModelGeneratorMetadata metadata = metadataPath != null ?
                    OntModelGeneratorMetadata.load(metadataPath) : OntModelGeneratorMetadata.load();
                generateOntModel(options, metadata);
            }
            case MARKDOWN -> {
                if (isGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for Markdown file generation.");
                }
                MarkdownGeneratorOptions options = optionsPath != null ?
                    MarkdownGeneratorOptions.load(optionsPath) : MarkdownGeneratorOptions.load();

                if (overwrite != null) {
                    options.setOverwrite(overwrite);
                }

                generateMarkdown(options);
            }
            case R -> {
                if (isNotGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for R file generation.");
                }
                RGeneratorOptions options = optionsPath != null ?
                    RGeneratorOptions.load(optionsPath) : RGeneratorOptions.load();
                RGeneratorMetadata metadata = metadataPath != null ?
                    RGeneratorMetadata.load(metadataPath) : RGeneratorMetadata.load();

                if (overwrite != null) {
                    options.setOverwrite(overwrite);
                }

                generateR(options, metadata);
            }
            case SQL -> {
                if (isNotGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for SQL file generation.");
                }
                SQLGeneratorOptions options = optionsPath != null ?
                    SQLGeneratorOptions.load(optionsPath) : SQLGeneratorOptions.load();
                SQLGeneratorMetadata metadata = metadataPath != null ?
                    SQLGeneratorMetadata.load(metadataPath) : SQLGeneratorMetadata.load();

                if (overwrite != null) {
                    options.setOverwrite(overwrite);
                }

                generateSQL(options, metadata);
            }
            case XLSX -> {
                if (isGeneratingIntoSeparateFiles()) {
                    handleError("The 'separate' option is not available for XLSX file generation.");
                }
                XSSFWorkbookGeneratorOptions options = optionsPath != null ?
                    XSSFWorkbookGeneratorOptions.load(optionsPath) : XSSFWorkbookGeneratorOptions.load();
                generateExcel(options);
            }
        }
    }

    private void generateGraphQLSchema(GraphQLGeneratorOptions options, GraphQLGeneratorMetadata metadata) {
        GraphQLGenerator graphQLGenerator = new GraphQLGenerator(options);

        Response<GraphQLSchema> response = graphQLGenerator.generate(schemaDirectory, metadata);

        response
            .onSuccessDoWithResultOnCondition(outputFormat == GRAPHQL, this::outputIDLSchema)
            .onSuccessDoWithResultOnCondition(outputFormat == GRAPHQL_INTROSPECTION, this::outputIntrospectionSchema)
            .onFailDoWithResponse(this::printGraphQLSchemaErrors);
    }

    private void outputIDLSchema(GraphQLSchema schema) {
        openWriter(outputPath)
            .onSuccessDoWithResult(printWriter -> printWriter.print(new SchemaPrinter().print(schema)))
            .onSuccessDoWithResult(PrintWriter::close);
    }

    private void outputIntrospectionSchema(GraphQLSchema schema) {
        try {
            GraphQL graphQL = GraphQL.newGraphQL(schema).build();
            ExecutionResult executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);

            ObjectMapper mapper = new ObjectMapper();

            openWriter(outputPath)
                .onSuccessDoWithResult(printWriter -> {
                    try {
                        printWriter.print(mapper.writeValueAsString(executionResult.toSpecification().get("data")));
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
                .onSuccessDoWithResult(PrintWriter::close);

        } catch (Exception exception) {
            handleException(exception);
        }
    }

    private void printGraphQLSchemaErrors(Response<GraphQLSchema> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the GraphQL Schema", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the GraphQL Schema", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateOpenAPISpecification(OpenAPIGeneratorOptions options, OpenAPIGeneratorMetadata metadata) {
        OpenAPIGenerator openAPIGenerator = new OpenAPIGenerator(options);

        Response<List<OpenAPI>> response = openAPIGenerator.generate(schemaDirectory, componentsDirectory, metadata);

        response
            .onSuccessDoWithResult(this::outputOpenAPISpecifications)
            .onFailDoWithResponse(this::printOpenAPISpecificationErrors);
    }

    private void outputOpenAPISpecifications(List<OpenAPI> specifications) {
        OpenAPIWriter openAPIWriter = new OpenAPIWriter(outputPath,
            outputFormat.equals(OutputFormat.OPEN_API_JSON) ? OUTPUT_FORMAT_JSON : OUTPUT_FORMAT_YAML,
            isGeneratingIntoSeparateFiles(),
            this::openWriter);

        try {
            openAPIWriter.write(specifications)
                .onSuccessDoWithResult(this::outputOpenAPISpecificationPaths)
                .onFailDoWithResponse(this::printOpenAPISpecificationFileErrors)
                .onFailDoWithResponse(this::handleFail) ;
        } catch (IOException e) {
            handleException(e);
        }
    }

    private Response<PrintWriter> openWriter(Path outputPath) {
        try {
            if (outputPath != null) {

                if (Files.isDirectory(outputPath)) {
                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Output path '%s' is a directory", outputPath));
                }

                Files.createDirectories(outputPath.getParent());

                if (!isOverwritingExistingFiles() && Files.isRegularFile(outputPath)) {
                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Output file '%s' already exists was not overwritten", outputPath));
                }

                return Response.success(new PrintWriter(new FileOutputStream(outputPath.toFile())));
            } else {
                return Response.success(new PrintWriter(System.out));
            }
        } catch (IOException e) {
            return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }

    private void printOpenAPISpecificationErrors(Response<List<OpenAPI>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the OpenAPI Specification", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the OpenAPI Specification", response.getAllErrors().size()), response.getAllErrors());
        }
    }


    private void outputOpenAPISpecificationPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any OpenAPI files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' OpenAPI file:");
            System.out.println(paths.getFirst().toString());
        } else {
            System.out.printf("Generated '%s' OpenAPI files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printOpenAPISpecificationFileErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the OpenAPI file(s)", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the OpenAPI file(s)", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateOntModel(OntModelGeneratorOptions options, OntModelGeneratorMetadata metadata) {
        OntModelGenerator ontModelGenerator = new OntModelGenerator(options);

        Response<OntModel> response = ontModelGenerator.generate(schemaDirectory, metadata);

        response.onSuccessDoWithResult(this::outputOntModel).onFailDoWithResponse(this::printOntModelErrors);
    }

    private void outputOntModel(OntModel model) {
        openWriter(outputPath)
            .onSuccessDoWithResult(printWriter -> model.write(printWriter, "TURTLE"))
            .onSuccessDoWithResult(PrintWriter::flush)
            .onSuccessDoWithResult(PrintWriter::close)
            .onFailDoWithResponse(this::handleFail);

    }

    private void printOntModelErrors(Response<OntModel> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the RDF Graph", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the RDF Graph", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateMarkdown(MarkdownGeneratorOptions options) {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    handleError("For Markdown generation the output path must be a directory");
                } else {

                    Files.createDirectories(outputPath);

                    MarkdownGenerator markdownGenerator = new MarkdownGenerator(options, outputPath);

                    Response<List<Path>> response = markdownGenerator.generate(schemaDirectory);

                    response
                        .onSuccessDoWithResult(this::outputMarkdownPaths)
                        .onFailDoWithResponse(this::printMarkdownErrors);
                }
            } else {
                handleError("For Markdown generation the output directory must be provided");
            }
        } catch (IOException exception) {
            handleException(exception);
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
            printErrors("There was 1 error generating the Markdown file(s)", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the Markdown file(s)", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateR(RGeneratorOptions options, RGeneratorMetadata metadata) {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    handleError("For R generation the output path must be a directory");
                } else {

                    if (overwrite && Files.exists(outputPath)) {
                        log.info("Overwriting existing R files in output directory '{}'", outputPath);
                        deleteFiles(outputPath, metadata.getFilePrefix()) ;
                    }

                    Files.createDirectories(outputPath);

                    RGenerator rGenerator = new RGenerator(options, outputPath);

                    Response<List<Path>> response = rGenerator.generate(schemaDirectory, metadata);

                    response.onSuccessDoWithResult(this::outputRPaths).onFailDoWithResponse(this::printRErrors);
                }
            } else {
                handleError("For R generation the output directory must be provided");
            }
        } catch (IOException exception) {
            handleException(exception);
        }
    }

    private void outputRPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any R files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' R file:");
            System.out.println(paths.getFirst().toString());
        } else {
            System.out.printf("Generated '%s' R files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printRErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the R file(s)", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the R file(s)", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateSQL(SQLGeneratorOptions options, SQLGeneratorMetadata metadata) {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    handleError("For SQL generation the output path must be a directory");
                } else {

                    if (overwrite && Files.exists(outputPath)) {
                        log.info("Overwriting existing SQL files in output directory '{}'", outputPath);
                        deleteDirectoryRecursively(outputPath) ;
                    }

                    Files.createDirectories(outputPath);

                    SQLGenerator sqlGenerator = new SQLGenerator(options, outputPath);

                    Response<List<Path>> response = sqlGenerator.generate(schemaDirectory, metadata);

                    response.onSuccessDoWithResult(this::outputSQLPaths).onFailDoWithResponse(this::printSQLErrors);
                }
            } else {
                handleError("For SQL generation the output directory must be provided");
            }
        } catch (IOException exception) {
            handleException(exception);
        }
    }

    private void outputSQLPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any SQL files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' SQL file:");
            System.out.println(paths.getFirst().toString());
        } else {
            System.out.printf("Generated '%s' SQL files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printSQLErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the SQL file(s)", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the SQL file(s)", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateExcel(XSSFWorkbookGeneratorOptions options) {
        try {
            if (outputPath != null) {
                Files.createDirectories(outputPath.getParent());

                // TODO option for split files by module

                if (Files.exists(outputPath) && !Files.isRegularFile(outputPath)) {
                    handleError("For Excel (xlsx) generation the output path must be a file");
                } else {

                    XSSFWorkbookGenerator xssfWorkbookGenerator = new XSSFWorkbookGenerator(options, outputPath);

                    Response<List<Path>> response = xssfWorkbookGenerator.generate(schemaDirectory);

                    response.onSuccessDoWithResult(this::outputExcelPaths).onFailDoWithResponse(this::printExcelErrors);
                }
            } else {
                handleError("For Excel (xlsx) generation the output file must be provided");
            }
        } catch (Exception exception) {
            handleException(exception);
        }
    }

    private void outputExcelPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any excel files");
        } else if (paths.size() == 1) {
            System.out.println("Generated '1' excel file:");
            System.out.println(paths.getFirst().toString());
        } else {
            System.out.printf("Generated '%s' excel files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printExcelErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the Excel file", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating Excel file", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    public boolean isOverwritingExistingFiles() {
        return overwrite != null && overwrite;
    }

    public boolean isGeneratingIntoSeparateFiles() {
        return separate != null && separate;
    }

    public boolean isNotGeneratingIntoSeparateFiles() {
        return separate != null && !separate;
    }

    public static void deleteDirectoryRecursively(Path dir) throws IOException {
        Files.walk(dir)
            .sorted((a, b) -> b.compareTo(a)) // delete children before parent
            .forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
    }

    public static void deleteFiles(Path dir, String prefix) throws IOException {
        try (var files = Files.list(dir)) {
            for (Path path : files.toList()) {
                if (Files.isRegularFile(path) && path.getFileName().toString().startsWith(prefix)) {
                    Files.delete(path);
                }
            }
        }
    }
}