package org.brapi.schematools.core.markdown;

import graphql.schema.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.groupingBy;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

/**
 * Generates a Markdown files for type and their field descriptions from a GraphQL Schema.
 * For each type in the schema a Markdown file with the same name as the type will be created.
 * For each field in a type a Markdown file with same name as the type will be created.
 * For each field in the Query type in the schema a Markdown file with the same name as the field will be created.
 * For each argument for each field in the Query type in a type a Markdown file with same name as the type will be created.
 * <p>
 * See the options to configure the generator
 */
@Slf4j
@AllArgsConstructor
public class GraphQLMarkdownGenerator {

    private Path outputPath;
    private GraphQLMarkdownGeneratorOptions options;

    private GraphQLMarkdownGenerator(Path outputPath) {
        this.outputPath = outputPath;
    }

    /**
     * Creates the default Generator.
     *
     * @param outputPath the output path for markdown files.
     * @return the default Generator
     */
    public static GraphQLMarkdownGenerator generator(Path outputPath) {
        return new GraphQLMarkdownGenerator(outputPath);
    }

    /**
     * Sets the output path.
     *
     * @param outputPath the output path for markdown files.
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator outputPath(Path outputPath) {
        this.outputPath = outputPath;
        return this;
    }

    /**
     * Overwrite the files if present.
     *
     * @param options the generator options
     * @return the writer for method chaining
     */
    public GraphQLMarkdownGenerator options(GraphQLMarkdownGeneratorOptions options) {
        this.options = options;
        return this;
    }

    /**
     * Generates Markdown files for type definitions and their field descriptions,
     * query definitions and their arguments descriptions
     * from the GraphQL schema
     *
     * @param schema the path to the complete BrAPI Specification
     * @return the paths of the Markdown files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(GraphQLSchema schema) {
        return new GraphQLMarkdownGenerator.Generator(schema).generate();
    }

    private class Generator {

        private final Path typeDescriptionsPath;
        private final Path typeFieldsPath;
        private final Path queryDescriptionsPath;
        private final Path queryArgumentsPath;

        private final GraphQLSchema graphQLSchema;
        private final Map<String, GraphQLFieldDefinition> duplicateObjectFieldDefinitions;
        private final Map<String, GraphQLInputObjectField> duplicateInputObjectFieldDefinitions;
        private final Map<String, GraphQLArgument> duplicateQueryArgumentDefinitions;

        public Generator(GraphQLSchema graphQLSchema) {
            this.graphQLSchema = graphQLSchema;

            Path typeDefinitionsPath = options.getTypeDefinitionsDirectory().isEmpty()
                ? outputPath : outputPath.resolve(options.getTypeDefinitionsDirectory());

            this.typeDescriptionsPath = typeDefinitionsPath.resolve(options.getDescriptionsDirectory());
            this.typeFieldsPath = typeDefinitionsPath.resolve(options.getFieldsDirectory());

            Path queryDefinitionsPath = options.getQueryDefinitionsDirectory().isEmpty()
                ? outputPath : outputPath.resolve(options.getQueryDefinitionsDirectory());

            this.queryDescriptionsPath = queryDefinitionsPath.resolve(options.getDescriptionsDirectory());

            this.queryArgumentsPath = queryDefinitionsPath.resolve(options.getArgumentsDirectory());

            List<GraphQLFieldDefinition> objectFieldDefinitions = new ArrayList<>(
                graphQLSchema.getAllTypesAsList().stream()
                    .filter(type -> type instanceof GraphQLObjectType)
                    .flatMap(type -> ((GraphQLObjectType) type).getFields().stream())
                    .toList());

            objectFieldDefinitions.addAll(graphQLSchema.getAllTypesAsList().stream()
                .filter(type -> type instanceof GraphQLInterfaceType)
                .flatMap(type -> ((GraphQLInterfaceType) type).getFields().stream())
                .toList());

            duplicateObjectFieldDefinitions = new TreeMap<>(objectFieldDefinitions.stream()
                .collect(groupingBy(GraphQLFieldDefinition::getName))
                .values().stream()
                .filter(fields -> fields.size() > 1)
                .map(fields -> fields.get(0))
                .collect(Collectors.toMap(GraphQLFieldDefinition::getName, java.util.function.Function.identity())));

            Map<String, List<GraphQLInputObjectField>> inputObjectFieldDefinitions = new TreeMap<>(graphQLSchema.getAllTypesAsList().stream()
                .filter(type -> type instanceof GraphQLInputObjectType)
                .flatMap(type -> ((GraphQLInputObjectType) type).getFields().stream())
                .collect(groupingBy(GraphQLInputObjectField::getName)));

            duplicateInputObjectFieldDefinitions = new TreeMap<>(inputObjectFieldDefinitions.values().stream()
                .filter(fields -> fields.size() > 1)
                .map(fields -> fields.get(0))
                .collect(Collectors.toMap(GraphQLInputObjectField::getName, identity())));

            Map<String, List<GraphQLArgument>> queryArgumentDefinitions = new TreeMap<>(graphQLSchema.getQueryType().getFields().stream()
                .flatMap(queryDefinition -> queryDefinition.getArguments().stream())
                .collect(groupingBy(GraphQLArgument::getName)));

            duplicateQueryArgumentDefinitions = new TreeMap<>(queryArgumentDefinitions.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("input") && entry.getValue().size() > 1)
                .map(Map.Entry::getValue)
                .map(fields -> fields.get(0))
                .collect(Collectors.toMap(GraphQLArgument::getName, identity())));
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(typeDescriptionsPath);
                Files.createDirectories(typeFieldsPath);
                Files.createDirectories(queryDescriptionsPath);
                Files.createDirectories(queryArgumentsPath);

                return generateMarkdownFiles(graphQLSchema);
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<List<Path>> generateMarkdownFiles(GraphQLSchema graphQLSchema) {

            List<Path> generatedPaths = new LinkedList<>();

            return Response.empty()
                .mapOnCondition(options.isCreatingTopLevelFieldDefinitions(),
                    () -> duplicateObjectFieldDefinitions.values().stream()
                        .map(this::generateMarkdownForTopLevelField).collect(Response.mergeLists())
                        .mapResult(generatedPaths::addAll))
                .mapOnCondition(options.isCreatingTopLevelFieldDefinitions(),
                    () -> duplicateInputObjectFieldDefinitions.values().stream()
                        .map(this::generateMarkdownForTopLevelField).collect(Response.mergeLists())
                        .mapResult(generatedPaths::addAll))
                .mapOnCondition(options.isCreatingTopLevelArgumentDefinitions(),
                    () -> duplicateQueryArgumentDefinitions.values().stream()
                        .map(this::generateMarkdownForTopLevelArgument).collect(Response.mergeLists())
                        .mapResult(generatedPaths::addAll))
                .map(
                    () -> graphQLSchema.getAllTypesAsList().stream()
                        .map(this::generateTypeMarkdown)
                        .collect(Response.mergeLists())
                        .mapResult(generatedPaths::addAll))
                .map(
                    () -> graphQLSchema.getQueryType().getFields().stream()
                        .map(this::generateQueryMarkdown)
                        .collect(Response.mergeLists())
                        .mapResult(generatedPaths::addAll))
                .map(() -> success(generatedPaths));
        }

        private Response<List<Path>> generateTypeMarkdown(GraphQLNamedType graphQLNamedType) {
            if (graphQLNamedType instanceof GraphQLObjectType graphQLObjectType) {
                return generateMarkdownForObjectType(graphQLObjectType);
            } else if (graphQLNamedType instanceof GraphQLInputObjectType graphQLInputObjectType) {
                return generateMarkdownForInputObjectType(graphQLInputObjectType);
            } else if (graphQLNamedType instanceof GraphQLInterfaceType graphQLInterfaceType) {
                return generateMarkdownForInterfaceType(graphQLInterfaceType);
            } else if (graphQLNamedType instanceof GraphQLEnumType graphQLEnumType) {
                return generateMarkdownForEnumType(graphQLEnumType);
            } else if (graphQLNamedType instanceof GraphQLScalarType) {
                return Response.empty();
            }

            return Response.fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s' with class '%s'",
                graphQLNamedType.getName(), graphQLNamedType.getClass().getSimpleName()));
        }

        private Response<List<Path>> generateMarkdownForObjectType(GraphQLObjectType objectType) {
            List<Path> paths = new ArrayList<>();
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", objectType.getName()));

            return writeToFile(descriptionPath, objectType.getDescription(), () -> options.getDescriptionForObjectType(objectType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> generateMarkdownForFields(objectType, objectType.getFields()))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }

        private Response<List<Path>> generateMarkdownForInputObjectType(GraphQLInputObjectType objectType) {
            List<Path> paths = new ArrayList<>();
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", objectType.getName()));

            return writeToFile(descriptionPath, objectType.getDescription(), () -> options.getDescriptionForInputObjectType(objectType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> generateMarkdownForInputFields(objectType, objectType.getFields()))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }

        private Response<List<Path>> generateQueryMarkdown(GraphQLFieldDefinition queryDefinition) {
            List<Path> paths = new ArrayList<>();
            Path descriptionPath = queryDescriptionsPath.resolve(String.format("%s.md", queryDefinition.getName()));

            return writeToFile(descriptionPath, graphQLSchema.getDescription(), () -> options.getDescriptionForQuery(queryDefinition))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> generateMarkdownForArguments(queryDefinition, queryDefinition.getArguments()))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }

        private Response<List<Path>> generateMarkdownForTopLevelField(GraphQLFieldDefinition field) {
            Path fieldPath = typeFieldsPath.resolve(String.format("%s.md", field.getName()));

            return writeToFile(fieldPath, field.getDescription(), () -> options.getDescriptionForField(null, field));
        }

        private Response<List<Path>> generateMarkdownForTopLevelField(GraphQLInputObjectField field) {
            Path fieldPath = typeFieldsPath.resolve(String.format("%s.md", field.getName()));

            return writeToFile(fieldPath, field.getDescription(), () -> options.getDescriptionForInputField(null, field));
        }

        private Response<List<Path>> generateMarkdownForFields(GraphQLNamedType type, List<GraphQLFieldDefinition> fields) {
            return fields.stream().map(field -> generateMarkdownForField(type, field)).collect(Response.mergeLists());
        }

        private Response<List<Path>> generateMarkdownForField(GraphQLNamedType type, GraphQLFieldDefinition field) {
            if (!options.isCreatingTopLevelFieldDefinitions() || !duplicateObjectFieldDefinitions.containsKey(field.getName())) {
                return createPath(this.typeFieldsPath, type.getName(), String.format("%s.md", field.getName()))
                    .mapResultToResponse(filePath -> writeToFile(filePath, field.getDescription(), () -> options.getDescriptionForField(type, field)));
            } else {
                log.warn("Skipping duplicate field '{}' in type '{}', it was created in top level", type.getName(), field.getName());
                return success(Collections.emptyList());
            }
        }

        private Response<Path> createPath(Path parentPath, String directory, String file) {
            Path path = parentPath.resolve(directory) ;
            try {
                Files.createDirectories(path);
                return success(path.resolve(file));
            } catch (IOException e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<List<Path>> generateMarkdownForInputFields(GraphQLInputObjectType type, List<GraphQLInputObjectField> fields) {
            return fields.stream().map(field -> generateMarkdownForInputField(type, field)).collect(Response.mergeLists());
        }

        private Response<List<Path>> generateMarkdownForInputField(GraphQLInputObjectType type, GraphQLInputObjectField field) {
            if (!options.isCreatingTopLevelFieldDefinitions() || !duplicateInputObjectFieldDefinitions.containsKey(field.getName())) {
                return createPath(this.typeFieldsPath, type.getName(), String.format("%s.md", field.getName()))
                    .mapResultToResponse(filePath -> writeToFile(filePath, field.getDescription(), () -> options.getDescriptionForInputField(type, field)));
            } else {
                log.warn("Skipping duplicate field '{}' in type '{}', it was created in top level", type.getName(), field.getName());
                return success(Collections.emptyList());
            }
        }

        private Response<List<Path>> generateMarkdownForInterfaceType(GraphQLInterfaceType interfaceType) {
            List<Path> paths = new ArrayList<>();
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", interfaceType.getName()));

            return writeToFile(descriptionPath, interfaceType.getDescription(), () -> options.getDescriptionForInterface(interfaceType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> generateMarkdownForFields(interfaceType, interfaceType.getFieldDefinitions()))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }

        private Response<List<Path>> generateMarkdownForEnumType(GraphQLEnumType graphQLEnumType) {
            List<Path> paths = new ArrayList<>();
            Path descriptionPath = typeDescriptionsPath.resolve(String.format("%s.md", graphQLEnumType.getName()));

            return writeToFile(descriptionPath, graphQLEnumType.getDescription(), () -> createEnumDescription(graphQLEnumType))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> success(paths));
        }

        private Response<List<Path>> generateMarkdownForTopLevelArgument(GraphQLArgument argument) {
            Path fieldPath = queryArgumentsPath.resolve(String.format("%s.md", argument.getName()));

            return writeToFile(fieldPath, argument.getDescription(), () -> options.getDescriptionForArgument(null, argument));
        }

        private Response<List<Path>> generateMarkdownForArguments(GraphQLFieldDefinition queryDefinition, List<GraphQLArgument> arguments) {
            return arguments.stream().map(argument -> generateMarkdownForArgument(queryDefinition, argument)).collect(Response.mergeLists());
        }

        private Response<List<Path>> generateMarkdownForArgument(GraphQLFieldDefinition queryDefinition, GraphQLArgument argument) {
            if (!options.isCreatingTopLevelArgumentDefinitions() || !duplicateQueryArgumentDefinitions.containsKey(argument.getName())) {
                return createPath(this.queryArgumentsPath, queryDefinition.getName(), String.format("%s.md", argument.getName()))
                    .mapResultToResponse(filePath -> writeToFile(filePath, argument.getDescription(), () -> options.getDescriptionForArgument(queryDefinition, argument)));
            } else {
                log.warn("Skipping duplicate argument '{}' in query '{}', it was created in top level", argument.getName(), queryDefinition.getName());
                return success(Collections.emptyList());
            }
        }

        private String createEnumDescription(GraphQLEnumType graphQLEnumType) {
            StringBuilder description = new StringBuilder(graphQLEnumType.getDescription() != null
                ? graphQLEnumType.getDescription() : options.getDescriptionForEnum(graphQLEnumType));

            description.append("\n\n Possible values are: \n");

            for (GraphQLEnumValueDefinition value : graphQLEnumType.getValues()) {
                description
                    .append("* ")
                    .append(value.getName())
                    .append("\n");
            }

            return description.toString();
        }

        private Response<List<Path>> writeToFile(Path path, String text, Supplier<String> descriptionProvider) {
            try {
                if (options.isOverwritingExistingFiles() && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return success(Collections.emptyList());
                } else {
                    PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset()));
                    printWriter.println(text != null ? text : descriptionProvider.get());
                    printWriter.close();
                    return success(Collections.singletonList(path));
                }
            } catch (IOException exception) {
                return fail(Response.ErrorType.VALIDATION, path, String.format("Can not write to file due to %s", exception.getMessage()));
            }
        }
    }
}
