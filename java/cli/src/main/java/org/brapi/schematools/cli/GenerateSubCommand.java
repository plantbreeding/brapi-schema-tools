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

/**
 * The Generate Sub-command
 */
@CommandLine.Command(
    name = "generate", mixinStandardHelpOptions = true,
    description = "Generates Various outputs from a BrAPI JSON schema, including OpenAPI Specification or GraphQL Schema"
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

    @CommandLine.Option(names = {"-x", "--throwExceptionOnFail"}, description = "Throw an exception on failure. False by default, if set to True if an exception is thrown when validation or generation fails.")
    private boolean throwExceptionOnFail = false;

    @CommandLine.Option(names = {"-s", "--stackTrace"}, description = "If an error is recorded output the stack trace.")
    private boolean stackTrace = false;

    @Override
    public void run() {
        try {
            err = new PrintWriter(System.err) ;

            switch (outputFormat) {

                case OPEN_API -> {
                    OpenAPIGeneratorOptions options = optionsPath != null ?
                        OpenAPIGeneratorOptions.load(optionsPath) : OpenAPIGeneratorOptions.load() ;
                    OpenAPIGeneratorMetadata metadata = metadataPath != null ?
                        OpenAPIGeneratorMetadata.load(metadataPath) :  OpenAPIGeneratorMetadata.load() ;
                    generateOpenAPISpecification(options, metadata);
                }
                case GRAPHQL -> {
                    GraphQLGeneratorOptions options = optionsPath != null ?
                        GraphQLGeneratorOptions.load(optionsPath) : GraphQLGeneratorOptions.load();
                    GraphQLGeneratorMetadata metadata = metadataPath != null ?
                        GraphQLGeneratorMetadata.load(metadataPath) :  GraphQLGeneratorMetadata.load() ;
                    generateGraphQLSchema(options, metadata);
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
        } catch (Exception exception) {

            String message = String.format("%s: %s", exception.getClass().getSimpleName(), exception.getMessage()) ;
            err.println(message);

            if (stackTrace) {
                exception.printStackTrace(err);
            }

            if (throwExceptionOnFail) {
                throw new BrAPICommandException(message, exception) ;
            }
        } finally {
            if (out != null) {
                out.close();
            }
            err.close();
        }
    }

    private void generateGraphQLSchema(GraphQLGeneratorOptions options, GraphQLGeneratorMetadata metadata) {
        GraphQLGenerator graphQLGenerator = new GraphQLGenerator(options);

        Response<GraphQLSchema> response = graphQLGenerator.generate(schemaDirectory, metadata);

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
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the GraphQL Schema");
        } else {
            err.println(message = String.format("There were %d errors generating the GraphQL Schema", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
    }

    private void generateOpenAPISpecification(OpenAPIGeneratorOptions options, OpenAPIGeneratorMetadata metadata) {
        OpenAPIGenerator openAPIGenerator = new OpenAPIGenerator(options);

        Response<List<OpenAPI>> response = openAPIGenerator.generate(schemaDirectory, componentsDirectory, metadata);

        response
            .onSuccessDoWithResultOnCondition(!options.isSeparatingByModule(), this::outputOpenAPISpecificationFile)
            .onSuccessDoWithResultOnCondition(options.isSeparatingByModule(), this::outputOpenAPISpecificationDirectory)
            .onFailDoWithResponse(this::printOpenAPISpecificationErrors);
    }

    private void outputOpenAPISpecificationFile(List<OpenAPI> specifications) {
        try {
            if (specifications.size() == 1) {
                outputOpenAPISpecification(specifications.get(0), outputPath);
            } else {
                if (specifications.size() == 0) {
                    err.println("No specification to to output!");
                } else {
                    err.printf("Can not output several specification to single file: '%s' :%n", outputPath.toFile());
                }
            }
        } catch (IOException e) {
            err.println(e.getMessage());
        }
    }

    private void outputOpenAPISpecificationDirectory(List<OpenAPI> specifications) {
        try {
            if (outputPath != null) {
                if (Files.isRegularFile(outputPath)) {
                    err.printf("Output path '%s' must be a directory if outputting separate files:%n", outputPath.toFile());
                }

                Files.createDirectories(outputPath);
            }

            for (OpenAPI specification : specifications) {
                outputOpenAPISpecification(specification,
                    outputPath != null ? outputPath.resolve(String.format("%s.json", specification.getInfo().getTitle())) : null);
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
        String message ;
        if (response.getAllErrors().size() == 1) {
            System.err.println(message = "There was 1 error generating the OpenAPI Specification");
        } else {
            System.err.println(message = String.format("There were %d errors generating the OpenAPI Specification", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
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
        } catch (IOException e) {
            err.println(e.getMessage());
        }
    }

    private void printOntModelErrors(Response<OntModel> response) {
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the RDF Graph");
        } else {
            err.println(message = String.format("There were %d errors generating the RDF Graph", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
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
            System.out.println("Generated '1' markdown file:");
            System.out.println(paths.get(0).toString());
        } else {
            System.out.printf("Generated '%s' markdown files:%n", paths.size());
            paths.forEach(path -> System.out.println(path.toString()));
        }
    }

    private void printMarkdownErrors(Response<List<Path>> response) {
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the Markdown");
        } else {
            err.println(message = String.format("There were %d errors generating the Markdown", response.getAllErrors().size())); ;
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
        }
    }

    private void generateExcel() {
        try {
            if (outputPath != null) {
                Files.createDirectories(outputPath.getParent());

                // TODO option for split files by module

                if (Files.exists(outputPath) && !Files.isRegularFile(outputPath)) {
                    err.println("For Excel (xlsx) generation the output path must be a file");
                }

                XSSFWorkbookGenerator xssfWorkbookGenerator = new XSSFWorkbookGenerator(outputPath);

                Response<List<Path>> response = xssfWorkbookGenerator.generate(schemaDirectory);

                response.onSuccessDoWithResult(this::outputExcelPaths).onFailDoWithResponse(this::printExcelErrors);
            } else {
                err.println("For Excel (xlsx) generation the output file must be provided");
            }
        } catch (Exception exception) {
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
        String message ;
        if (response.getAllErrors().size() == 1) {
            err.println(message = "There was 1 error generating the Excel");
        } else {
            err.println(message = String.format("There were %d errors generating the Excel", response.getAllErrors().size()));
        }

        response.getAllErrors().forEach(this::printError);

        if (throwExceptionOnFail) {
            throw new BrAPICommandException(message, response.getAllErrors()) ;
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