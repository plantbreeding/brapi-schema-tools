package org.brapi.schematools.core.graphql;

import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

/**
 * Generates a Markdown files for type and their field descriptions from a GraphQL Schema
 */
@Slf4j
@AllArgsConstructor
public class GraphQLMarkdownGenerator {

    private Path outputPath ;

    private boolean overwrite ;
    private String queryDefinitionsDirectory = "queries";
    private String typeDefinitionsDirectory = "dataTypes";
    private String descriptionsDirectory = "descriptions";
    private String fieldsDirectory = "fields" ;
    private String argumentsDirectory = "arguments" ;

    private GraphQLMarkdownGenerator() {

    }

    /**
     * Creates the default Generator.
     * @param outputPath the output path for markdown files.
     * @return the default Generator
     */
    public static GraphQLMarkdownGenerator generator(Path outputPath) {
        return new GraphQLMarkdownGenerator().outputPath(outputPath) ;
    }

    /**
     * Sets the output path.
     * @param outputPath the output path for markdown files.
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator outputPath(Path outputPath) {
        this.outputPath = outputPath ;
        return this;
    }

    /**
     * Overwrite the files.
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator overwrite() {
        overwrite = true ;
        return this;
    }

    /**
     * Sets the type definitions directory.
     * @param typeDefinitionsDirectory the directory of markdown files for types definitions
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator typeDefinitionsDirectory(String typeDefinitionsDirectory) {
        this.typeDefinitionsDirectory = typeDefinitionsDirectory ;
        return this;
    }

    /**
     * Sets the query definitions directory.
     * @param queryDefinitionsDirectory the directory of markdown files for query definitions
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator queryDefinitionsDirectory(String queryDefinitionsDirectory) {
        this.queryDefinitionsDirectory = queryDefinitionsDirectory ;
        return this;
    }

    /**
     * Sets the descriptions directory.
     * @param descriptionsDirectory the directory of markdown descriptions
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator descriptionsDirectory(String descriptionsDirectory) {
        this.descriptionsDirectory = descriptionsDirectory ;
        return this;
    }

    /**
     * Sets the fields directory.
     * @param fieldsDirectory the directory of markdown field descriptions
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator fieldsDirectory(String fieldsDirectory) {
        this.fieldsDirectory = fieldsDirectory ;
        return this;
    }

    /**
     * Sets the arguments directory.
     * @param argumentsDirectory the directory of markdown arguments descriptions
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator argumentsDirectory(String argumentsDirectory) {
        this.argumentsDirectory = argumentsDirectory ;
        return this;
    }

    /**
     * Generates Markdown files for type definitions and their field descriptions,
     * query definitions and their arguments descriptions
     * from the GraphQL schema
     * @param schema the path to the complete BrAPI Specification
     * @return the paths of the Markdown files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(GraphQLSchema schema) {

        return new GraphQLMarkdownGenerator.Generator(schema).generate() ;
    }

    private class Generator {
        
        private final Path typeDescriptionsPath;
        private final Path typeFieldsPath;
        private final Path queryDescriptionsPath;
        private final Path queryArgumentsPath;

        private GraphQLSchema graphQLSchema ;

        public Generator(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema ;

            Path typeDefinitionsPath = typeDefinitionsDirectory != null
                ? outputPath.resolve(typeDefinitionsDirectory) : outputPath;

            this.typeDescriptionsPath = typeDefinitionsPath.resolve(descriptionsDirectory);
            this.typeFieldsPath = typeDefinitionsPath.resolve(fieldsDirectory);

            Path queryDefinitionsPath = queryDefinitionsDirectory != null
                ? outputPath.resolve(queryDefinitionsDirectory) : outputPath;

            this.queryDescriptionsPath = queryDefinitionsPath.resolve(descriptionsDirectory);

            this.queryArgumentsPath = queryDefinitionsPath.resolve(argumentsDirectory);
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(typeDescriptionsPath) ;
                Files.createDirectories(typeFieldsPath) ;
                Files.createDirectories(queryDescriptionsPath) ;
                Files.createDirectories(queryArgumentsPath) ;

                return generateMarkdownFiles(graphQLSchema) ;
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateMarkdownFiles(GraphQLSchema graphQLSchema) {
            return Stream.of(
                    graphQLSchema.getAllTypesAsList().stream()
                        .map(this::generateTypeMarkdown)
                        .collect(Response.mergeLists()),
                    graphQLSchema.getQueryType().getFields().stream()
                        .map(this::generateQueryMarkdown)
                        .collect(Response.mergeLists())
                ).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> generateTypeMarkdown(GraphQLNamedType graphQLNamedType) {
            if (graphQLNamedType instanceof GraphQLObjectType graphQLObjectType) {
                return generateMarkdownForObjectType(graphQLObjectType) ;
            } else if (graphQLNamedType instanceof GraphQLInputObjectType graphQLInputObjectType) {
                return generateMarkdownForObjectType(graphQLInputObjectType) ;
            } else if (graphQLNamedType instanceof GraphQLInterfaceType graphQLInterfaceType) {
                return generateMarkdownForInterfaceType(graphQLInterfaceType) ;
            } else if (graphQLNamedType instanceof GraphQLEnumType graphQLEnumType) {
                return generateMarkdownForEnumType(graphQLEnumType) ;
            } else if (graphQLNamedType instanceof GraphQLScalarType) {
                return Response.empty() ;
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s' with class '%s'",
                graphQLNamedType.getName(), graphQLNamedType.getClass().getSimpleName()));
        }

        private Response<List<Path>> generateMarkdownForObjectType(GraphQLObjectType objectType) {
            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", objectType.getName()));
            Path fieldsPath = this.typeFieldsPath.resolve(objectType.getName());

            try {
                Files.createDirectories(fieldsPath) ;

                return writeToFile(descriptionPath, objectType.getDescription())
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> generateMarkdownForFields(fieldsPath, objectType.getFields()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths)) ;
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateMarkdownForObjectType(GraphQLInputObjectType objectType) {
            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", objectType.getName()));
            Path fieldsPath = this.typeFieldsPath.resolve(objectType.getName());

            try {
                Files.createDirectories(fieldsPath) ;

                return writeToFile(descriptionPath, objectType.getDescription())
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> generateMarkdownForInputFields(fieldsPath, objectType.getFields()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths)) ;
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateQueryMarkdown(GraphQLFieldDefinition graphQLFieldDefinition) {
            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = queryDescriptionsPath.resolve(String.format("%s.md", graphQLFieldDefinition.getName()));
            Path fieldsPath = this.queryArgumentsPath.resolve(graphQLFieldDefinition.getName());

            try {
                Files.createDirectories(fieldsPath) ;

                return writeToFile(descriptionPath, graphQLSchema.getDescription())
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> generateMarkdownForArguments(fieldsPath, graphQLFieldDefinition.getArguments()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths)) ;
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateMarkdownForFields(Path fieldsPath, List<GraphQLFieldDefinition> fields) {
            return fields.stream().map(field -> generateMarkdownForField(fieldsPath, field)).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> generateMarkdownForField(Path path, GraphQLFieldDefinition field) {
            Path fieldPath = path.resolve(String.format("%s.md", field.getName()));

            return writeToFile(fieldPath, field.getDescription()) ;
        }

        private Response<List<Path>> generateMarkdownForInputFields(Path fieldsPath, List<GraphQLInputObjectField> fields) {
            return fields.stream().map(field -> generateMarkdownForInputField(fieldsPath, field)).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> generateMarkdownForInputField(Path path, GraphQLInputObjectField field) {
            Path fieldPath = path.resolve(String.format("%s.md", field.getName()));

            return writeToFile(fieldPath, field.getDescription()) ;
        }

        private Response<List<Path>> generateMarkdownForInterfaceType(GraphQLInterfaceType interfaceType) {
            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", interfaceType.getName()));
            Path fieldsPath = this.typeFieldsPath.resolve(interfaceType.getName());

            try {
                Files.createDirectories(fieldsPath) ;

                return writeToFile(descriptionPath, interfaceType.getDescription())
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> generateMarkdownForFields(fieldsPath, interfaceType.getFieldDefinitions()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths)) ;
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private Response<List<Path>> generateMarkdownForEnumType(GraphQLEnumType graphQLEnumType) {

            List<Path> paths = new ArrayList<>() ;
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", graphQLEnumType.getName()));

            return writeToFile(descriptionPath, createDescription(graphQLEnumType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths)) ;
        }

        private String createDescription(GraphQLEnumType graphQLEnumType) {
            StringBuilder description = new StringBuilder(graphQLEnumType.getDescription() != null ? graphQLEnumType.getDescription() : "");

            description.append("\n\n Possible values are: \n");

            for (GraphQLEnumValueDefinition value : graphQLEnumType.getValues()) {
                description
                    .append("* ")
                    .append(value.getName())
                    .append("\n");
            }

            return description.toString();
        }

        private Response<List<Path>> writeToFile(Path path, String text) {
            try {
                if (overwrite && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return success(Collections.emptyList()) ;
                } else {
                    PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset()));
                    printWriter.println(text != null ? text : "TODO description");
                    printWriter.close();
                    return success(Collections.singletonList(path)) ;
                }
            } catch (IOException exception){
                return fail(Response.ErrorType.VALIDATION, path, String.format("Can not write to file due to %s", exception.getMessage())) ;
            }
        }

        private Response<List<Path>> generateMarkdownForArguments(Path argumentsPath, List<GraphQLArgument> arguments) {
            return arguments.stream().map(argument -> generateMarkdownForArgument(argumentsPath, argument)).collect(Response.mergeLists()) ;
        }

        private Response<List<Path>> generateMarkdownForArgument(Path path, GraphQLArgument argument) {
            Path argumentsPath = path.resolve(String.format("%s.md", argument.getName()));

            return writeToFile(argumentsPath, argument.getDescription()) ;
        }
    }


}
