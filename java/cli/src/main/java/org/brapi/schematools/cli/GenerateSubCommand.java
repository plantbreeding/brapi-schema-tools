package org.brapi.schematools.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaPrinter;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import org.apache.jena.ontapi.model.OntModel;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.markdown.MarkdownGenerator;
import org.brapi.schematools.core.ontmodel.OntModelGenerator;
import org.brapi.schematools.core.ontmodel.metadata.OntModelGeneratorMetadata;
import org.brapi.schematools.core.ontmodel.options.OntModelGeneratorOptions;
import org.brapi.schematools.core.openapi.OpenAPIGenerator;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.xlsx.XSSFWorkbookGenerator;
import picocli.CommandLine;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * The Generate Sub-command
 */
@CommandLine.Command(
    name = "generate", mixinStandardHelpOptions = true,
    description = "Generates the OpenAPI Specification or GraphQL Schema from a BrAPI JSON schema"
)
public class GenerateSubCommand implements Runnable {
    private PrintWriter out ;
    private PrintWriter err ;

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

    @CommandLine.Option(names = {"-r", "--overwrite"}, description = "Overwrite the output file(s) if it already exists. True by default, if set to False the output wll not be over writen.")
    private boolean overwrite = true;

    @Override
    public void run() {
        try {
            err = new PrintWriter(System.err) ;

            switch (outputFormat) {

                case OPEN_API -> {
                    OpenAPIGeneratorOptions options = optionsPath != null ?
                        OpenAPIGeneratorOptions.load(optionsPath) : OpenAPIGeneratorOptions.load() ;
                    generateOpenAPISpecification(options);
                }
                case GRAPHQL -> {
                    GraphQLGeneratorOptions options = optionsPath != null ?
                        GraphQLGeneratorOptions.load(optionsPath) : GraphQLGeneratorOptions.load();
                    GraphQLGeneratorMetadata metadata ;
                    generateGraphQLSchema(options);
                }
                case OWL -> {
                    OntModelGeneratorOptions options = optionsPath != null ?
                        OntModelGeneratorOptions.load(optionsPath) :  OntModelGeneratorOptions.load() ;
                    OntModelGeneratorMetadata metadata = metadataPath != null ?
                        OntModelGeneratorMetadata.load(metadataPath) :  OntModelGeneratorMetadata.load() ;
                    generateOntModel(options, metadata);
                }
                case MARKDOWN -> {
                    generateMarkdown();
                }
                case XLSX -> {
                    generateExcel();
                }
            }
        } catch (IOException exception) {
            err.println(exception.getMessage());
        } finally {
            if (out != null) {
                out.close();
            }
            err.close();
        }
    }

    private void generateGraphQLSchema(GraphQLGeneratorOptions options) {
        GraphQLGenerator graphQLGenerator = new GraphQLGenerator(options);

        Response<GraphQLSchema> response = graphQLGenerator.generate(schemaDirectory);

        response.onSuccessDoWithResult(this::outputIDLSchema).onFailDoWithResponse(this::printGraphQLSchemaErrors);
    }

    private void outputIDLSchema(GraphQLSchema schema) {
        try {
            if (openWriter(outputPath)) {
                out.print(new SchemaPrinter().print(schema));
                out.close();
            }
        } catch (IOException e) {
            err.println(e.getMessage());
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
        } catch (IOException e) {
            err.println(e.getMessage());
        }
    }

    private void printGraphQLSchemaErrors(Response<GraphQLSchema> response) {
        if (response.getAllErrors().size() == 1) {
            err.printf("There was 1 error generating the GraphQL Schema:%n");
        } else {
            err.printf("There were %d errors generating the GraphQL Schema:%n", response.getAllErrors().size());
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
            if (specifications.size() == 1) {
                outputOpenAPISpecification(specifications.get(0), outputPath);
            } else {
                if (outputPath != null) {
                    if (!Files.isDirectory(optionsPath)) {
                        err.printf("Output path '%s' must be a directory if outputting separate files:%n", outputPath.toFile());
                    }
                }
                for (OpenAPI specification : specifications) {
                    outputOpenAPISpecification(specification,
                        outputPath != null ? outputPath.resolve(String.format("%s.json", specification.getInfo().getTitle())) : null) ;
                }
            }
        } catch (IOException e) {
            err.println(e.getMessage());
        }
    }

    private void outputOpenAPISpecification(OpenAPI specification, Path outputPath) throws IOException {
        if (openWriter(outputPath)) {
            out.print(Json31.pretty(specification));
            out.close();
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
        } catch (IOException e) {
            err.println(e.getMessage());
        }
    }

    private void printOntModelErrors(Response<OntModel> response) {
        if (response.getAllErrors().size() == 1) {
            err.printf("There was 1 error generating the RDF Graph:%n");
        } else {
            err.printf("There were %d errors generating the RDF Graph:%n", response.getAllErrors().size());
        }

        response.getAllErrors().forEach(this::printError);
    }

    private boolean openWriter(Path outputPathFile) throws IOException {
        if (outputPathFile != null) {
            Files.createDirectories(outputPathFile.getParent());

            if (!overwrite && Files.exists(outputPathFile)) {
                err.println(String.format("Output file '%s' already exists was not overwritten", outputPath));
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
                    err.println("For Markdown generation the output path must be a directory");
                }

                Files.createDirectories(outputPath);

                MarkdownGenerator markdownGenerator = new MarkdownGenerator(outputPath, overwrite);

                Response<List<Path>> response = markdownGenerator.generate(schemaDirectory);

                response.onSuccessDoWithResult(this::outputMarkdownPaths).onFailDoWithResponse(this::printMarkdownErrors);
            } else {
                err.println("For Markdown generation the output directory must be provided");
            }
        } catch (IOException exception) {
            err.println(exception.getMessage());
        }
    }

    private void outputMarkdownPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any markdown files");
        } else if (paths.size() == 1) {
            System.out.println(String.format("Generated '1' markdown file:"));
            System.out.println(paths.get(0).toString());
        } else {
            System.out.println(String.format("Generated '%s' markdown files:", paths.size()));
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printMarkdownErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            err.printf("There was 1 error generating the Markdown:%n");
        } else {
            err.printf("There were %d errors generating the Markdown:%n", response.getAllErrors().size());
        }

        response.getAllErrors().forEach(this::printError);
    }

    private void generateExcel() {
        try {
            if (outputPath != null) {
                Files.createDirectories(outputPath.getParent());

                if (Files.isRegularFile(outputPath)) {
                    err.println("For Excel (xlsx) generation the output path must be a file");
                }

                XSSFWorkbookGenerator xssfWorkbookGenerator = new XSSFWorkbookGenerator(outputPath, overwrite);

                Response<List<Path>> response = xssfWorkbookGenerator.generate(schemaDirectory);

                response.onSuccessDoWithResult(this::outputExcelPaths).onFailDoWithResponse(this::printExcelErrors);
            } else {
                err.println("For Excel (xlsx) generation the output file must be provided");
            }
        } catch (IOException exception) {
            err.println(exception.getMessage());
        }
    }

    private void outputExcelPaths(List<Path> paths) {
        if (paths.isEmpty()) {
            System.out.println("Did not generate any excel files");
        } else if (paths.size() == 1) {
            System.out.println(String.format("Generated '1' excel file:"));
            System.out.println(paths.get(0).toString());
        } else {
            System.out.println(String.format("Generated '%s' excel files:", paths.size()));
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printExcelErrors(Response<List<Path>> response) {
        if (response.getAllErrors().size() == 1) {
            err.printf("There was 1 error generating the Excel:%n");
        } else {
            err.printf("There were %d errors generating the Excel:%n", response.getAllErrors().size());
        }

        response.getAllErrors().forEach(this::printError);
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