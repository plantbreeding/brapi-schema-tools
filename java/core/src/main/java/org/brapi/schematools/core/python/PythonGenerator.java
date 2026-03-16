package org.brapi.schematools.core.python;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.python.metadata.PythonGeneratorMetadata;
import org.brapi.schematools.core.python.options.PythonGeneratorOptions;
import org.brapi.schematools.core.model.BrAPIPrimitiveType;
import org.brapi.schematools.core.python.thymeleaf.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.options.LinkType;
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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.StringUtils.toSnakeCase;
import static org.brapi.schematools.core.python.PythonTypeUtils.findPyType;

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

                return brAPIClassCache.getPrimaryClasses()
                    .stream()
                    .map(this::createPrimaryModel)
                    .collect(Response.toList())
                    .mapResultToResponse(this::createBrapiClient)
                    .onSuccessDoWithResult(paths::addAll)
                    .map(() -> createCommonClasses(brAPIClassCache.getAllNonPrimaryDependencies()))
                    .onSuccessDoWithResult(paths::add)
                    .map(() -> success(paths));
            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, e.getMessage());
            }
        }

        private Response<List<Path>> createBrapiClient(List<ClassModel> entityClasses) {
            Context context = new Context();

            context.setVariable("entityClasses", entityClasses);

            List<Dependency> entityClassDependencies = entityClasses.stream()
                .map(classModel -> Dependency.builder()
                    .name(classModel.getQueryClassName())
                    .module("brapi.entities." + metadata.getFilePrefix() + toSnakeCase(classModel.getName()))
                    .build())
                .toList();

            context.setVariable("entityClassDependencies", entityClassDependencies);

            String text = templateEngine.process("BrapiClient.txt", context);

            List<Path> paths = new ArrayList<>();

            return writeToFile(outputPath.resolve(metadata.getFilePrefix() + "client.py"), "BrapiClient", text)
                .onSuccessDoWithResult(paths::add)
                .map(() -> entityClasses
                    .stream()
                    .map(this::createPythonEntityClass)
                    .collect(Response.toList()))
                .onSuccessDoWithResult(paths::addAll)
                .map(() -> createEntitiesInit(entityClasses))
                .onSuccessDoWithResult(paths::add)
                .map(() -> success(paths));
        }

        private Response<Path> createCommonClasses(List<BrAPIClass> brAPIClasses) {
            Context context = new Context();

            List<ClassModel> commonClasses = brAPIClasses.stream()
                .map(this::createClassModel)
                .toList();

            context.setVariable("commonClasses", commonClasses);

            Map<String, Dependency> entityClassDependencies = commonClasses.stream()
                .flatMap(classModel -> brAPIClassCache.getPrimaryDependencies(classModel.getName()).stream())
                .map(b -> Dependency.builder()
                    .name(b.getName())
                    .module("brapi.entities." + metadata.getFilePrefix() + toSnakeCase(b.getName()))
                    .build())
                .collect(Collectors.toMap(Dependency::getName, Function.identity(), (d1, d2) -> d1));

            context.setVariable("entityClassDependencies", entityClassDependencies.values());

            String text = templateEngine.process("CommonClasses.txt", context);
            return writeToFile(
                outputPath.resolve(metadata.getFilePrefix() + "common.py"),
                "BrapiClient",
                text
            );
        }

        private Response<Path> createPythonEntityClass(ClassModel primaryModel) {
            Context context = new Context();

            context.setVariable("brapiSchemaToolsVersion", options.getSchemaToolsVersion());

            String fileName = metadata.getFilePrefix() + toSnakeCase(primaryModel.getName()) + ".py";
            context.setVariable("fileName", fileName);
            context.setVariable("commonModule", metadata.getFilePrefix() + "common");
            context.setVariable("primaryModel", primaryModel);

            // makes the template easier to read
            context.setVariable("entityName", primaryModel.getName());
            context.setVariable("queryClassName", primaryModel.getQueryClassName());
            context.setVariable("queryFunctionName", primaryModel.getQueryFunctionName());
            context.setVariable("entityNameSnakeCase", primaryModel.getNameSnakeCase());
            context.setVariable("entityPluralNameSnakeCase", primaryModel.getPluralNameSnakeCase());

            context.setVariable("idArgumentName", primaryModel.getIdArgumentName());
            context.setVariable("idPropertyName", primaryModel.getIdPropertyName());


            context.setVariable("commonDependencies", primaryModel.getCommonDependencies());
            context.setVariable("exclusiveDependencies", primaryModel.getExclusiveDependencies());
            context.setVariable("primaryDependencies", primaryModel.getPrimaryDependencies());
            context.setVariable("flattenConfig", primaryModel.getFlattenConfig());
            context.setVariable("endpoints", primaryModel.getEndpoints());
            context.setVariable("pluralToSingularGetParams", primaryModel.getPluralToSingularGetParams());
            context.setVariable("unchangedGetParams", primaryModel.getUnchangedGetParams());
            context.setVariable("ignoreGetParams", primaryModel.getIgnoreGetParams());

            context.setVariable("filters", primaryModel.getFilters());
            context.setVariable("exampleFilters", primaryModel.getExampleFilters());

            String text = templateEngine.process("EntityClass.txt", context);

            return writeToFile(createPathForEntityClass(fileName), primaryModel.getName(), text);
        }

        private Response<Path> createEntitiesInit(List<ClassModel> entityClasses) {
            Context context = new Context();

            context.setVariable("brapiSchemaToolsVersion", options.getSchemaToolsVersion());

            List<EntityModuleDescription> entityModules = entityClasses.stream()
                .map(classModel -> EntityModuleDescription.builder()
                    .classNames(findClassNamesForEntityClass(classModel))
                    .moduleName(metadata.getFilePrefix() + toSnakeCase(classModel.getName()))
                    .build())
                .toList();

            context.setVariable("entityModules", entityModules);

            context.setVariable("commonModule", metadata.getFilePrefix() + "common");

            String text = templateEngine.process("EntitiesInit.txt", context);

            return writeToFile(
                createPathForEntityClass("__init__.py"),
                "EntitiesInit",
                text
            );
        }

        private List<String> findClassNamesForEntityClass(ClassModel entityClass) {
            List<String> classNames = new ArrayList<>();

            classNames.add(entityClass.getName());
            classNames.add(entityClass.getQueryClassName());

            // Use the transitive exclusive deps already computed in createPrimaryModel
            classNames.addAll(entityClass.getExclusiveDependencies().stream().map(ClassModel::getName).toList());

            return classNames;
        }

        private Response<ClassModel> createPrimaryModel(BrAPIClass brAPIClass) {

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                ClassModel.ClassModelBuilder builder = ClassModel.builder()
                    .name(brAPIObjectType.getName())
                    .queryFunctionName(StringUtils.toSnakeCase(brAPIObjectType.getName()))
                    .nameSnakeCase(StringUtils.toSnakeCase(brAPIObjectType.getName()))
                    .pluralNameSnakeCase(StringUtils.toSnakeCase(options.getPluralFor(brAPIObjectType)))
                    .docstring(brAPIClass.getDescription()) ;

                try {
                    List<ClassModelField> requiredFields = new ArrayList<>();
                    List<ClassModelField> scalarFields = new ArrayList<>();
                    List<ClassModelField> nestedListFields = new ArrayList<>();
                    List<ClassModelField> relationshipFields = new ArrayList<>();
                    List<ClassModelField> flattenNestedListFields = new ArrayList<>();
                    List<ClassModelField> flattenRelationshipFields = new ArrayList<>();

                    brAPIObjectType.getProperties().forEach(property -> {
                        boolean isDeprecatedAndIgnored =
                            options.getBrAPISchemaReader().isIgnoringDepreciatedProperties() && property.isDeprecated();

                        if (property.getType() instanceof BrAPIPrimitiveType primitiveType) {
                            if (property.isRequired()) {
                                requiredFields.add(ClassModelField.builder().name(property.getName()).type(findPyType(primitiveType).getResultOrThrow()).build());
                            } else {
                                scalarFields.add(ClassModelField.builder().name(property.getName()).type(findPyType(primitiveType).getResultOrThrow()).build());
                            }
                        } else if (property.getType() instanceof BrAPIArrayType arrayType) {
                            if (arrayType.getItems() instanceof BrAPIArrayType) {
                                throw new RuntimeException(String.format("Properties '%s' with 2+ dimensions are not supported yet", property.getName()));
                            }
                            if (isDeprecatedAndIgnored) return;
                            LinkType arrayLinkType = options.getProperties().getLinkTypeFor(brAPIObjectType, property)
                                .orElseResult(LinkType.EMBEDDED);
                            if (arrayLinkType == LinkType.NONE || arrayLinkType == LinkType.SUB_QUERY) {
                                // NONE: not in REST response; SUB_QUERY: fetched via separate endpoint — omit from model
                                return;
                            } else if (arrayLinkType == LinkType.ID) {
                                // ONE_TO_MANY relationship — REST returns an array of ID strings,
                                // e.g. observationVariables → observationVariableDbIds: List[str]
                                String idsFieldName = options.getProperties().getIdsPropertyNameFor(property);
                                nestedListFields.add(ClassModelField.builder()
                                    .name(idsFieldName)
                                    .type("array")
                                    .itemType("str")
                                    .build());
                                // Already flat strings — no to_df flattening needed
                            } else {
                                // EMBEDDED: full nested objects present in REST response
                                ClassModelField field = ClassModelField.builder()
                                    .name(property.getName())
                                    .type(arrayType.getName())
                                    .itemType(findType(arrayType.getItems()).getResultOrThrow())
                                    .build();
                                nestedListFields.add(field);
                                flattenNestedListFields.add(field);
                            }
                        } else if (property.getType() instanceof BrAPIObjectType objectType) {
                            if (isDeprecatedAndIgnored) return;
                            LinkType linkType = options.getProperties().getLinkTypeFor(brAPIObjectType, property)
                                .orElseResult(LinkType.EMBEDDED);
                            if (linkType == LinkType.NONE || linkType == LinkType.SUB_QUERY) {
                                // Not returned in the REST response — omit from the model
                            } else if (linkType == LinkType.ID) {
                                // API returns flat scalar fields (e.g. cultivarDbId, cultivarName)
                                options.getProperties().getLinkPropertiesFor(objectType).forEach(linkProp ->
                                    scalarFields.add(ClassModelField.builder().name(linkProp.getName()).type("str").build()));
                            } else {
                                // EMBEDDED: nested object present in REST response
                                ClassModelField field = ClassModelField.builder().name(property.getName()).type(objectType.getName()).build();
                                relationshipFields.add(field);
                                flattenRelationshipFields.add(field);
                            }
                        } else if (property.getType() instanceof BrAPIReferenceType referenceType) {
                            if (isDeprecatedAndIgnored) return;
                            LinkType linkType = options.getProperties().getLinkTypeFor(brAPIObjectType, property)
                                .orElseResult(LinkType.EMBEDDED);
                            if (linkType == LinkType.NONE || linkType == LinkType.SUB_QUERY) {
                                // Not returned in the REST response — omit from the model
                            } else if (linkType == LinkType.ID) {
                                // Resolve reference and expand to scalar link properties
                                BrAPIClass refClass = brAPIClassCache.getBrAPIClass(referenceType.getName());
                                if (refClass instanceof BrAPIObjectType refObjectType) {
                                    options.getProperties().getLinkPropertiesFor(refObjectType).forEach(linkProp ->
                                        scalarFields.add(ClassModelField.builder().name(linkProp.getName()).type("str").build()));
                                } else {
                                    // Fallback: treat as opaque relationship field
                                    relationshipFields.add(ClassModelField.builder().name(property.getName()).type(referenceType.getName()).build());
                                }
                            } else {
                                // EMBEDDED: nested object present in REST response
                                ClassModelField field = ClassModelField.builder().name(property.getName()).type(referenceType.getName()).build();
                                relationshipFields.add(field);
                                flattenRelationshipFields.add(field);
                            }
                        } else if (property.getType() instanceof BrAPIEnumType enumType) {
                            // Enums are scalar values in the API response — no flattening needed
                            relationshipFields.add(ClassModelField.builder().name(property.getName()).type(enumType.getName()).build());
                        } else {
                            throw new RuntimeException(String.format("Property '%s' with type '%s' not supported yet", property.getName(), property.getType().getName()));
                        }
                    });

                    builder.requiredFields(requiredFields)
                        .scalarFields(scalarFields)
                        .nestedListFields(nestedListFields)
                        .relationshipFields(relationshipFields);

                    // Collect type names directly referenced by non-deprecated properties.
                    // This prevents deprecated-only dependencies (e.g. GermplasmMCPD) from
                    // appearing in the exclusive/common dep lists.
                    Set<String> directNonDeprecatedTypeNames = new HashSet<>();
                    brAPIObjectType.getProperties().forEach(property -> {
                        if (!(options.getBrAPISchemaReader().isIgnoringDepreciatedProperties() && property.isDeprecated())) {
                            collectDirectTypeName(property.getType(), directNonDeprecatedTypeNames);
                        }
                    });

                    // BFS state — shared across entity + request-class dep collection
                    Set<String> depAllSeen = new HashSet<>();
                    Set<BrAPIClass> depExclusiveSet = new LinkedHashSet<>();
                    Set<BrAPIClass> depCommonSet = new LinkedHashSet<>();
                    Deque<BrAPIClass> depExclusiveQueue = new ArrayDeque<>();
                    Deque<BrAPIClass> depCommonQueue = new ArrayDeque<>();

                    // Seed from entity's direct (non-deprecated) exclusive deps
                    brAPIClassCache.getExclusiveDependencies(brAPIObjectType.getName()).stream()
                        .filter(dep -> directNonDeprecatedTypeNames.contains(dep.getName()))
                        .filter(dep -> depAllSeen.add(dep.getName()))
                        .forEach(dep -> { depExclusiveSet.add(dep); depExclusiveQueue.add(dep); });

                    // Seed from entity's direct (non-deprecated) common deps
                    brAPIClassCache.getCommonDependencies(brAPIObjectType.getName()).stream()
                        .filter(dep -> directNonDeprecatedTypeNames.contains(dep.getName()))
                        .filter(dep -> depAllSeen.add(dep.getName()))
                        .forEach(dep -> { depCommonSet.add(dep); depCommonQueue.add(dep); });

                    // BFS: expand deps-of-deps transitively
                    expandTransitiveDeps(depExclusiveQueue, depCommonQueue, depAllSeen, depExclusiveSet, depCommonSet);

                    // Build intermediate lists (may be rebuilt below if requestClass exists)
                    List<ClassModel> exclusiveDependencies = buildExclusiveClassModels(depExclusiveSet);
                    List<Dependency> commonDependencies = buildCommonDependencies(depCommonSet);

                    builder.exclusiveDependencies(exclusiveDependencies);
                    builder.commonDependencies(commonDependencies);

                    List<Dependency> primaryDependencies = brAPIClassCache.getPrimaryDependencies(brAPIObjectType.getName())
                        .stream()
                        .map(b -> Dependency.builder()
                            .name(b.getName())
                            .module(metadata.getFilePrefix() + toSnakeCase(b.getName()))
                            .build())
                        .toList();

                    builder.primaryDependencies(primaryDependencies);

                    builder.flattenConfig(FlattenConfig.builder()
                        .relationshipFields(flattenRelationshipFields.stream().map(ClassModelField::getName).toList())
                        .arrayFields(flattenNestedListFields.stream().map(ClassModelField::getName).toList())
                        .build());

                    Endpoints.EndpointsBuilder endpoints = Endpoints.builder();

                    if (options.getSingleGet().isGeneratingFor(brAPIObjectType) ||
                        options.getPost().isGeneratingFor(brAPIObjectType) ||
                        options.getPut().isGeneratingFor(brAPIObjectType) ||
                        options.getPost().isGeneratingFor(brAPIObjectType)) {
                        endpoints.crud(stripLeadingSlash(options.getPathItemNameFor(brAPIClass)));

                        builder.idPropertyName(options.getProperties().getIdPropertyFor(brAPIClass).getResultOrThrow().getName());
                        builder.idArgumentName(StringUtils.toSnakeCase(options.getProperties().getIdPropertyFor(brAPIClass).getResultOrThrow().getName()));
                    }

                    if (options.getListGet().isGeneratingFor(brAPIObjectType)) {
                        endpoints.crud(stripLeadingSlash(options.getPathItemNameFor(brAPIClass)));
                    }

                    if (options.getSearch().isGeneratingFor(brAPIObjectType)) {
                        endpoints.search(stripLeadingSlash(options.getSearchPathItemNameFor(brAPIClass)));
                    }

                    if (options.getTable().isGeneratingFor(brAPIObjectType)) {
                        endpoints.table(stripLeadingSlash(options.getPathItemNameFor(brAPIClass)) + "/table");
                    }

                    if (options.getSearchTable().isGeneratingFor(brAPIObjectType)) {
                        endpoints.searchTable(stripLeadingSlash(options.getSearchPathItemNameFor(brAPIClass)) + "/table");
                    }

                    endpoints.get(options.getSingleGet().isGeneratingFor(brAPIObjectType));
                    endpoints.list(options.getListGet().isGeneratingFor(brAPIObjectType));
                    endpoints.create(options.getPost().isGeneratingFor(brAPIObjectType));
                    endpoints.createMany(options.getPost().isGeneratingFor(brAPIObjectType));
                    endpoints.update(options.getPut().isGeneratingFor(brAPIObjectType));
                    endpoints.delete(options.getDelete().isGeneratingFor(brAPIObjectType));

                    builder.endpoints(endpoints.build());

                    List<Filter> filters = new ArrayList<>();

                    List<PropertyMapping> pluralToSingularGetParams = new ArrayList<>();
                    List<PropertyMapping> unchangedGetParams = new ArrayList<>();
                    List<PropertyMapping> ignoreGetParams = new ArrayList<>();

                    builder.queryClassName(String.format("%sQuery", brAPIObjectType.getName()));

                    BrAPIClass requestClass = brAPIClassCache.getBrAPIClass(String.format("%sRequest", brAPIObjectType.getName()));

                    if (requestClass != null) {
                        if (requestClass instanceof BrAPIObjectType requestObject) {
                            // Add request class deps into BFS (no deprecated filter for request params)
                            brAPIClassCache.getExclusiveDependencies(requestObject.getName()).stream()
                                .filter(dep -> depAllSeen.add(dep.getName()))
                                .forEach(dep -> { depExclusiveSet.add(dep); depExclusiveQueue.add(dep); });

                            brAPIClassCache.getCommonDependencies(requestObject.getName()).stream()
                                .filter(dep -> depAllSeen.add(dep.getName()))
                                .forEach(dep -> { depCommonSet.add(dep); depCommonQueue.add(dep); });

                            // BFS: expand transitive deps from request class
                            expandTransitiveDeps(depExclusiveQueue, depCommonQueue, depAllSeen, depExclusiveSet, depCommonSet);

                            // Rebuild and override with full lists
                            exclusiveDependencies = buildExclusiveClassModels(depExclusiveSet);
                            commonDependencies = buildCommonDependencies(depCommonSet);

                            builder.exclusiveDependencies(exclusiveDependencies);
                            builder.commonDependencies(commonDependencies);

                            // Direct property names on the primary entity (e.g. "commonCropName", "germplasmDbId").
                            // These are never prefixed with "by_" even if they match a link property of another type.
                            Set<String> ownPropertyNames = brAPIObjectType.getProperties().stream()
                                .map(BrAPIObjectProperty::getName)
                                .collect(Collectors.toSet());

                            // Build the set of link property names (e.g. "programDbId", "programName",
                            // "studyDbId", "trialDbId") by collecting link properties from ALL primary classes
                            // OTHER than this entity.  Request params whose singular form is in this set
                            // get the "by_" prefix, unless the name is also a direct property of the entity.
                            Set<String> relationshipFilterSingularNames = brAPIClassCache.getPrimaryClasses().stream()
                                .filter(cls -> !cls.getName().equals(brAPIObjectType.getName()))
                                .filter(cls -> cls instanceof BrAPIObjectType)
                                .flatMap(cls -> options.getProperties().getLinkPropertiesFor((BrAPIObjectType) cls).stream()
                                    .map(BrAPIObjectProperty::getName))
                                .filter(name -> !ownPropertyNames.contains(name))
                                .collect(Collectors.toSet());

                            requestObject.getProperties().forEach(property -> filters.add(createFilterMethod(property, relationshipFilterSingularNames)));

                            if (options.getListGet().isGeneratingFor(brAPIObjectType)) {
                                requestObject.getProperties()
                                    .forEach(property -> {
                                        PropertyMapping propertyMapping = PropertyMapping.builder()
                                            .pluralName(property.getName())
                                            .singularName(options.getSingularForProperty(property.getName()))
                                            .build();

                                        if (options.getListGet().isUsingPropertyFromRequestFor(brAPIObjectType, property)) {
                                            if (propertyMapping.isUnchanged()) {
                                                unchangedGetParams.add(propertyMapping);
                                            } else {
                                                pluralToSingularGetParams.add(propertyMapping);
                                            }
                                        } else {
                                            ignoreGetParams.add(propertyMapping);
                                        }
                                    });
                            }

                        } else {
                            return fail(Response.ErrorType.VALIDATION, String.format("Request schema for '%s' is not an object type", brAPIObjectType.getName()));
                        }
                    }

                    builder.filters(filters);
                    builder.pluralToSingularGetParams(pluralToSingularGetParams);
                    builder.unchangedGetParams(unchangedGetParams);
                    builder.ignoreGetParams(ignoreGetParams);

                    List<Filter> exampleFilters = filters.stream().filter(f -> f.getExampleArg() != null || f.getExampleArgs() != null).toList();

                    if (!exampleFilters.isEmpty()) {
                        builder.exampleFilters(exampleFilters.subList(0, Math.min(3, filters.size())));
                    } else {
                        builder.exampleFilters(List.of());
                    }

                    return success(builder.build()) ;

                } catch (RuntimeException e) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Error processing class '%s': %s", brAPIClass.getName(), e.getMessage()));
                }
            } else {
                return fail(Response.ErrorType.VALIDATION,
                    brAPIClass.getName() + " is not an object class");
            }
        }

        /**
         * Collects the direct class-level type name(s) from a BrAPIType, recursing into arrays.
         * Primitive types are skipped (no class dependency).
         */
        private static String stripLeadingSlash(String path) {
            return path != null && path.startsWith("/") ? path.substring(1) : path;
        }

        private void collectDirectTypeName(BrAPIType type, Set<String> names) {
            if (type instanceof BrAPIArrayType arrayType) {
                collectDirectTypeName(arrayType.getItems(), names);
            } else if (type instanceof BrAPIObjectType objectType) {
                names.add(objectType.getName());
            } else if (type instanceof BrAPIReferenceType referenceType) {
                names.add(referenceType.getName());
            } else if (type instanceof BrAPIEnumType enumType) {
                names.add(enumType.getName());
            }
            // BrAPIPrimitiveType → no class dependency
        }

        /**
         * BFS expansion of transitive (deps-of-deps) dependencies.
         * <ul>
         *   <li>Exclusive deps of an exclusive dep stay exclusive.</li>
         *   <li>Common deps of an exclusive dep become common.</li>
         *   <li>All deps of a common dep become common.</li>
         * </ul>
         * {@code allSeen} is used as the "already queued" guard to prevent cycles and duplicates.
         */
        private void expandTransitiveDeps(
            Deque<BrAPIClass> exclusiveQueue,
            Deque<BrAPIClass> commonQueue,
            Set<String> allSeen,
            Set<BrAPIClass> exclusiveSet,
            Set<BrAPIClass> commonSet
        ) {
            // Process the exclusive queue until empty, then the common queue once.
            // Common deps of common deps are already defined in the common file —
            // the primary entity file only needs to import things it directly uses,
            // so we do NOT expand through common deps.
            while (!exclusiveQueue.isEmpty()) {
                BrAPIClass dep = exclusiveQueue.poll();
                // exclusive → exclusive: keep exclusive
                brAPIClassCache.getExclusiveDependencies(dep.getName()).forEach(transitive -> {
                    if (allSeen.add(transitive.getName())) {
                        exclusiveSet.add(transitive);
                        exclusiveQueue.add(transitive);
                    }
                });
                // exclusive → common: import from common (but do not expand further)
                brAPIClassCache.getCommonDependencies(dep.getName()).forEach(transitive -> {
                    if (allSeen.add(transitive.getName())) {
                        commonSet.add(transitive);
                        // intentionally NOT queued — common file is self-contained
                    }
                });
            }
            // Drain the common queue (seeded by direct entity deps) — no expansion
            commonQueue.clear();
        }

        private List<ClassModel> buildExclusiveClassModels(Set<BrAPIClass> exclusiveSet) {
            return exclusiveSet.stream()
                .map(this::createClassModel)
                .collect(Collectors.toCollection(ArrayList::new));
        }

        private List<Dependency> buildCommonDependencies(Set<BrAPIClass> commonSet) {
            return commonSet.stream()
                .map(b -> Dependency.builder()
                    .name(b.getName())
                    .module(metadata.getFilePrefix() + "common")
                    .build())
                .collect(Collectors.toCollection(ArrayList::new));
        }

        private Filter createFilterMethod(BrAPIObjectProperty property, Set<String> relationshipFilterSingularNames) {
            // If the singular of the property name matches a LinkType.ID link property
            // (e.g. "programDbId", "programName"), prefix the method with "by_".
            String singularName = options.getSingularForProperty(property.getName());
            String methodName = relationshipFilterSingularNames.contains(singularName)
                ? "by_" + toSnakeCase(singularName)
                : toSnakeCase(property.getName());

            Filter.FilterBuilder builder = Filter.builder()
                .methodName(methodName)
                .paramName(property.getName())
                .argName(toSnakeCase(property.getName()))
                .groupComment(property.getName())
                .docstring(property.getDescription())
                .required(property.isRequired())
                .type(findType(property));

            if (property.getType() instanceof BrAPIArrayType brAPIArrayType) {
                builder.itemType(findType(brAPIArrayType.getItems()).getResultOrThrow());
            }

            if (!property.getExamples().isEmpty()) {
                switch (property.getType()) {
                    case BrAPIPrimitiveType primitiveType -> {
                        if (primitiveType.getName().equals(BrAPIPrimitiveType.STRING)) {
                            builder.exampleArg(String.format("\"%s\"", property.getExamples().getFirst().toString()));
                        } else {
                            builder.exampleArg(property.getExamples().getFirst().toString());
                        }
                    }
                    case BrAPIEnumType enumType -> {
                        if (enumType.getType().equals(BrAPIPrimitiveType.STRING)) {
                            builder.exampleArg(String.format("\"%s\"", property.getExamples().getFirst().toString()));
                        } else {
                            builder.exampleArg(property.getExamples().getFirst().toString());
                        }
                    }
                    case BrAPIArrayType brAPIArrayType -> {
                        if (brAPIArrayType.getItems().getName().equals(BrAPIPrimitiveType.STRING)) {
                            builder.exampleArg(String.format("\"%s\"", property.getExamples().getFirst().toString()));
                            builder.exampleArgs(String.format("[\"%s\"]", property.getExamples().getFirst().toString()));
                            if (property.getExamples().size() > 1) {
                                builder.exampleArg2(String.format("\"%s\"", property.getExamples().get(1).toString()));
                                builder.exampleMultipleArgs(String.format("[\"%s\", \"%s\"]", property.getExamples().getFirst().toString(), property.getExamples().get(1).toString()));
                            }
                        } else {
                            builder.exampleArg(property.getExamples().getFirst().toString());
                            builder.exampleArgs(String.format("[%s]", property.getExamples().getFirst().toString()));
                            if (property.getExamples().size() > 1) {
                                builder.exampleArg2(String.format("%s", property.getExamples().get(1).toString()));
                                builder.exampleMultipleArgs(String.format("[%s, %s]", property.getExamples().getFirst().toString(), property.getExamples().get(1).toString()));
                            }
                        }
                    }
                    case null, default -> builder.exampleArg(property.getExamples().getFirst().toString());
                }
            }

            return builder.build();
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
                    .filter(property -> property.getType() instanceof BrAPIPrimitiveType
                        || property.getType() instanceof BrAPIEnumType)
                    .map(this::createModelField).toList());
            } else if (brAPIClass instanceof BrAPIEnumType brAPIEnumType) {
                boolean isStringEnum = BrAPIPrimitiveType.STRING.equals(brAPIEnumType.getType());
                builder.enumClass(true);
                builder.enumBaseType(resolvePythonEnumBase(brAPIEnumType.getType()));
                builder.enumValues(brAPIEnumType.getValues().stream()
                    .map(v -> EnumValueModel.builder()
                        .name(toEnumMemberName(v.getName()))
                        .value(v.getValue())
                        .stringValue(isStringEnum)
                        .build())
                    .toList());
                builder.requiredFields(List.of());
                builder.scalarFields(List.of());
            } else {
                builder.requiredFields(List.of());
                builder.scalarFields(List.of());
            }

            return builder.build();
        }

        /** Maps a BrAPI primitive type string to the Python enum base class name. */
        private String resolvePythonEnumBase(String brAPIType) {
            return switch (brAPIType) {
                case BrAPIPrimitiveType.INTEGER -> "IntEnum";
                case BrAPIPrimitiveType.NUMBER  -> "float, Enum";
                case BrAPIPrimitiveType.BOOLEAN -> "int, Enum";
                default                         -> "StrEnum";   // string
            };
        }

        /**
         * Converts a raw enum value name (e.g. {@code "fieldStudy"}) into a valid
         * Python UPPER_SNAKE_CASE identifier (e.g. {@code "FIELD_STUDY"}).
         */
        private String toEnumMemberName(String name) {
            // reuse existing toSnakeCase then upper-case
            return toSnakeCase(name).toUpperCase();
        }

        private ClassModelField createModelField(BrAPIObjectProperty property) {
            return ClassModelField.builder().name(property.getName()).type(findType(property)).build();
        }

        private boolean isGenerating(BrAPIClass brAPIClass) {
            return brAPIClass instanceof BrAPIObjectType && brAPIClass.getMetadata() != null &&
                brAPIClass.getMetadata().isPrimaryModel() && options.isGeneratingFor(brAPIClass);
        }

        private String findType(BrAPIObjectProperty property) {

            BrAPIType type = property.getType();

            if (type instanceof BrAPIPrimitiveType primitiveType) {
                return findPyType(primitiveType).getResultOrThrow();
            } else if (type instanceof BrAPIArrayType arrayType) {
                if (arrayType.getItems() instanceof BrAPIArrayType) {
                    throw new RuntimeException(String.format("Properties '%s' with 2+ dimensions are not supported yet", property.getName()));
                }
                return String.format("List[%s]", findType(arrayType.getItems()).getResultOrThrow());
            } else if (property.getType() instanceof BrAPIObjectType objectType) {
                return objectType.getName();
            } else if (property.getType() instanceof BrAPIReferenceType referenceType) {
                return referenceType.getName();
            } else if (property.getType() instanceof BrAPIEnumType enumType) {
                return enumType.getName();
            } else {
                throw new RuntimeException(String.format("Property '%s' with type '%s' not supported yet", property.getName(), property.getType().getName()));
            }
        }

        private Response<String> findType(BrAPIType type) {

            if (type instanceof BrAPIPrimitiveType primitiveType) {
                return findPyType(primitiveType);
            } else if (type instanceof BrAPIArrayType arrayType) {
                if (arrayType.getItems() instanceof BrAPIArrayType) {
                    return fail(Response.ErrorType.VALIDATION, String.format("Type '%s' with 2+ dimensions are not supported yet", type.getName()));
                }
                return success(String.format("List[%s]", findType(arrayType.getItems())));
            } else if (type instanceof BrAPIObjectType objectType) {
                return success(objectType.getName());
            } else if (type instanceof BrAPIReferenceType referenceType) {
                return success(referenceType.getName());
            } else if (type instanceof BrAPIEnumType enumType) {
                return success(enumType.getName());
            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("Type '%s' not supported yet", type.getName()));
            }
        }

        private String getDescription(BrAPIObjectProperty brAPIObjectProperty) {
            return StringUtils.extractFirstLine(brAPIObjectProperty.getDescription());
        }

        private Path createPathForEntityClass(String fileName) {
            return outputPath.resolve(options.getEntitiesDirectory()).resolve(fileName);
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
