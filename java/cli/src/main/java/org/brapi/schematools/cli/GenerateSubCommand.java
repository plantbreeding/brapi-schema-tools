package org.brapi.schematools.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.jena.ontapi.model.OntModel;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.markdown.MarkdownGenerator;
import org.brapi.schematools.core.ontmodel.OntModelGenerator;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.openapi.generator.OpenAPIGenerator;
import org.brapi.schematools.core.openapi.generator.metadata.OpenAPIGeneratorMetadata;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.xlsx.XSSFWorkbookGenerator;
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
import static org.brapi.schematools.core.utils.OpenAPIUtils.prettyPrint;

/**
 * The Generate Sub-command
 */
@CommandLine.Command(
    name = "generate", mixinStandardHelpOptions = true,
    description = "Generates Various outputs from a BrAPI JSON schema, including OpenAPI Specification or GraphQL Schema"
)
public class GenerateSubCommand extends AbstractSubCommand {
    private PrintWriter out ;

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
    private boolean overwrite = true;

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
                GraphQLGeneratorOptions options = optionsPath != null ?
                    GraphQLGeneratorOptions.load(optionsPath) : GraphQLGeneratorOptions.load();
                GraphQLGeneratorMetadata metadata = metadataPath != null ?
                    GraphQLGeneratorMetadata.load(metadataPath) : GraphQLGeneratorMetadata.load();
                generateGraphQLSchema(options, metadata);
            }
            case OWL -> {
                OntModelGeneratorOptions options = optionsPath != null ?
                    OntModelGeneratorOptions.load(optionsPath) : OntModelGeneratorOptions.load();
                OntModelGeneratorMetadata metadata = metadataPath != null ?
                    OntModelGeneratorMetadata.load(metadataPath) : OntModelGeneratorMetadata.load();
                generateOntModel(options, metadata);
            }
            case MARKDOWN -> generateMarkdown();
            case XLSX -> generateExcel();
        }
    }

    @Override
    protected void closeOut() {
        if (out != null) {
            out.close();
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
        try {
            if (openWriter(outputPath)) {
                out.print(new SchemaPrinter().print(schema));
                out.close();
            }
        } catch (IOException exception) {
            handleException(exception) ;
        }
    }

    private void outputIntrospectionSchema(GraphQLSchema schema) {
        try {
            GraphQL graphQL = GraphQL.newGraphQL(schema).build();
            ExecutionResult executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);

            ObjectMapper mapper = new ObjectMapper();

            if (openWriter(outputPath)) {
                out.print(mapper.writeValueAsString(executionResult.toSpecification().get("data")));
                out.close();
            }
        } catch (IOException exception) {
            handleException(exception) ;
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
        try {
            if (specifications.size() == 1) {
                if (outputPath != null && Files.isDirectory(outputPath)) {
                    outputOpenAPISpecification(specifications.getFirst(), resolveOutputPath(specifications.getFirst()));
                } else {
                    outputOpenAPISpecification(specifications.getFirst(), outputPath);
                }

            } else {
                if (specifications.isEmpty()) {
                    handleError("No specification to to output!");
                } else {
                    if (outputPath != null && Files.isRegularFile(outputPath)) {
                        handleError(String.format("Output path '%s' must be a directory if outputting to separate files.", outputPath.toFile()));
                    } else {
                        for (OpenAPI specification : specifications) {
                            outputOpenAPISpecification(specification, resolveOutputPath(specification));
                        }
                    }
                }
            }
        } catch (IOException exception) {
            handleException(exception) ;
        }
    }

    private Path resolveOutputPath(OpenAPI specification) {
        return outputPath != null ? outputPath.resolve(
            String.format(outputFormat == OutputFormat.OPEN_API ? "%s.yaml" : "%s.json", specification.getInfo().getTitle())) : null ;
    }

    private void outputOpenAPISpecification(OpenAPI specification, Path outputPath) throws IOException {
        if (openWriter(outputPath)) {
            out.print(prettyPrint(specification, outputFormat == OutputFormat.OPEN_API_JSON ? OUTPUT_FORMAT_JSON : OUTPUT_FORMAT_YAML));
            out.close();
        }
    }

    private void printOpenAPISpecificationErrors(Response<List<OpenAPI>> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the OpenAPI Specification", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the OpenAPI Specification", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private void generateOntModel(OntModelGeneratorOptions options, OntModelGeneratorMetadata metadata) {
        OntModelGenerator ontModelGenerator = new OntModelGenerator(options);

        Response<OntModel> response = ontModelGenerator.generate(schemaDirectory, metadata);

        response.onSuccessDoWithResult(this::outputOntModel).onFailDoWithResponse(this::printOntModelErrors);
    }

    private void outputOntModel(OntModel model) {
        try {
            if (openWriter(outputPath)) {
                model.write(out, "TURTLE");
                out.flush();
                out.close();
            }
        } catch (IOException exception) {
            handleException(exception) ;
        }
    }

    private void printOntModelErrors(Response<OntModel> response) {
        if (response.getAllErrors().size() == 1) {
            printErrors("There was 1 error generating the RDF Graph", response.getAllErrors());
        } else {
            printErrors(String.format("There were %d errors generating the RDF Graph", response.getAllErrors().size()), response.getAllErrors());
        }
    }

    private boolean openWriter(Path outputPathFile) throws IOException {
        if (outputPathFile != null) {

            if (Files.isDirectory(outputPathFile)) {
                handleError(String.format("Output path '%s' is a directory", outputPath));
                return false ;
            }

            Files.createDirectories(outputPathFile.getParent());

            if (!overwrite && Files.isRegularFile(outputPathFile)) {
                handleError(String.format("Output file '%s' already exists was not overwritten", outputPath));
                return false ;
            }

            out = new PrintWriter(new FileOutputStream(outputPathFile.toFile()));
        } else {
            out = new PrintWriter(System.out);
        }

        return true ;
    }

    private void generateMarkdown() {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    handleError("For Markdown generation the output path must be a directory");
                } else {

                    Files.createDirectories(outputPath);

                    MarkdownGenerator markdownGenerator = new MarkdownGenerator(outputPath, overwrite);

                    Response<List<Path>> response = markdownGenerator.generate(schemaDirectory);

                    response.onSuccessDoWithResult(this::outputMarkdownPaths).onFailDoWithResponse(this::printMarkdownErrors);
                }
            } else {
                handleError("For Markdown generation the output directory must be provided");
            }
        } catch (IOException exception) {
            handleException(exception) ;
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

    private void generateExcel() {
        try {
            if (outputPath != null) {
                Files.createDirectories(outputPath.getParent());

                // TODO option for split files by module

                if (Files.exists(outputPath) && !Files.isRegularFile(outputPath)) {
                    handleError("For Excel (xlsx) generation the output path must be a file");
                } else {

                    XSSFWorkbookGenerator xssfWorkbookGenerator = new XSSFWorkbookGenerator(outputPath);

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
}