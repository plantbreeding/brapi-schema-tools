package org.brapi.schematools.core.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.options.LinkType.ID;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.BrAPITypeUtils.unwrapType;
import static org.brapi.schematools.core.utils.StringUtils.*;

@Slf4j
public class ANSICreateTableDDLGenerator implements CreateTableDDLGenerator {

    private final SQLGeneratorOptions options;
    private final SQLGeneratorMetadata metadata;
    private final BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache;
    private final String tableUsing;
    private final Map<String, Object> tableProperties;
    private final Set<String> constraints;
    private final Set<String> tables;
    /**
     * Top-level column names actually emitted in each primary table's {@code CREATE TABLE}.
     * Used so FK constraints never reference columns that were omitted (e.g. deprecated relationships).
     */
    private final Map<String, Set<String>> emittedColumnsByTable;
    private final List<SeparateTableScript> separateTableScripts;

    public ANSICreateTableDDLGenerator(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, List<BrAPIClass> brAPIClasses) {
        this.options = options;
        this.metadata = metadata;
        this.brAPIClassCache = BrAPIClassCacheBuilder.builder(brAPIClasses).build();

        this.tableUsing = options.getTableUsing() != null && !options.getTableUsing().isBlank() ? options.getTableUsing() : null;
        this.tableProperties = options.getTableProperties();
        this.tables = new TreeSet<>() ;
        this.constraints = new TreeSet<>() ;
        this.emittedColumnsByTable = new HashMap<>();
        this.separateTableScripts = new ArrayList<>();
    }

    @Override
    public Response<String> generateDDLForObjectType(BrAPIObjectType brAPIObjectType) {
        return new Generator(brAPIObjectType).generate();
    }

    @Override
    public Response<String> generateDropScript() {

        StringBuilder builder = new StringBuilder();

        for (String table : tables) {
            builder.append("DROP TABLE IF EXISTS ");
            builder.append(table);
            builder.append(" ;");
            builder.append(System.lineSeparator());
        }

        return success(builder.toString());
    }

    @Override
    public Response<String> generateForeignKeyConstraintScript() {
        StringBuilder builder = new StringBuilder();

        for (String constraint : constraints) {
            builder.append(constraint);
            builder.append(System.lineSeparator());
        }

        return success(builder.toString());
    }

    @Override
    public Response<List<SeparateTableScript>> drainSeparateTableScripts() {
        List<SeparateTableScript> scripts = new ArrayList<>(separateTableScripts);
        separateTableScripts.clear();
        return success(scripts);
    }

    private class Generator {
        private final BrAPIObjectType brAPIObjectType;
        private final List<LinkTable> linkTables = new ArrayList<>();
        private final List<ControlledVocabularyTable> controlledVocabularyTables = new ArrayList<>();
        private int indent = 0 ;
        private int arrayStructDepth = 0;
        /** Set while generating a primary table definition so top-level columns can be recorded. */
        private String currentPrimaryTableName;

        public Generator(BrAPIObjectType brAPIObjectType) {
            this.brAPIObjectType = brAPIObjectType;
        }

        public Response<String> generate() {

            if (brAPIObjectType.getMetadata() != null && brAPIObjectType.getMetadata().getControlledVocabularyProperties() != null
                && !brAPIObjectType.getMetadata().getControlledVocabularyProperties().isEmpty()) {
                brAPIObjectType.getProperties()
                    .stream()
                    .filter(this::isAddingDepreciatedProperty)
                    .filter(property -> brAPIObjectType.getMetadata().getControlledVocabularyProperties().contains(property.getName()))
                    .filter(property -> options.getControlledVocabulary().isGeneratingFor(brAPIObjectType, property))
                    .map(property -> new ControlledVocabularyTable(brAPIObjectType, property))
                    .forEach(controlledVocabularyTables::add) ;
            }

            return createTableDefinition(
                createTableNameFullName(brAPIObjectType),
                () -> createTableDescription(brAPIObjectType),
                () -> createColumnDefinitions(brAPIObjectType),
                getTableDescription(brAPIObjectType),
                findClusterColumns(brAPIObjectType),
                true)
                .mapResultToResponse(this::attachOrSeparateLinkTables)
                .mapResultToResponse(this::attachOrSeparateControlledVocabularyTables);
        }

        private Response<String> createTableDefinition(String tableName,
                                                       Supplier<Response<String>> descriptionSupplier,
                                                       Supplier<Response<String>> columnSupplier,
                                                       String description,
                                                       List<String> clusterColumns,
                                                       boolean primaryTable) {

            StringBuilder builder = new StringBuilder();
            tables.add(tableName);

            String previousPrimaryTableName = currentPrimaryTableName;
            if (primaryTable) {
                currentPrimaryTableName = tableName;
                emittedColumnsByTable.computeIfAbsent(tableName, ignored -> new LinkedHashSet<>());
            }

            try {
                return Response.empty()
                    .mapOnCondition(options.isAddingTableHeaderComments(), descriptionSupplier)
                    .onSuccessDoWithResult(builder::append)
                    .map(() -> createTableDefinitionStart(tableName))
                    .onSuccessDoWithResult(builder::append)
                    .map(columnSupplier)
                    .onSuccessDoWithResult(builder::append)
                    .map(() -> createTableDefinitionEnd(tableName, description, clusterColumns, primaryTable))
                    .onSuccessDoWithResult(builder::append)
                    .map(() -> success(builder.toString())) ;
            } finally {
                currentPrimaryTableName = previousPrimaryTableName;
            }
        }

        private void registerEmittedColumn(String columnName) {
            if (currentPrimaryTableName == null || arrayStructDepth > 0 || columnName == null || columnName.isBlank()) {
                return;
            }
            emittedColumnsByTable
                .computeIfAbsent(currentPrimaryTableName, ignored -> new LinkedHashSet<>())
                .add(columnName);
        }

        private Set<String> emittedColumnsFor(String tableName) {
            return emittedColumnsByTable.getOrDefault(tableName, Set.of());
        }

        /**
         * Keeps only FK source columns that were actually emitted on the table.
         * Drops constraints entirely when none remain (e.g. deprecated Locale.program -> programPUI omitted).
         */
        private List<BrAPIObjectProperty> filterEmittedForeignKeyColumns(
                String tableName,
                BrAPIPropertyWithType relationship,
                List<BrAPIObjectProperty> sourceLinkProps,
                Set<String> emittedColumns) {

            if (sourceLinkProps == null || sourceLinkProps.isEmpty()) {
                log.warn("Skipping FK constraint on table '{}': no source columns found for property '{}'",
                    tableName, relationship.getProperty().getName());
                return List.of();
            }

            List<BrAPIObjectProperty> emittedSourceColumns = sourceLinkProps.stream()
                .filter(property -> emittedColumns.contains(property.getName()))
                .toList();

            if (emittedSourceColumns.isEmpty()) {
                log.warn(
                    "Skipping FK constraint on table '{}' for property '{}': none of the link columns {} were emitted in CREATE TABLE (emitted={})",
                    tableName,
                    relationship.getProperty().getName(),
                    sourceLinkProps.stream().map(BrAPIObjectProperty::getName).toList(),
                    emittedColumns);
            } else if (emittedSourceColumns.size() != sourceLinkProps.size()) {
                log.warn(
                    "Partial FK columns on table '{}' for property '{}': using {} (omitted non-emitted link columns)",
                    tableName,
                    relationship.getProperty().getName(),
                    emittedSourceColumns.stream().map(BrAPIObjectProperty::getName).toList());
            }

            return emittedSourceColumns;
        }

        private Response<String> createTableDefinitionStart(String tableName) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;

            if (options.isAddingDropTable()) {
                builder.append("DROP TABLE IF EXISTS ");
                builder.append(tableName);
                builder.append("; ");
                appendNewLine(builder) ;
            }

            builder.append("CREATE TABLE ");
            if (options.isAddingIfNotExists()) {
                builder.append("IF NOT EXISTS ");
            }

            builder.append(tableName);

            builder.append(" (");

            indent() ;
            appendNewLine(builder) ;

            return success(builder.toString());
        }

        private Response<String> createTableDefinitionEnd(String tableName, String description, List<String> clusterColumns, boolean primaryTable) {

            try {
                StringBuilder builder = new StringBuilder();

                if (primaryTable && (options.isAddingForeignKeyConstraints() || options.isGeneratingForeignKeyConstraintScript())) {
                    List<BrAPIPropertyWithType> foreignKeyProperties = brAPIObjectType.getProperties()
                        .stream()
                        // Match CREATE TABLE: omit deprecated relationships when the reader ignores them
                        .filter(this::isAddingDepreciatedProperty)
                        .filter(property -> brAPIClassCache.dereferenceType(property.getType()) instanceof BrAPIObjectType)
                        .filter(property -> getLinkTypeFor(brAPIObjectType, property).getResultIfPresentOrElseResult(LinkType.NONE) == ID)
                        .map(property -> BrAPIPropertyWithType.builder().parentType(brAPIObjectType).property(property).type(unwrapAndDereferenceType(property.getType())).build())
                        .filter(propertyWithType -> propertyWithType.getType() instanceof BrAPIObjectType)
                        .toList();

                    Set<String> emittedColumns = emittedColumnsFor(tableName);

                    if (options.isAddingForeignKeyConstraints()) {
                        for (BrAPIPropertyWithType brAPIPropertyWithType : foreignKeyProperties) {
                            List<BrAPIObjectProperty> sourceLinkProps = filterEmittedForeignKeyColumns(
                                tableName,
                                brAPIPropertyWithType,
                                options.getProperties().getLinkPropertiesFor(
                                    brAPIPropertyWithType.getParentType(),
                                    brAPIPropertyWithType.getProperty(),
                                    (BrAPIObjectType) brAPIPropertyWithType.getType()),
                                emittedColumns);
                            if (sourceLinkProps.isEmpty()) {
                                continue;
                            }
                            builder.append(",");
                            appendNewLine(builder);
                            builder.append("CONSTRAINT ");
                            builder.append(createForeignKeyConstraintName(
                                tableName,
                                brAPIPropertyWithType.getProperty(),
                                (BrAPIObjectType) brAPIPropertyWithType.getType()));
                            builder.append(" FOREIGN KEY(");
                            String inlineFkColumns = sourceLinkProps.stream()
                                .map(BrAPIObjectProperty::getName)
                                .collect(Collectors.joining(", "));
                            builder.append(inlineFkColumns);
                            builder.append(") REFERENCES ");
                            builder.append(createTableNameFullName((BrAPIObjectType) brAPIPropertyWithType.getType()));
                        }
                    } else {
                        for (BrAPIPropertyWithType brAPIPropertyWithType : foreignKeyProperties) {
                            List<BrAPIObjectProperty> sourceLinkProps = filterEmittedForeignKeyColumns(
                                tableName,
                                brAPIPropertyWithType,
                                options.getProperties().getLinkPropertiesFor(
                                    brAPIPropertyWithType.getParentType(),
                                    brAPIPropertyWithType.getProperty(),
                                    (BrAPIObjectType) brAPIPropertyWithType.getType()),
                                emittedColumns);
                            if (sourceLinkProps.isEmpty()) {
                                continue;
                            }
                            StringBuilder builder2 = new StringBuilder();

                            builder2.append("ALTER TABLE ");
                            if (options.isAddingConstraintIfExists()) {
                                builder2.append("IF EXISTS ");
                            }
                            builder2.append(tableName);
                            builder2.append(" ADD CONSTRAINT ");
                            builder2.append(createForeignKeyConstraintName(
                                tableName,
                                brAPIPropertyWithType.getProperty(),
                                (BrAPIObjectType) brAPIPropertyWithType.getType()));
                            builder2.append(" FOREIGN KEY(");
                            String fkColumns = sourceLinkProps.stream()
                                .map(BrAPIObjectProperty::getName)
                                .collect(Collectors.joining(", "));
                            builder2.append(fkColumns);
                            builder2.append(") REFERENCES ");
                            builder2.append(createTableNameFullName((BrAPIObjectType) brAPIPropertyWithType.getType()));
                            builder2.append(" ;");
                            constraints.add(builder2.toString()) ;
                        }
                    }
                }

                dedent() ;
                appendNewLine(builder) ;
                builder.append(") ");

                if (tableUsing != null) {
                    appendNewLine(builder) ;
                    builder.append("USING ");
                    builder.append(tableUsing);
                }

                if (options.isClustering()) {

                    List<String> columns = clusterColumns;

                    if (clusterColumns.size() > 4) {
                        log.warn("Clustering on more than 4 columns is not supported in many SQL dialects, table {} has {} clustering columns. Removing extra ones. ", tableName, clusterColumns.size());

                        columns = clusterColumns.subList(0, 4) ;
                    }

                    if (!columns.isEmpty()) {
                        appendNewLine(builder) ;
                        builder.append("CLUSTER BY (");
                        builder.append(String.join(",", columns));
                        builder.append(")");
                    } else {
                        log.warn("No clustering columns found for table {}", tableName);
                    }
                }

                if (tableProperties != null && !tableProperties.isEmpty()) {
                    appendNewLine(builder) ;
                    builder.append("TBLPROPERTIES (");
                    builder.append(tableProperties.entrySet().stream().map(this::tableProperty).collect(Collectors.joining()));
                    builder.append(")");
                }

                if (options.isAddingTableComments()) {
                    appendNewLine(builder) ;
                    builder.append("COMMENT '");

                    builder.append(escapeSingleSQLQuotes(description));

                    builder.append("'");
                }

                builder.append(";");
                appendNewLine(builder) ;

                return success(builder.toString());

            } catch (Exception e) {
                return fail(Response.ErrorType.VALIDATION, String.format("Error while creating table definition end for table '%s': %s", tableName, e.getMessage()));
            }
        }

        private String newLine() {
            return System.lineSeparator() + " ".repeat(indent);
        }

        private void appendNewLine(StringBuilder builder) {
            builder.append(newLine());
        }

        private void indent() {
            indent += options.getIndentSize() ;
        }

        private void dedent() {
            indent -= options.getIndentSize() ;
        }

        private String createTableNameFullName(BrAPIObjectType brAPIObjectType) {
            return metadata.getTablePrefix() != null ?
                metadata.getTablePrefix() + createTableName(brAPIObjectType) : createTableName(brAPIObjectType);
        }

        private String createTableName(String fullTableName) {
            return metadata.getTablePrefix() != null ?
                fullTableName.substring(metadata.getTablePrefix().length()) : fullTableName;
        }

        private String createTableName(BrAPIObjectType brAPIObjectType) {
            String name = brAPIObjectType.getName() ;

            if (options.isUsingPluralTableNames()) {
                // Honour pluralFor overrides (e.g. SelectionHistory -> SelectionHistory)
                // instead of always calling toPlural(...).
                name = options.getPluralFor(name) ;
            }

            if (options.isUsingSnakeCaseTableNames()) {
                name = toSnakeCase(name) ;
            }

            return name ;
        }

        /**
         * Builds a unique FK constraint name for a relationship property.
         * Includes the source relationship property so two FKs from the same table
         * to the same target (e.g. fromSeedLot / toSeedLot) do not collide.
         * Shape: {@code <sourceTable>_<property>_<targetTable>[_prefix]_fk}
         */
        private String createForeignKeyConstraintName(String tableName, BrAPIObjectProperty property, BrAPIObjectType targetType) {
            StringBuilder constraintName = new StringBuilder();
            constraintName.append(createTableName(tableName));
            constraintName.append('_');
            constraintName.append(createConstraintPropertySegment(property.getName()));
            constraintName.append('_');
            constraintName.append(createTableName(targetType));
            if (metadata.getForeignKeyConstraintPrefix() != null) {
                constraintName.append(metadata.getForeignKeyConstraintPrefix());
            }
            constraintName.append("_fk");
            return constraintName.toString();
        }

        private String createConstraintPropertySegment(String propertyName) {
            return options.isUsingSnakeCaseTableNames() ? toSnakeCase(propertyName) : propertyName;
        }

        private String getTableDescription(BrAPIObjectType brAPIObjectType) {
            if (brAPIObjectType.getDescription() != null) {
                return removeCarriageReturns(brAPIObjectType.getDescription());
            } else {
                return removeCarriageReturns(options.getDescriptionFor(brAPIObjectType));
            }
        }

        private List<String> findClusterColumns(BrAPIObjectType brAPIObjectType) {
            return options.getProperties().getClusteringPropertiesFor(brAPIObjectType)
                .stream()
                .filter(p -> {
                    BrAPIType dereferencedType = brAPIClassCache.dereferenceType(p.getType());
                    return !(dereferencedType instanceof BrAPIPrimitiveType primitiveType
                        && "boolean".equals(primitiveType.getName()));
                })
                .map(BrAPIObjectProperty::getName)
                .toList() ;
        }

        private LinkType getLinkPropertiesFor(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty brAPIObjectProperty, BrAPIType dereferenceType) {
            return options.getProperties().getLinkTypeFor(brAPIObjectType, brAPIObjectProperty, dereferenceType).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE);
        }

        private void warn(Response<?> response) {
            log.warn(response.getMessagesCombined(", "));
        }

        private String tableProperty(Map.Entry<String, Object> entry) {
            StringBuilder builder = new StringBuilder();

            builder.append("'");
            builder.append(entry.getKey());
            builder.append("' = ");

            if (entry.getValue() instanceof String) {
                builder.append("\"");
                builder.append(entry.getValue());
                builder.append("\"");
            } else {
                builder.append(entry.getValue());
            }

            return builder.toString();
        }

        private Response<String> createColumnDefinitions(BrAPIObjectType brAPIObjectType) {

            // Group 1: Primary properties (dbId, name, PUI) — always come first
            List<BrAPIObjectProperty> primaryProps = new ArrayList<>(options.getProperties().getPrimaryPropertiesFor(brAPIObjectType));

            // Group 2: Object-link (ID-type) properties are first collected as raw
            // object-link props (e.g. 'crop' → Crop), then each is expanded via
            // getLinkPropertiesFor into the FK column properties it will actually
            // generate (e.g. 'commonCropName').  Where an expanded column name matches
            // a property that exists directly on this type, the direct property is
            // preferred so its own description and nullability metadata are used, and
            // so that object-identity deduplication against later groups works correctly.
            // Placed early so that clustering columns (Group 3) appear before complex
            // nested types and remain within Databricks' default 32-column stats window.
            List<BrAPIObjectProperty> linkProps = new ArrayList<>();
            brAPIObjectType.getProperties()
                .stream()
                .filter(this::isAddingDepreciatedProperty)
                .filter(property -> getLinkTypeFor(brAPIObjectType, property).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) == ID)
                .filter(property -> !primaryProps.contains(property))
                .sorted(Comparator.comparing(BrAPIObjectProperty::getName))
                .forEach(linkProps::add);

            // Track column names already claimed by Group 1 and Group 2 expansions so
            // that later groups can also exclude clashing names that belong to different
            // BrAPIObjectProperty instances (i.e. where object-identity dedup is
            // insufficient, e.g. a derived 'commonCropName' vs a direct 'commonCropName').
            Set<String> seenLinkColumnNames = new HashSet<>();
            primaryProps.stream().map(BrAPIObjectProperty::getName).forEach(seenLinkColumnNames::add);

            List<BrAPIObjectProperty> expandedLinkProps = new ArrayList<>();
            for (BrAPIObjectProperty linkProp : linkProps) {
                if (linkProp.getType() instanceof BrAPIArrayType) {
                    // Array link properties (e.g. 'studies: ARRAY<Study>') are rendered
                    // directly so that createColumnDefinition dispatches them through
                    // createArrayColumnDefinition and produces ARRAY<STRING>, not a scalar.
                    // Compose the description from the array property + the item type's ID
                    // property so the comment carries the full FK semantics.
                    if (seenLinkColumnNames.add(linkProp.getName())) {
                        BrAPIType itemType = unwrapAndDereferenceType(linkProp.getType());
                        BrAPIObjectProperty propToAdd = itemType instanceof BrAPIObjectType itemObjectType
                            ? options.getProperties().withArrayLinkDescription(linkProp, itemObjectType)
                            : linkProp;
                        expandedLinkProps.add(propToAdd);
                    }
                } else {
                    BrAPIType dereferencedType = unwrapAndDereferenceType(linkProp.getType());
                    if (dereferencedType instanceof BrAPIObjectType linkObjectType) {
                        for (BrAPIObjectProperty derivedProp : options.getProperties().getLinkPropertiesFor(brAPIObjectType, linkProp, linkObjectType)) {
                            if (seenLinkColumnNames.add(derivedProp.getName())) {
                                // Prefer the direct property on this type when the names
                                // match so that its description / nullability are used and
                                // object-identity dedup works for later groups
                                brAPIObjectType.getProperties().stream()
                                    .filter(p -> p.getName().equals(derivedProp.getName()))
                                    .findFirst()
                                    .ifPresentOrElse(expandedLinkProps::add,
                                        () -> expandedLinkProps.add(derivedProp));
                            }
                        }
                    }
                }
            }

            // Group 3: Clustering properties (not already included), in the configured
            // order.  Placing them here — before the remaining ARRAY/STRUCT columns —
            // ensures they receive Delta Lake stats and avoid DELTA_CLUSTERING_COLUMN_MISSING_STATS
            List<BrAPIObjectProperty> seen = new ArrayList<>(primaryProps);
            seen.addAll(linkProps);           // excludes original object-link props (e.g. 'crop') from later groups
            seen.addAll(expandedLinkProps);   // excludes expanded column props (e.g. 'commonCropName') from later groups

            List<BrAPIObjectProperty> clusterProps = new ArrayList<>();
            options.getProperties().getClusteringPropertiesFor(brAPIObjectType)
                .stream()
                .filter(this::isAddingDepreciatedProperty)
                .filter(p -> getLinkTypeFor(brAPIObjectType, p).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) != LinkType.NONE)
                .filter(p -> !seen.contains(p))
                .filter(p -> !seenLinkColumnNames.contains(p.getName()))
                .forEach(clusterProps::add);
            seen.addAll(clusterProps);

            // Group 4: All remaining properties with a non-NONE link type, sorted alphabetically
            List<BrAPIObjectProperty> otherProps = new ArrayList<>();
            brAPIObjectType.getProperties()
                .stream()
                .filter(this::isAddingDepreciatedProperty)
                .filter(p -> getLinkTypeFor(brAPIObjectType, p).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) != LinkType.NONE)
                .filter(p -> !seen.contains(p))
                .filter(p -> !seenLinkColumnNames.contains(p.getName()))
                .sorted(Comparator.comparing(BrAPIObjectProperty::getName))
                .forEach(otherProps::add);

            // Only add the "-- Properties" separator when at least one earlier group
            // (link or clustering) contributed columns, so tables without those groups
            // don't get a redundant separator
            String otherComment = (!expandedLinkProps.isEmpty() || !clusterProps.isEmpty()) ? "-- Properties" : "";

            return buildGroupedColumnDefinitions(brAPIObjectType,
                List.of(primaryProps,           expandedLinkProps,      clusterProps,                   otherProps),
                List.of("-- Primary properties", "-- Link properties",   "-- Clustering properties",     otherComment));
        }

        private boolean isAddingDepreciatedProperty(BrAPIObjectProperty property) {
            return !(options.getBrAPISchemaReader().isIgnoringDepreciatedProperties() && property.isDeprecated()) ;
        }

        /**
         * Assembles column definitions from multiple ordered groups into a single
         * comma-separated SQL column list.  When a group has a non-blank comment string
         * and is not the very first group of columns, the comment is inserted on its own
         * line (at the current indent level) just before the first column of that group,
         * producing output like:
         * <pre>
         *   lastColOfPrevGroup STRING,
         *   -- Comment
         *   firstColOfNextGroup STRING,
         * </pre>
         */
        private Response<String> buildGroupedColumnDefinitions(
                BrAPIObjectType brAPIObjectType,
                List<List<BrAPIObjectProperty>> groups,
                List<String> comments) {

            List<Response<String>> allCols = new ArrayList<>();

            for (int i = 0; i < groups.size(); i++) {
                List<BrAPIObjectProperty> group = groups.get(i);
                if (group.isEmpty()) continue;

                String comment = (i < comments.size()) ? comments.get(i) : "";

                List<Response<String>> groupCols = group.stream()
                    .map(p -> createColumnDefinition(brAPIObjectType, p))
                    .toList();

                if (!comment.isBlank()) {
                    // Prepend "-- comment\n<indent>" to the first column definition of
                    // this group so it appears on its own line between the groups
                    String commentPrefix = comment + newLine();
                    List<Response<String>> annotated = new ArrayList<>(groupCols);
                    annotated.set(0, groupCols.getFirst().mapResult(col -> commentPrefix + col));
                    allCols.addAll(annotated);
                } else {
                    allCols.addAll(groupCols);
                }
            }

            return allCols.stream()
                .collect(Response.toList())
                .mapResult(cols -> String.join("," + newLine(), cols));
        }

        private Response<String> createTableDescription(BrAPIObjectType brAPIObjectType) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_START);

            if (brAPIObjectType.getDescription() != null) {
                appendNewLine(builder) ;
                builder.append(escapeBlockCommentContent(brAPIObjectType.getDescription()));
            } else {
                appendNewLine(builder) ;
                builder.append(escapeBlockCommentContent(options.getDescriptionFor(brAPIObjectType)));
            }

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_END);

            return success(builder.toString());
        }

        private BrAPIType unwrapAndDereferenceType(BrAPIType type) {
            BrAPIType unwrappedType = unwrapType(type);
            return brAPIClassCache.dereferenceType(unwrappedType);
        }

        private Response<LinkType> getLinkTypeFor(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property) {
            return options.getProperties().getLinkTypeFor(brAPIObjectType, property, unwrapAndDereferenceType(property.getType()));
        }

        private Response<String> attachOrSeparateLinkTables(String ddl) {
            if (!options.isGeneratingLinkTables() || linkTables.isEmpty()) {
                return success(ddl);
            }

            if (options.isSeparatingLinkTables()) {
                return linkTables.stream()
                    .map(this::stageSeparateLinkTable)
                    .collect(Response.toList())
                    .map(() -> success(ddl));
            }

            return appendLinkTableDefinitions(ddl);
        }

        private Response<String> stageSeparateLinkTable(LinkTable linkTable) {
            return appendLinkTableDefinition(linkTable)
                .mapResult(sql -> {
                    separateTableScripts.add(new SeparateTableScript(
                        createLinkTableName(linkTable) + ".sql",
                        createLinkTableName(linkTable),
                        sql));
                    return sql;
                });
        }

        private Response<String> appendLinkTableDefinitions(String ddl) {
            StringBuilder builder = new StringBuilder(ddl);

            appendNewLine(builder) ;

            return linkTables.stream()
                .map(this::appendLinkTableDefinition)
                .collect(Response.toList())
                .mapResult(result -> String.join(newLine(), result))
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString);
        }

        private Response<String> appendLinkTableDefinition(LinkTable linkTable) {
            return createTableDefinition(
                createLinkTableFullName(linkTable),
                () -> createTableDescription(linkTable),
                () -> createColumnDefinitions(linkTable),
                getTableComment(linkTable),
                findClusterColumns(linkTable),
                false);
        }

        private String createLinkTableFullName(LinkTable linkedTable) {
            return metadata.getTablePrefix() != null ?
                metadata.getTablePrefix() + createLinkTableName(linkedTable) : createLinkTableName(linkedTable);
        }

        private String createLinkTableName(LinkTable linkedTable) {
            String name = linkedTable.getDereferencedItemType().getName() + "By" + linkedTable.getParentType().getName();

            if (options.isUsingSnakeCaseTableNames()) {
                name = toSnakeCase(name) ;
            }

            return name ;
        }

        private Response<String> createColumnDefinitions(LinkTable linkTable) {
            if (linkTable.getDereferencedItemType() instanceof BrAPIObjectType childBrAPIObjectType) {
                List<BrAPIObjectProperty> linkProperties = new ArrayList<>(options.getProperties().getLinkPropertiesFor(childBrAPIObjectType));
                linkProperties.addAll(options.getProperties().getLinkPropertiesFor(linkTable.getParentType()));

                return createLinkObjectDefinition(linkProperties);

            } else {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("Cannot create link table column definitions from for '%s' to non-object type '%s'",
                        linkTable.getParentType().getName(), linkTable.getDereferencedItemType().getName()));
            }
        }

        private Response<String> createTableDescription(LinkTable linkTable) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_START);

            appendNewLine(builder) ;
            builder.append(String.format("Creates a lookup table for property '%s' for '%s' to '%s'",
                linkTable.getProperty().getName(),
                linkTable.getParentType().getName(),
                linkTable.getDereferencedItemType().getName()
            )) ;

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_END);

            return success(builder.toString());
        }

        private String getTableComment(LinkTable linkedTable) {
            return String.format("Link table for %s to %s on property %s", linkedTable.getParentType().getName(), linkedTable.getDereferencedItemType().getName(), linkedTable.getProperty().getName());
        }

        private List<String> findClusterColumns(LinkTable linkTable) {
            return Collections.emptyList() ;
        }

        private Response<String> attachOrSeparateControlledVocabularyTables(String ddl) {
            if (!options.getControlledVocabulary().isGenerating() || controlledVocabularyTables.isEmpty()) {
                return success(ddl);
            }

            if (options.isSeparatingControlledVocabularyTables()) {
                return controlledVocabularyTables.stream()
                    .map(this::stageSeparateControlledVocabularyTable)
                    .collect(Response.toList())
                    .map(() -> success(ddl));
            }

            return appendControlledVocabularyDefinitions(ddl);
        }

        private Response<String> stageSeparateControlledVocabularyTable(ControlledVocabularyTable controlledVocabularyTable) {
            return appendControlledVocabularyDefinition(controlledVocabularyTable)
                .mapResult(sql -> {
                    separateTableScripts.add(new SeparateTableScript(
                        createControlledVocabularyTableName(controlledVocabularyTable) + ".sql",
                        createControlledVocabularyTableName(controlledVocabularyTable),
                        sql));
                    return sql;
                });
        }

        private Response<String> appendControlledVocabularyDefinitions(String ddl) {
            StringBuilder builder = new StringBuilder(ddl);

            appendNewLine(builder) ;

            return controlledVocabularyTables.stream()
                .map(this::appendControlledVocabularyDefinition)
                .collect(Response.toList())
                .mapResult(result -> String.join(newLine(), result))
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString);
        }

        private Response<String> appendControlledVocabularyDefinition(ControlledVocabularyTable controlledVocabularyTable) {
            return createTableDefinition(
                createControlledVocabularyFullTableName(controlledVocabularyTable),
                () -> createTableDescription(controlledVocabularyTable),
                () -> createColumnDefinitions(controlledVocabularyTable),
                getTableComment(controlledVocabularyTable),
                findClusterColumns(controlledVocabularyTable),
                false);
        }

        private String createControlledVocabularyFullTableName(ControlledVocabularyTable controlledVocabularyTable) {
            return metadata.getTablePrefix() != null ?
                metadata.getTablePrefix() + createControlledVocabularyTableName(controlledVocabularyTable) : createControlledVocabularyTableName(controlledVocabularyTable);
        }

        private String createControlledVocabularyTableName(ControlledVocabularyTable controlledVocabularyTable) {
            // Property names are often already plural (e.g. observationLevels). Singularise first
            // so plural table naming does not produce double plurals like ObservationLevelses.
            String name = toSentenceCase(toSingular(controlledVocabularyTable.getProperty().getName())) ;

            if (options.isUsingPluralTableNames()) {
                name = toPlural(name) ;
            }

            if (options.isUsingSnakeCaseTableNames()) {
                name = toSnakeCase(name) ;
            }

            return name ;
        }

        private Response<String> createColumnDefinitions(ControlledVocabularyTable controlledVocabularyTable) {
            BrAPIObjectProperty property = controlledVocabularyTable.getProperty();
            BrAPIType dereferencedType = unwrapAndDereferenceType(property.getType());

            // Controlled vocabulary tables store one vocabulary row each, not an ARRAY/STRUCT blob.
            if (dereferencedType instanceof BrAPIArrayType arrayType) {
                BrAPIType itemType = unwrapAndDereferenceType(arrayType.getItems());
                if (itemType instanceof BrAPIObjectType itemObjectType) {
                    return createColumnDefinitions(itemObjectType);
                }
                if (itemType instanceof BrAPIPrimitiveType || itemType instanceof BrAPIEnumType) {
                    String simpleTypeName = itemType instanceof BrAPIEnumType enumType
                        ? enumType.getType()
                        : itemType.getName();
                    return createSimpleColumnDefinition(
                        controlledVocabularyTable.getParentType(),
                        property.toBuilder().type(itemType).nullable(false).build(),
                        simpleTypeName);
                }
                return fail(Response.ErrorType.VALIDATION,
                    String.format("Unsupported controlled-vocabulary array item type '%s' for property '%s'",
                        itemType != null ? itemType.getName() : "null", property.getName()));
            }

            if (dereferencedType instanceof BrAPIObjectType objectType) {
                return createColumnDefinitions(objectType);
            }

            return createColumnDefinition(controlledVocabularyTable.getParentType(), property);
        }

        private Response<String> createTableDescription(ControlledVocabularyTable controlledVocabularyTable) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_START);

            appendNewLine(builder) ;
            builder.append(escapeBlockCommentContent(options.getControlledVocabulary().getDescriptionFor(controlledVocabularyTable.getParentType(), controlledVocabularyTable.getProperty())));

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_END);

            return success(builder.toString());
        }

        private String getTableComment(ControlledVocabularyTable controlledVocabularyTable) {
            return String.format("Controlled Vocabulary table for property %s on %s", controlledVocabularyTable.getProperty().getName(), controlledVocabularyTable.getParentType().getName());
        }

        private List<String> findClusterColumns(ControlledVocabularyTable controlledVocabularyTable) {
            return Collections.emptyList() ;
        }

        private Response<String> addColumnEnd(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property, String columnDefinition) {

            StringBuilder builder = new StringBuilder(columnDefinition);

            if (options.isAddingNotNullConstraints() && !property.isNullable()
                && (arrayStructDepth == 0 || options.isAddingConstraintsInArrayStructs())) {
                builder.append(" NOT NULL");
            }

            if (arrayStructDepth == 0 && options.isAddingPrimaryKeyConstraints() && options.getProperties().isPrimaryLinkPropertyFor(brAPIObjectType, property)
                && arrayStructDepth == 0) {
                builder.append(" PRIMARY KEY");
            }

            builder.append(" COMMENT '");

            if (property.getDescription() != null) {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(property.getDescription())));
            } else {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(options.getProperties().getDescriptionFor(brAPIObjectType, property))));
            }

            builder.append("'");

            return success(builder.toString());
        }

        private Response<String> createColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {
            BrAPIType dereferencedType = brAPIClassCache.dereferenceType(property.getType());

            if (property.getType().getName().equals("AdditionalInfo")) {
                return createAdditionalInfoColumnDefinition(parentType, property);
            } else if (dereferencedType instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                return createSimpleColumnDefinition(parentType, property, brAPIPrimitiveType.getName());
            } else if (dereferencedType instanceof BrAPIEnumType brAPIEnumType) {
                return createSimpleColumnDefinition(parentType, property, brAPIEnumType.getType());
            } else if (dereferencedType instanceof BrAPIObjectType brAPIObjectDereferencedType) {
                return createObjectColumnDefinition(parentType, property, brAPIObjectDereferencedType);
            } else if (dereferencedType instanceof BrAPIOneOfType brAPIOneOfType) {
                return createOneOfTypeColumnDefinition(parentType, property, brAPIOneOfType);
            } else if (dereferencedType instanceof BrAPIAllOfType) {
                return fail(Response.ErrorType.VALIDATION, "All-of-types are not supported, should have been removed at this point!");
            } else if (dereferencedType instanceof BrAPIArrayType brAPIArrayType) {
                return createArrayColumnDefinition(parentType, property, brAPIArrayType);
            }

            return fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", dereferencedType != null ? dereferencedType.getName() : "null"));
        }

        private Response<String> createAdditionalInfoColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {

            registerEmittedColumn(property.getName());

            String builder = property.getName() +
                " MAP<STRING,STRING>";

            return success(builder).conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(parentType, property, result));
        }

        private Response<String> createSimpleColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, String type) {

            registerEmittedColumn(property.getName());

            StringBuilder builder = new StringBuilder();
            builder.append(property.getName());
            builder.append(" ");

            return findSimpleColumnType(type)
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString)
                .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(parentType, property, result));
        }

        private Response<String> findSimpleColumnType(String type) {
            return
                switch (type) {
                    case "integer" -> success("INT");
                    case "number" -> success("DOUBLE");
                    case "boolean" -> success("BOOLEAN");
                    case "string" -> success("STRING");
                    default -> fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type));
                };
        }

        private Response<String> createObjectColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
            return options.getProperties().getLinkTypeFor(parentType, property, brAPIObjectType).mapResultToResponse(
                linkType -> switch (linkType) {
                    case EMBEDDED ->
                        createObjectColumnType(brAPIObjectType)
                            .mapResult(columnType -> {
                                registerEmittedColumn(property.getName());
                                return property.getName() + " " + columnType;
                            })
                            .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result));
                    case ID -> createLinkObjectDefinition(parentType, property, brAPIObjectType);
                    default ->
                        fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for property '%s' with item type '%s'", linkType, property.getName(), brAPIObjectType.getName()));
                });
        }

        private Response<String> createLinkObjectDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
            List<BrAPIObjectProperty> linkPropertiesFor = options.getProperties().getLinkPropertiesFor(parentType, property, brAPIObjectType);

            if (linkPropertiesFor.isEmpty()) {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("No link properties for property '%s' in '%s' with item type '%s'",
                        property.getName(), parentType.getName(), brAPIObjectType.getName()));
            }

            return createLinkObjectDefinition(linkPropertiesFor);
        }

        private Response<String> createLinkObjectDefinition(List<BrAPIObjectProperty> linkProperties) {
            return linkProperties.stream()
                .filter(p -> p.getType() instanceof BrAPIPrimitiveType)
                .map(p -> createSimpleColumnDefinition(brAPIObjectType, p, p.getType().getName())).collect(Response.toList())
                .mapResult(columnDefinitions -> String.join("," + newLine(), columnDefinitions));
        }

        private Response<String> createObjectColumnType(BrAPIObjectType brAPIObjectType) {
            arrayStructDepth++;
            try {
                StringBuilder builder = new StringBuilder();
                indent();
                appendNewLine(builder);
                builder.append("STRUCT<");
                indent();
                appendNewLine(builder);

                return createColumnDefinitions(brAPIObjectType)
                    .mapResult(builder::append)
                    .onSuccessDo(this::dedent)
                    .onSuccessDoWithResult(this::appendNewLine)
                    .mapResult(b -> b.append(">"))
                    .mapResult(StringBuilder::toString)
                    .onSuccessDo(this::dedent);
            } finally {
                arrayStructDepth--;
            }
        }

        private Response<String> createOneOfTypeColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIOneOfType brAPIOneOfType) {

            int i = 1;

            List<Response<String>> responses = new ArrayList<>(brAPIOneOfType.getPossibleTypes().size());

            for (BrAPIType type : brAPIOneOfType.getPossibleTypes()) {
                StringBuilder builder = new StringBuilder();
                builder.append(property.getName());
                builder.append(i);
                registerEmittedColumn(property.getName() + i);
                indent();
                appendNewLine(builder);
                builder.append("STRUCT<");
                indent();
                appendNewLine(builder) ;

                if (type instanceof BrAPIObjectType childType) {
                    responses.add(createColumnDefinitions(childType)
                        .mapResult(builder::append)
                        .onSuccessDo(this::dedent)
                        .onSuccessDoWithResult(this::appendNewLine)
                        .mapResult(b -> b.append(">"))
                        .conditionalMapResult(i < brAPIOneOfType.getPossibleTypes().size(), b -> b.append(","))
                        .mapResult(StringBuilder::toString));
                } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                    responses.add(findSimpleColumnType(brAPIPrimitiveType.getName())
                        .mapResult(builder::append)
                        .onSuccessDo(this::dedent)
                        .mapResult(b -> b.append(">"))
                        .conditionalMapResult(i < brAPIOneOfType.getPossibleTypes().size(), b -> b.append(","))
                        .mapResult(StringBuilder::toString));
                } else {
                    responses.add(fail(Response.ErrorType.VALIDATION, String.format("Unknown embedded one of type '%s'", type.getName())));
                }

                appendNewLine(builder) ;
                dedent();

                ++i;
            }

            return responses.stream().collect(Response.toList())
                .mapResult(s -> String.join(newLine(), s))
                .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result));
        }

        private Response<String> createArrayColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIArrayType brAPIArrayType) {
            BrAPIType dereferencedItemType = brAPIClassCache.dereferenceType(brAPIArrayType.getItems());

            if (dereferencedItemType == null) {
                return fail(Response.ErrorType.VALIDATION, String.format("Cannot dereference '%s'", brAPIArrayType.getItems().getName()));
            }

            return options.getProperties().getLinkTypeFor(parentType, property, dereferencedItemType)
                .mapResultToResponse(linkType -> createArrayColumnDefinition(parentType, property, dereferencedItemType, linkType)) ;

        }

        private Response<String> createArrayColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIType dereferencedItemType, LinkType linkType) {
            StringBuilder builder = new StringBuilder();

            return switch (linkType) {
                case EMBEDDED -> {
                    registerEmittedColumn(property.getName());
                    builder.append(property.getName());
                    if (dereferencedItemType instanceof BrAPIObjectType) {
                        indent();
                        appendNewLine(builder);
                    } else {
                        builder.append(" ");
                    }

                    builder.append("ARRAY<");

                    yield createArrayColumnType(dereferencedItemType)
                        .mapResult(builder::append)
                        .onSuccessDoWithResultOnCondition(dereferencedItemType instanceof BrAPIObjectType, this::appendNewLine)
                        .mapResult(b -> b.append(">"))
                        .mapResult(StringBuilder::toString)
                        .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result))
                        .onSuccessDoOnCondition(dereferencedItemType instanceof BrAPIObjectType, this::dedent);
                }
                case ID -> {
                    if (dereferencedItemType instanceof BrAPIObjectType dereferencedItemTypeObjectType) {
                        String idsColumnName = options.getProperties().getIdsPropertyNameFor(property);
                        registerEmittedColumn(idsColumnName);
                        builder.append(idsColumnName);
                        builder.append(" ");
                        builder.append("ARRAY<");

                        yield options.getProperties().getIdPropertyFor(dereferencedItemTypeObjectType)
                            .mapResultToResponse(p -> findSimpleColumnType(p.getType().getName()))
                            .mapResult(builder::append)
                            .mapResult(b -> b.append(">"))
                            .mapResult(StringBuilder::toString)
                            .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result)) ;
                    } else {
                        yield fail(Response.ErrorType.VALIDATION, String.format("Unknown link ID array type '%s'", dereferencedItemType.getName()));
                    }
                }
                case SUB_QUERY -> {
                    LinkTable linkedTable = new LinkTable(parentType, property, dereferencedItemType);
                    linkTables.add(linkedTable);
                    builder.append("-- For property '");
                    builder.append(property.getName());
                    builder.append("' Link table '");
                    builder.append(createLinkTableName(linkedTable));
                    builder.append("' will be created separately");
                    yield success(builder.toString());
                }
                default ->
                    fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for Array with item type '%s'", linkType, dereferencedItemType.getName()));
            };
        }

        private Response<String> createArrayColumnType(BrAPIType itemType) {
            return switch (itemType) {
                case BrAPIObjectType brAPIObjectItemType -> createObjectColumnType(brAPIObjectItemType);
                case BrAPIPrimitiveType brAPIPrimitiveType -> findSimpleColumnType(brAPIPrimitiveType.getName());
                case BrAPIArrayType brAPIArrayType -> createArrayColumnType(brAPIArrayType.getItems());
                case BrAPIEnumType brAPIEnumType -> findSimpleColumnType(brAPIEnumType.getType());
                default ->
                    fail(Response.ErrorType.VALIDATION, String.format("Unknown embedded array type '%s'", itemType.getName()));
            };
        }
    }

    @AllArgsConstructor
    @Getter
    private static class LinkTable {
        private final BrAPIObjectType parentType;
        private final BrAPIObjectProperty property;
        private final BrAPIType dereferencedItemType;
    }

    @AllArgsConstructor
    @Getter
    private static class ControlledVocabularyTable {
        private final BrAPIObjectType parentType;
        private final BrAPIObjectProperty property;
    }
}
