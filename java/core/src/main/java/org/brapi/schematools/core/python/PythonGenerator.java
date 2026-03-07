package org.brapi.schematools.core.python;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.python.metadata.PythonGeneratorMetadata;
import org.brapi.schematools.core.python.options.PythonGeneratorOptions;
import org.brapi.schematools.core.python.thymeleaf.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.utils.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.toSnakeCase;

/**
 * Generates Python Client from a BrAPI JSON Schema.
 */
@Slf4j
@AllArgsConstructor
public class PythonGenerator {
    private final BrAPISchemaReader schemaReader;
    private final PythonGeneratorOptions options;
    private final Path outputPath;
    private final String commentPrefix = "# ";

    private final TemplateEngine templateEngine = createTemplateEngine();

    /**
     * Creates a PythonGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link PythonGeneratorOptions}.
     *
     * @param outputPath the path of the output file or directory
     */
    public PythonGenerator(Path outputPath) {
        this(PythonGeneratorOptions.load(), outputPath);
    }

    /**
     * Creates a PythonGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link PythonGeneratorOptions}.
     *
     * @param options    The options to be used in the generation.
     * @param outputPath the path of the output file or directory
     */
    public PythonGenerator(PythonGeneratorOptions options, Path outputPath) {
        this(new BrAPISchemaReader(options.getBrAPISchemaReader()), options, outputPath);
    }

    /**
     * Generates Python entity files from the complete BrAPI Specification in a directory
     * that contains subdirectories for each module with BrAPI JSON schemas and additional
     * subdirectories called 'Requests' for request schemas and BrAPI-Common for common schemas.
     *
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the paths of the Python files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory) {
        return generate(schemaDirectory, PythonGeneratorMetadata.load());
    }

    /**
     * Generates Python entity files from the complete BrAPI Specification.
     *
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @param metadata        the metadata for the generator
     * @return the paths of the Python files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory, PythonGeneratorMetadata metadata) {
        return schemaReader.readDirectories(schemaDirectory)
            .mapResultToResponse(brAPISchemas -> new Generator(brAPISchemas, metadata).generate());
    }

    private class Generator {
        private final BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache;
        private final PythonGeneratorMetadata metadata;

        public Generator(List<BrAPIClass> brAPIClasses, PythonGeneratorMetadata metadata) {
            this.brAPIClassCache = BrAPIClassCacheBuilder.createCache(this::isGenerating, brAPIClasses);
            this.metadata = metadata;
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(outputPath);

                List<Path> paths = new ArrayList<>();

                return createBrapiClient(brAPIClassCache.getPrimaryClasses())
                    .onSuccessDoWithResult(paths::add)
                    .map(() -> createCommonClasses(brAPIClassCache.getAllDependencies()))
                    .onSuccessDoWithResult(paths::add)
                    .map(() -> brAPIClassCache.getPrimaryClasses()
                        .stream()
                        .map(this::createPythonEntityClass)
                        .collect(Response.mergeLists()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths));
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<Path> createBrapiClient(List<BrAPIClass> entityClasses) {
            Context context = new Context();

            List<Map<String, String>> entries = entityClasses.stream()
                .filter(c -> c instanceof BrAPIObjectType)
                .map(c -> {
                    String entityName     = c.getName();
                    String entityLower    = StringUtils.toParameterCase(entityName);
                    String queryClassName = entityName + "Query";
                    String moduleLower    = StringUtils.toLowerCase(entityName);
                    return Map.of(
                        "entityName",     entityName,
                        "functionName",   entityLower,
                        "queryClassName", queryClassName,
                        "moduleName",     moduleLower
                    );
                }).toList();

            // Import lines: ".entities.germplasm import GermplasmQuery"
            List<String> imports = entries.stream()
                .map(e -> ".entities." + e.get("moduleName") + " import " + e.get("queryClassName"))
                .toList();

            context.setVariable("entries", entries);
            context.setVariable("imports", imports);

            String text = templateEngine.process("BrapiClient.txt", context);
            return writeToFile(
                outputPath.resolve(metadata.getFilePrefix() + "client.py"),
                "BrapiClient",
                text
            );
        }

        private Response<Path> createCommonClasses(List<BrAPIClass> commonClasses) {
            Context context = new Context();

            List<ClassModel> commonModels = commonClasses.stream()
                .map(this::createClassModel)
                .toList();

            context.setVariable("commonModels", commonModels);

            String text = templateEngine.process("CommonClasses.txt", context);
            return writeToFile(
                outputPath.resolve(metadata.getFilePrefix() + "common.py"),
                "BrapiClient",
                text
            );
        }

        private Response<List<Path>> createPythonEntityClass(BrAPIClass brAPIClass) {

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                BrAPIClass requestClass = brAPIClassCache.getBrAPIClass(String.format("%sRequest", brAPIObjectType.getName()));
                BrAPIObjectType requestObject = null;

                if (requestClass != null) {
                    if (requestClass instanceof BrAPIObjectType) {
                        requestObject = (BrAPIObjectType) requestClass;
                    } else {
                        return fail(Response.ErrorType.VALIDATION, String.format("Request schema for '%s' is not an object type", brAPIObjectType.getName()));
                    }
                }

                try {
                    Context context = new Context();

                    context.setVariable("brapiSchemaToolsVersion", options.getSchemaToolsVersion());

                    context.setVariable("entityName", brAPIObjectType.getName());
                    context.setVariable("entityNameSnakeCase", StringUtils.toSnakeCase(brAPIObjectType.getName()));

                    List<ClassModelField> requiredFields = new ArrayList<>();
                    List<ClassModelField> scalarFields = new ArrayList<>();
                    List<ClassModelField> nestedListFields = new ArrayList<>();
                    List<ClassModelField> relationshipFields = new ArrayList<>();

                    brAPIObjectType.getProperties().forEach(property -> {
                        if (property.getType() instanceof BrAPIPrimitiveType primitiveType) {
                            if (property.isRequired()) {
                                requiredFields.add(ClassModelField.builder().name(property.getName()).type(PythonTypeUtils.findPyType(primitiveType)).build());
                            } else {
                                scalarFields.add(ClassModelField.builder().name(property.getName()).type(PythonTypeUtils.findPyType(primitiveType)).build());
                            }
                        } else if (property.getType() instanceof BrAPIArrayType arrayType) {
                            if (arrayType.getItems() instanceof BrAPIArrayType) {
                                throw new RuntimeException(String.format("Properties '%s' with 2+ dimensions are not supported yet", property.getName()));
                            }
                            nestedListFields.add(ClassModelField.builder()
                                .name(property.getName())
                                .type(arrayType.getName())
                                .itemType(arrayType.getItems().getName())
                                .build());
                        } else if (property.getType() instanceof BrAPIObjectType objectType) {
                            relationshipFields.add(ClassModelField.builder().name(property.getName()).type(objectType.getName()).build());
                        } else if (property.getType() instanceof BrAPIReferenceType referenceType) {
                            relationshipFields.add(ClassModelField.builder().name(property.getName()).type(referenceType.getName()).build());
                        } else if (property.getType() instanceof BrAPIEnumType enumType) {
                            relationshipFields.add(ClassModelField.builder().name(property.getName()).type(enumType.getName()).build());
                        } else {
                            throw new RuntimeException(String.format("Property '%s' with type '%s' not supported yet", property.getName(), property.getType().getName()));
                        }
                    });

                    context.setVariable("getAll", options.getListGet().isGeneratingFor(brAPIObjectType));
                    context.setVariable("search", options.getSearch().isGeneratingFor(brAPIObjectType));
                    context.setVariable("create", options.getPost().isGeneratingFor(brAPIObjectType));
                    context.setVariable("update", options.getPut().isGeneratingFor(brAPIObjectType));
                    context.setVariable("delete", options.getDelete().isGeneratingFor(brAPIObjectType));
                    context.setVariable("delete", options.getTable().isGeneratingFor(brAPIObjectType));

                    context.setVariable("primaryModel", ClassModel.builder()
                        .docstring(brAPIClass.getDescription())
                        .requiredFields(requiredFields)
                        .scalarFields(scalarFields)
                        .nestedListFields(nestedListFields)
                        .relationshipFields(relationshipFields)
                        .build());

                    List<ClassModel> exclusiveDependencies = brAPIClassCache.getExclusiveDependencies(brAPIObjectType.getName())
                        .stream()
                        .map(this::createClassModel).toList() ;

                    context.setVariable("exclusiveDependencies", exclusiveDependencies);

                    List<ClassModel> commonDependencies = brAPIClassCache.getCommonDependencies(brAPIObjectType.getName())
                        .stream()
                        .map(this::createClassModel).toList() ;

                    context.setVariable("commonDependencies", commonDependencies);

                    context.setVariable("flattenConfig", FlattenConfig.builder()
                        .relationshipFields(List.of())
                        .relationshipFields(List.of())
                        .build()) ;

                    Endpoints.EndpointsBuilder endpoints = Endpoints.builder();

                    if (options.getSingleGet().isGeneratingFor(brAPIObjectType)
                        || options.getListGet().isGeneratingFor(brAPIObjectType)
                        || options.getPost().isGeneratingFor(brAPIObjectType)
                        || options.getPut().isGeneratingFor(brAPIObjectType)
                        || options.getDelete().isGeneratingFor(brAPIObjectType)) {
                        endpoints.crud(options.getPathItemNameFor(brAPIClass));
                    }

                    if (options.getSearch().isGeneratingFor(brAPIObjectType)) {
                        endpoints.search(options.getPathItemNameFor(brAPIClass));
                    }

                    if (options.getTable().isGeneratingFor(brAPIObjectType)) {
                        endpoints.table(options.getPathItemNameFor(brAPIClass));
                    }

                    context.setVariable("endpoints", endpoints.build()) ;

                    ArrayList<FilterMethod> filterMethods = new ArrayList<>();

                    if (requestClass != null) {
                        requestObject.getProperties().forEach(property -> {
                            filterMethods.add(FilterMethod.builder()
                                .methodName(property.getName())
                                .argName(property.getName())
                                .groupComment(property.getName())
                                //.exampleArg()
                                .docstring(property.getDescription())
                                .type(findType(property))
                                .build());
                        });
                    }

                    context.setVariable("filterMethods", filterMethods) ;

                    context.setVariable("bulkFilterParams", List.of()) ;

                    /* context.setVariable("moduleName", brAPIObjectType.getModule());
                    context.setVariable("entityPath", options.getPathItemNameFor(brAPIClass));
                    context.setVariable("searchPath", options.getSearchPathItemNameFor(brAPIClass));

                    BrAPIClass requestSchema = brAPIClassCache.getBrAPIClass(
                        String.format("%sRequest", brAPIObjectType.getName()));

                    if (requestSchema instanceof BrAPIObjectType requestObjectType) {
                        context.setVariable("requestArguments",
                            requestObjectType.getProperties().stream()
                                .map(BrAPIObjectProperty::getName).toList());
                        context.setVariable("queryParameters",
                            requestObjectType.getProperties().stream()
                                .map(BrAPIObjectProperty::getName)
                                .map(options::getSingularForProperty).toList());
                        context.setVariable("argumentDescriptions",
                            requestObjectType.getProperties().stream()
                                .map(this::getDescription).toList());
                    }*/

                    String text = templateEngine.process("EntityClass.txt", context);

                    return writeToFile(createPathForEntityClass(brAPIObjectType), brAPIObjectType.getName(), text)
                        .mapResult(List::of);
                } catch (RuntimeException e) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Error processing class '%s': %s", brAPIClass.getName(), e.getMessage()));
                }
            } else {
                return fail(Response.ErrorType.VALIDATION,
                    brAPIClass.getName() + " is not an object class");
            }
        }

        private ClassModel createClassModel(BrAPIClass brAPIClass) {
            ClassModel.ClassModelBuilder builder = ClassModel.builder()
                .name(brAPIClass.getName());

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                builder.requiredFields(brAPIObjectType.getProperties()
                    .stream()
                    .filter(BrAPIObjectProperty::isRequired)
                    .map(this::createModelField).toList());

                builder.scalarFields(brAPIObjectType.getProperties()
                    .stream()
                    .filter(property -> !property.isRequired())
                    .filter(property -> property.getType() instanceof BrAPIPrimitiveType)
                    .map(this::createModelField).toList());
            }

            return builder.build() ;
        }

        private ClassModelField createModelField(BrAPIObjectProperty property) {
            return ClassModelField.builder().name(property.getName()).type(findType(property)).build() ;
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass instanceof BrAPIObjectType && brAPIClass.getMetadata() != null &&
                brAPIClass.getMetadata().isPrimaryModel() && options.isGeneratingFor(brAPIClass);
        }

        private String findType(BrAPIObjectProperty property) {

            BrAPIType type = property.getType() ;

            if (type instanceof BrAPIPrimitiveType primitiveType) {
                return PythonTypeUtils.findPyType(primitiveType);
            } else if (type instanceof BrAPIArrayType arrayType) {
                if (arrayType.getItems() instanceof BrAPIArrayType) {
                    throw new RuntimeException(String.format("Properties '%s' with 2+ dimensions are not supported yet", property.getName()));
                }
                return arrayType.getItems().getName() + "[]";
            } else if (property.getType() instanceof BrAPIObjectType objectType) {
                return objectType.getName() ;
            } else if (property.getType() instanceof BrAPIReferenceType referenceType) {
                return referenceType.getName() ;
            } else if (property.getType() instanceof BrAPIEnumType enumType) {
                return enumType.getName() ;
            } else {
                throw new RuntimeException(String.format("Property '%s' with type '%s' not supported yet", property.getName(), property.getType().getName()));
            }
        }

        private String getDescription(BrAPIObjectProperty brAPIObjectProperty) {
            return StringUtils.extractFirstLine(brAPIObjectProperty.getDescription());
        }

        private Path createPathForEntityClass(BrAPIObjectType brAPIObjectType) {
            return outputPath.resolve(options.getEntitiesDirectory()).resolve(metadata.getFilePrefix() + toSnakeCase(brAPIObjectType.getName()) + ".py");
        }

        private Response<Path> writeToFile(Path path, String name, String text) {
            try {
                if (!options.isOverwritingExistingFiles() && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return Response.empty();
                } else {
                    Files.createDirectories(path.getParent());

                    PrintWriter printWriter = new PrintWriter(
                        Files.newBufferedWriter(path, Charset.defaultCharset()));

                    printWriter.print(commentPrefix);
                    printWriter.println(name);

                    printWriter.println(text);

                    if (options.isAddingGeneratorComments()) {
                        printWriter.println();
                        printWriter.print(commentPrefix);
                        printWriter.println("Generated by Schema Tools "
                            + this.getClass().getSimpleName()
                            + " Version: '" + options.getSchemaToolsVersion() + "'");
                    }

                    printWriter.close();
                    return success(path);
                }
            } catch (IOException exception) {
                return fail(Response.ErrorType.VALIDATION, path,
                    String.format("Can not write to file due to %s", exception.getMessage()));
            }
        }
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("PythonTemplates/");
        resolver.setSuffix(".txt");
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
