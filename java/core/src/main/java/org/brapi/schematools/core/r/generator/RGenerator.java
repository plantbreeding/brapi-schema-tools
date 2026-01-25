package org.brapi.schematools.core.r.generator;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.r.metadata.RGeneratorMetadata;
import org.brapi.schematools.core.r.options.RGeneratorOptions;
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

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
/**
 * Generates R Client from a BrAPI JSON Schema.
 */
@Slf4j
@AllArgsConstructor
public class RGenerator {
    private final BrAPISchemaReader schemaReader ;
    private final RGeneratorOptions options ;
    private final Path outputPath ;
    private final String commentPrefix = "# " ;

    private final TemplateEngine templateEngine = createTemplateEngine();

    /**
     * Creates a RGenerator using a default {@link BrAPISchemaReader} and
     * the default {@link RGeneratorOptions}.
     * @param outputPath the path of the output file or directory
     */
    public RGenerator(Path outputPath) {
        this(new BrAPISchemaReader(), RGeneratorOptions.load(), outputPath) ;
    }

    /**
     * Creates a RGenerator using a default {@link BrAPISchemaReader} and
     * the provided {@link RGeneratorOptions}.
     * @param options The options to be used in the generation.
     * @param outputPath the path of the output file or directory
     */
    public RGenerator(RGeneratorOptions options, Path outputPath) {
        this(new BrAPISchemaReader(), options, outputPath) ;
    }

    /**
     * Generates SQL files for type and their field descriptions
     * from the complete BrAPI Specification in
     * a directory contains a subdirectories for each module that contain
     * the BrAPI JSON schema and the additional subdirectories called 'Requests'
     * that contains the request schemas and BrAPI-Common that contains common schemas
     * for use across modules.
     * @param schemaDirectory the path to the complete BrAPI Specification
     * @return the paths of the Markdown files generated from the complete BrAPI Specification
     */
    public Response<List<Path>> generate(Path schemaDirectory, RGeneratorMetadata metadata) {
        return schemaReader.readDirectories(schemaDirectory)
            .mapResultToResponse(brAPISchemas -> new Generator(brAPISchemas, metadata).generate()) ;
    }

    private class Generator {
        private final BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache;
        private final RGeneratorMetadata metadata ;
        public Generator(List<BrAPIClass> brAPIClasses, RGeneratorMetadata metadata) {
            this.brAPIClassCache = BrAPIClassCacheBuilder.createCache(brAPIClasses) ;
            this.metadata = metadata ;
        }

        public Response<List<Path>> generate() {
            try {
                Files.createDirectories(outputPath) ;

                List<BrAPIClass> entityClasses = brAPIClassCache.getBrAPICClassesAsList()
                    .stream()
                    .filter(this::isGenerating).toList();

                List<Path> paths = new ArrayList<>();

                return createBrAPIClient(entityClasses)
                    .onSuccessDoWithResult(paths::add)
                    .map(() -> entityClasses
                            .stream()
                            .map(this::createR6ClassForModel)
                            .collect(Response.mergeLists()))
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> success(paths));
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
            }
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass instanceof BrAPIObjectType && brAPIClass.getMetadata() != null &&
                brAPIClass.getMetadata().isPrimaryModel() && options.isGeneratingFor(brAPIClass);
        }

        private Response<Path> createBrAPIClient(List<BrAPIClass> entityClasses) {
            Context context = new Context();

            context.setVariable("classNames", entityClasses.stream().map(options::getPluralFor).toList());
            context.setVariable("functionNames", entityClasses.stream().map(options::getPluralFor).map(StringUtils::toParameterCase).toList());
            context.setVariable("entityNames", entityClasses.stream().map(BrAPIClass::getName).toList());

            String text = templateEngine.process("BrAPIClient.txt", context) ;

            return writeToFile(outputPath.resolve(metadata.getFilePrefix() + "BrAPIClient.R"), "BrAPIClient", text) ;
        }

        private Response<List<Path>> createR6ClassForModel(BrAPIClass brAPIClass) {
            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                Context context = new Context();
                context.setVariable("className", options.getPluralFor(brAPIClass));
                context.setVariable("entityPath", options.getPathItemNameFor(brAPIClass));
                context.setVariable("entityName", brAPIObjectType.getName());
                context.setVariable("get-description", options.getSingleGet().getDescriptionFor(brAPIClass.getName()));
                context.setVariable("fields", brAPIObjectType.getProperties().stream()
                    .map(f -> f.getName() + " : " + f.getType())
                    .toList());

                context.setVariable("get", options.getSingleGet().isGeneratingFor(brAPIObjectType));
                context.setVariable("getAll", options.getListGet().isGeneratingFor(brAPIObjectType));
                context.setVariable("search", options.getSearch().isGeneratingFor(brAPIObjectType));
                context.setVariable("create", options.getPost().isGeneratingFor(brAPIObjectType));
                context.setVariable("update", options.getPut().isGeneratingFor(brAPIObjectType));
                context.setVariable("delete", options.getDelete().isGeneratingFor(brAPIObjectType));

                String text = templateEngine.process("EntityClass.txt", context) ;

                return writeToFile(createPathForEntityClass(brAPIObjectType), brAPIObjectType.getName(), text)
                    .mapResult(List::of) ;
            } else {
                return fail(Response.ErrorType.VALIDATION, brAPIClass.getName() + " is not a object class") ;
            }
        }

        private Path createPathForEntityClass(BrAPIObjectType brAPIObjectType) {
            return outputPath.resolve(metadata.getFilePrefix() + brAPIObjectType.getName() + ".R");
        }

        private Response<Path> writeToFile(Path path, String name, String text) {
            try {
                if (!options.isOverwritingExistingFiles() && Files.exists(path)) {
                    log.warn("Output file '{}' already exists and was not overwritten", path);
                    return Response.empty() ;
                } else {
                    Files.createDirectories(path.getParent()) ;

                    PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(path, Charset.defaultCharset()));

                    printWriter.print(commentPrefix) ;
                    printWriter.println(name);

                    printWriter.println(text);

                    if (options.isAddingGeneratorComments()) {
                        printWriter.println();
                        printWriter.print(commentPrefix) ;
                        printWriter.println("Generated by Schema Tools " + this.getClass().getSimpleName() + " Version: '" + options.getSchemaToolsVersion() +"'");
                    }

                    printWriter.close();
                    return success(path) ;
                }
            } catch (IOException exception){
                return fail(Response.ErrorType.VALIDATION, path, String.format("Can not write to file due to %s", exception.getMessage())) ;
            }
        }
    }

    private TemplateEngine createTemplateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("RTemplates/");
        resolver.setSuffix(".txt");
        resolver.setTemplateMode("TEXT");
        resolver.setCharacterEncoding("UTF-8");
        TemplateEngine engine = new TemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }
}
