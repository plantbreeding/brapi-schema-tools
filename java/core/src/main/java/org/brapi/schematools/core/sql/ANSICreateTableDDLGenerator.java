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

    public ANSICreateTableDDLGenerator(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, List<BrAPIClass> brAPIClasses) {
        this.options = options;
        this.metadata = metadata;
        this.brAPIClassCache = BrAPIClassCacheBuilder.createCache(brAPIClasses);

        this.tableUsing = options.getTableUsing() != null && !options.getTableUsing().isBlank() ? options.getTableUsing() : null;
        this.tableProperties = options.getTableProperties();
        this.tables = new TreeSet<>() ;
        this.constraints = new TreeSet<>() ;
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
            name = toPlural(name) ;
        }

        if (options.isUsingSnakeCaseTableNames()) {
            name = toSnakeCase(name) ;
        }

        return name ;
    }

    @AllArgsConstructor
    private class Generator {
        private final BrAPIObjectType brAPIObjectType;
        private final List<LinkTable> linkTables = new ArrayList<>();
        private final List<ControlledVocabularyTable> controlledVocabularyTables = new ArrayList<>();
        private int indent = 0 ;

        public Generator(BrAPIObjectType brAPIObjectType) {
            this.brAPIObjectType = brAPIObjectType;
        }

        public Response<String> generate() {

            if (brAPIObjectType.getMetadata() != null && brAPIObjectType.getMetadata().getControlledVocabularyProperties() != null
                && !brAPIObjectType.getMetadata().getControlledVocabularyProperties().isEmpty()) {
                brAPIObjectType.getProperties()
                    .stream()
                    .filter(property -> brAPIObjectType.getMetadata().getControlledVocabularyProperties().contains(property.getName()))
                    .map(property -> new ControlledVocabularyTable(brAPIObjectType, property))
                    .forEach(controlledVocabularyTables::add) ;
            }

            return createTableDefinition(
                createTableNameFullName(brAPIObjectType),
                () -> createTableDescription(brAPIObjectType),
                () -> createColumnDefinitions(brAPIObjectType),
                getTableDescription(brAPIObjectType),
                findClusterColumns(brAPIObjectType)
            )
                .conditionalMapResultToResponse(options.isGeneratingLinkTables() && !linkTables.isEmpty(), this::appendLinkTableDefinitions)
                .conditionalMapResultToResponse(options.getControlledVocabulary().isGenerating() && !controlledVocabularyTables.isEmpty(), this::appendControlledVocabularyDefinitions);
        }

        private Response<String> createTableDefinition(String tableName,
                                                       Supplier<Response<String>> descriptionSupplier,
                                                       Supplier<Response<String>> columnSupplier,
                                                       String description,
                                                       List<String> clusterColumns) {

            StringBuilder builder = new StringBuilder();
            tables.add(tableName);

            return Response.empty()
                .mapOnCondition(options.isAddingTableHeaderComments(), descriptionSupplier)
                .onSuccessDoWithResult(builder::append)
                .map(() -> createTableDefinitionStart(tableName))
                .onSuccessDoWithResult(builder::append)
                .map(columnSupplier)
                .onSuccessDoWithResult(builder::append)
                .map(() -> createTableDefinitionEnd(tableName, description, clusterColumns))
                .onSuccessDoWithResult(builder::append)
                .map(() -> success(builder.toString())) ;
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

        private Response<String> createTableDefinitionEnd(String tableName, String description, List<String> clusterColumns) {

            try {
                StringBuilder builder = new StringBuilder();

                if (options.isAddingForeignKeyConstraints() || options.isGeneratingForeignKeyConstraintScript()) {
                    List<BrAPIPropertyWithType> foreignKeyProperties = brAPIObjectType.getProperties()
                        .stream()
                        .filter(property -> getLinkTypeFor(brAPIObjectType, property).getResultIfPresentOrElseResult(LinkType.NONE) == ID)
                        .map(property -> BrAPIPropertyWithType.builder().property(property).type(unwrapAndDereferenceType(property.getType())).build())
                        .filter(propertyWithType -> propertyWithType.getType() instanceof BrAPIObjectType)
                        .toList();

                    if (options.isAddingForeignKeyConstraints()) {
                        for (BrAPIPropertyWithType brAPIPropertyWithType : foreignKeyProperties) {
                            builder.append(",");
                            appendNewLine(builder);
                            builder.append("CONSTRAINT ");
                            builder.append(createTableName(tableName));
                            builder.append("_");
                            builder.append(createTableName((BrAPIObjectType) brAPIPropertyWithType.getType()));
                            builder.append("_fk FOREIGN KEY(");
                            builder.append(options.getProperties().getLinkPropertyFor((BrAPIObjectType) brAPIPropertyWithType.getType()).getResultOrThrow().getName());
                            builder.append(") REFERENCES ");
                            builder.append(createTableNameFullName((BrAPIObjectType) brAPIPropertyWithType.getType()));
                        }
                    } else {
                        for (BrAPIPropertyWithType brAPIPropertyWithType : foreignKeyProperties) {
                            StringBuilder builder2 = new StringBuilder();

                            builder2.append("ALTER TABLE ");
                            builder2.append(tableName);
                            builder2.append(" ADD CONSTRAINT ");
                            builder2.append("_");
                            builder2.append(createTableName((BrAPIObjectType) brAPIPropertyWithType.getType()));
                            builder2.append("_fk FOREIGN KEY(");
                            builder2.append(options.getProperties().getLinkPropertyFor((BrAPIObjectType) brAPIPropertyWithType.getType()).getResultOrThrow().getName());
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

                    builder.append(removeCarriageReturns(escapeQuotes(description)));

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
                name = toPlural(name) ;
            }

            if (options.isUsingSnakeCaseTableNames()) {
                name = toSnakeCase(name) ;
            }

            return name ;
        }

        private String getTableDescription(BrAPIObjectType brAPIObjectType) {
            if (brAPIObjectType.getDescription() != null) {
                return removeCarriageReturns(escapeQuotes(brAPIObjectType.getDescription()));
            } else {
                return removeCarriageReturns(escapeQuotes(options.getDescriptionFor(brAPIObjectType)));
            }
        }

        private List<String> findClusterColumns(BrAPIObjectType brAPIObjectType) {
            return options.getProperties().getClusteringPropertiesFor(brAPIObjectType)
                .stream()
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

            List<BrAPIObjectProperty> properties = new ArrayList<>(options.getProperties().getPrimaryPropertiesFor(brAPIObjectType));

            brAPIObjectType.getProperties()
                .stream()
                .filter(brAPIObjectProperty -> getLinkTypeFor(brAPIObjectType, brAPIObjectProperty).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) != LinkType.NONE)
                .filter(brAPIObjectProperty -> !properties.contains(brAPIObjectProperty))
                .sorted(Comparator.comparing(BrAPIObjectProperty::getName))
                .forEach(properties::add) ;

            return properties.stream()
                .map(property -> createColumnDefinition(brAPIObjectType, property))
                .collect(Response.toList()).mapResult(columns -> String.join("," + newLine(), columns)) ;
        }

        private Response<String> createTableDescription(BrAPIObjectType brAPIObjectType) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_START);

            if (brAPIObjectType.getDescription() != null) {
                appendNewLine(builder) ;
                builder.append(brAPIObjectType.getDescription());
            } else {
                appendNewLine(builder) ;
                builder.append(options.getDescriptionFor(brAPIObjectType));
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
                findClusterColumns(linkTable));
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
                findClusterColumns(controlledVocabularyTable));
        }

        private String createControlledVocabularyFullTableName(ControlledVocabularyTable controlledVocabularyTable) {
            return metadata.getTablePrefix() != null ?
                metadata.getTablePrefix() + createControlledVocabularyTableName(controlledVocabularyTable) : createControlledVocabularyTableName(controlledVocabularyTable);
        }

        private String createControlledVocabularyTableName(ControlledVocabularyTable controlledVocabularyTable) {
            String name = toSentenceCase(controlledVocabularyTable.getProperty().getName()) ;

            if (options.isUsingPluralTableNames()) {
                name = toPlural(name) ;
            }

            if (options.isUsingSnakeCaseTableNames()) {
                name = toSnakeCase(name) ;
            }

            return name ;
        }

        private Response<String> createColumnDefinitions(ControlledVocabularyTable controlledVocabularyTable) {

            return createColumnDefinition(controlledVocabularyTable.getParentType(), controlledVocabularyTable.getProperty()) ;
        }

        private Response<String> createTableDescription(ControlledVocabularyTable controlledVocabularyTable) {

            StringBuilder builder = new StringBuilder();

            appendNewLine(builder) ;
            builder.append(SQLGenerator.COMMENT_START);

            appendNewLine(builder) ;
            builder.append(options.getControlledVocabulary().getDescriptionFor(controlledVocabularyTable.getParentType(), controlledVocabularyTable.getProperty()));

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

            if (options.isAddingNotNullConstraints() && !property.isNullable()) {
                builder.append(" NOT NULL");
            }

            if (options.isAddingPrimaryKeyConstraints() && options.getProperties().isPrimaryLinkPropertyFor(brAPIObjectType, property)) {
                builder.append(" PRIMARY KEY");
            }

            builder.append(" COMMENT '");

            if (property.getDescription() != null) {
                builder.append(removeCarriageReturns(escapeQuotes(property.getDescription())));
            } else {
                builder.append(removeCarriageReturns(escapeQuotes(options.getProperties().getDescriptionFor(brAPIObjectType, property))));
            }

            builder.append("'");

            return success(builder.toString());
        }

        private Response<String> createColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {
            BrAPIType dereferencedType = brAPIClassCache.dereferenceType(property.getType());

            if (property.getType().getName().equals("AdditionalInfo")) {
                return createAdditionalInfoColumnDefinition(property);
            } else if (dereferencedType instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                return createSimpleColumnDefinition(property, brAPIPrimitiveType.getName());
            } else if (dereferencedType instanceof BrAPIEnumType brAPIEnumType) {
                return createSimpleColumnDefinition(property, brAPIEnumType.getType());
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

        private Response<String> createAdditionalInfoColumnDefinition(BrAPIObjectProperty property) {

            String builder = property.getName() +
                " MAP<STRING,STRING>";

            return success(builder).conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result));
        }

        private Response<String> createSimpleColumnDefinition(BrAPIObjectProperty property, String type) {

            StringBuilder builder = new StringBuilder();
            builder.append(property.getName());
            builder.append(" ");

            return findSimpleColumnType(type)
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString)
                .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result));
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
                            .mapResult(columnType -> property.getName() + " " + columnType)
                            .conditionalMapResultToResponse(options.isAddingTableColumnComments(), result -> addColumnEnd(brAPIObjectType, property, result));
                    case ID -> createLinkObjectDefinition(parentType, property, brAPIObjectType);
                    default ->
                        fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for property '%s' with item type '%s'", linkType, property.getName(), brAPIObjectType.getName()));
                });
        }

        private Response<String> createLinkObjectDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
            List<BrAPIObjectProperty> linkPropertiesFor = options.getProperties().getLinkPropertiesFor(property, brAPIObjectType);

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
                .map(p -> createSimpleColumnDefinition(p, p.getType().getName())).collect(Response.toList())
                .mapResult(columnDefinitions -> String.join("," + newLine(), columnDefinitions));
        }

        private Response<String> createObjectColumnType(BrAPIObjectType brAPIObjectType) {
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
                .onSuccessDo(this::dedent) ;
        }

        private Response<String> createOneOfTypeColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIOneOfType brAPIOneOfType) {

            int i = 1;

            List<Response<String>> responses = new ArrayList<>(brAPIOneOfType.getPossibleTypes().size());

            for (BrAPIType type : brAPIOneOfType.getPossibleTypes()) {
                StringBuilder builder = new StringBuilder();
                builder.append(property.getName());
                builder.append(i);
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
                        builder.append(options.getProperties().getIdsPropertyNameFor(property));
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
