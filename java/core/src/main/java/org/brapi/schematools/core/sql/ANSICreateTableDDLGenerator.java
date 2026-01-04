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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.options.LinkType.ID;
import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.BrAPITypeUtils.unwrapType;
import static org.brapi.schematools.core.utils.StringUtils.escapeSingleSQLQuotes;
import static org.brapi.schematools.core.utils.StringUtils.removeCarriageReturns;

@Slf4j
public class ANSICreateTableDDLGenerator implements CreateTableDDLGenerator {

    private final SQLGeneratorOptions options ;
    private final SQLGeneratorMetadata metadata ;
    private final BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache ;
    private final String tableUsing ;
    private final Map<String,Object> tableProperties ;

    public ANSICreateTableDDLGenerator(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, List<BrAPIClass> brAPIClasses) {
        this.options = options;
        this.metadata = metadata;
        this.brAPIClassCache = BrAPIClassCacheBuilder.createCache(brAPIClasses) ;

        this.tableUsing = options.getTableUsing() != null && !options.getTableUsing().isBlank() ? options.getTableUsing() : null ;
        this.tableProperties = options.getTableProperties()  ;
    }

    @Override
    public Response<String> generateDDLForObjectType(BrAPIObjectType brAPIObjectType) {
        return  new Generator(brAPIObjectType).generate() ;
    }

    @AllArgsConstructor
    private class Generator {
        private final BrAPIObjectType brAPIObjectType ;
        private final List<LinkTable> linkTables = new ArrayList<>() ;

        public Response<String> generate() {
            return createColumnDefinitions(brAPIObjectType)
                .mapResultToResponse(columnDefinitions -> createTableDefinition(brAPIObjectType, createTableName(brAPIObjectType), columnDefinitions, getTableDescription(brAPIObjectType)))
                .conditionalMapResultToResponse(!linkTables.isEmpty(), this::appendLinkTableDefinitions);
        }

        private Response<String> appendLinkTableDefinitions(String ddl) {
            StringBuilder builder = new StringBuilder(ddl);

            builder.append(System.lineSeparator()) ;
            builder.append(System.lineSeparator()) ;

            return linkTables.stream()
                .map(this::appendLinkTableDefinition)
                .collect(Response.toList())
                .mapResult(result -> String.join(System.lineSeparator(), result))
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString) ;
        }

        private Response<String> appendLinkTableDefinition(LinkTable linkTable) {
            return createColumnDefinitions(linkTable)
                .mapResultToResponse(columnDefinitions -> createTableDefinition(brAPIObjectType, createLinkTableFullName(linkTable), columnDefinitions, getLinkTableDescription(linkTable))) ;
        }

        private Response<String> createTableDefinition(BrAPIObjectType brAPIObjectType, String tableName, String columnDefinitions, String description) {

            StringBuilder builder = new StringBuilder();

            builder.append(System.lineSeparator());
            builder.append(System.lineSeparator());

            if (options.isAddingDropTable()) {
                builder.append("DROP TABLE ");
                builder.append(tableName);
                builder.append("; ");
                builder.append(System.lineSeparator());
            }

            builder.append("CREATE TABLE ");
            if (options.isAddingIfNotExists()) {
                builder.append("IF NOT EXISTS ");
            }

            builder.append(tableName);
            builder.append(" (");
            builder.append(System.lineSeparator());

            builder.append(columnDefinitions);

            builder.append(") ");

            if (options.isClustering()) {
                List<String> clusterColumn = findClusterColumns(brAPIObjectType);

                if (!clusterColumn.isEmpty()) {
                    builder.append(" CLUSTER BY ( ");
                    builder.append(String.join(", ", findClusterColumns(brAPIObjectType)));
                    builder.append(")");
                } else {
                    log.warn("No clustering columns found for table {}", brAPIObjectType.getName());
                }
            }

            if (tableUsing != null) {
                builder.append(" USING ");
                builder.append(tableUsing);
            }

            if (tableProperties != null && !tableProperties.isEmpty()) {
                builder.append(" TBLPROPERTIES (");
                builder.append(tableProperties.entrySet().stream().map(this::tableProperty).collect(Collectors.joining()));
                builder.append(")");
            }

            if (options.isAddingTableComments()) {
                builder.append(" COMMENT '");

                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(description)));

                builder.append("' ");
            }

            builder.append("; ") ;
            builder.append(System.lineSeparator());

            return success(builder.toString());
        }

        private String createTableName(BrAPIObjectType brAPIObjectType) {
            return metadata.getTablePrefix() != null ? metadata.getTablePrefix() + brAPIObjectType.getName() : brAPIObjectType.getName();
        }

        private String getTableDescription(BrAPIObjectType brAPIObjectType) {
            if (brAPIObjectType.getDescription() != null) {
                return removeCarriageReturns(escapeSingleSQLQuotes(brAPIObjectType.getDescription()));
            } else {
                return removeCarriageReturns(escapeSingleSQLQuotes(options.getDescriptionFor(brAPIObjectType)));
            }
        }

        private List<String> findClusterColumns(BrAPIObjectType brAPIObjectType) {

            List<String> clusterColumns = new ArrayList<>();

            options.getProperties().getIdPropertyFor(brAPIObjectType).ifPresentDoWithResult(brAPIObjectProperty -> clusterColumns.add(brAPIObjectProperty.getName()));

            for (BrAPIObjectProperty brAPIObjectProperty : brAPIObjectType.getProperties()) {

                BrAPIType dereferenceType = brAPIClassCache.dereferenceType(brAPIObjectProperty.getType());

                LinkType linkType = options.getProperties().getLinkTypeFor(brAPIObjectType, brAPIObjectProperty, dereferenceType).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE);

                if (dereferenceType instanceof BrAPIObjectType brAPIObjectPropertyObjectType && linkType.equals(ID)) {
                    options.getProperties().getLinkPropertiesFor(brAPIObjectPropertyObjectType).forEach(linkProperty -> clusterColumns.add(linkProperty.getName()));
                }
            }

            return clusterColumns;
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

            List<BrAPIObjectProperty> properties = brAPIObjectType.getProperties()
                .stream()
                .filter(brAPIObjectProperty -> getLinkTypeFor(brAPIObjectType, brAPIObjectProperty).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) != LinkType.NONE)
                .toList();

            return properties.stream().sorted(Comparator.comparing(BrAPIObjectProperty::getName)).map(property ->
                    createColumnDefinition(brAPIObjectType, property)
                        .conditionalMapResultToResponse(options.isAddingTableComments(), result -> addColumnComment(brAPIObjectType, property, result)))
                .collect(Response.toList()).mapResult(columns -> String.join(","+ System.lineSeparator(), columns));
        }

        private Response<String> createColumnDefinitions(LinkTable linkTable) {
            if (linkTable.getDereferenceItemType() instanceof BrAPIObjectType childBrAPIObjectType) {
                List<BrAPIObjectProperty> linkProperties = new ArrayList<>(options.getProperties().getLinkPropertiesFor(childBrAPIObjectType));
                linkProperties.addAll(options.getProperties().getLinkPropertiesFor( linkTable.getParentType()));

                return createLinkObjectDefinition(linkProperties) ;

            } else {
                return fail(Response.ErrorType.VALIDATION, String.format("Cannot create link table from for '%s' to non-object type '%s'", linkTable.getParentType().getName(), linkTable.getDereferenceItemType().getName()));
            }
        }

        private Response<LinkType> getLinkTypeFor(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property) {
            BrAPIType unwrappedType = unwrapType(property.getType());
            BrAPIType dereferencedType = brAPIClassCache.dereferenceType(unwrappedType);

            return options.getProperties().getLinkTypeFor(brAPIObjectType, property, dereferencedType);
        }

        private Response<String> addColumnComment(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property, String columnDefinition) {

            StringBuilder builder = new StringBuilder(columnDefinition);

            builder.append(" COMMENT '");

            if (property.getDescription() != null) {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(property.getDescription())));
            } else {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(options.getProperties().getDescriptionFor(brAPIObjectType, property))));
            }

            builder.append("' ");

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
            } else if (dereferencedType instanceof BrAPIObjectType brAPIObjectType) {
                return createObjectColumnDefinition(parentType, property, brAPIObjectType);
            } else if (dereferencedType instanceof BrAPIOneOfType brAPIOneOfType) {
                return createOneOfTypeColumnDefinition(parentType, property, brAPIOneOfType);
            } else if (dereferencedType instanceof BrAPIAllOfType brAPIAllOfType) {
                return fail(Response.ErrorType.VALIDATION, "All-of-types are not supported, should have been removed at this point!");
            } else if (dereferencedType instanceof BrAPIArrayType brAPIArrayType) {
                return createArrayColumnDefinition(parentType, property, brAPIArrayType);
            }

            return fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", dereferencedType != null ? dereferencedType.getName() : "null"));
        }

        private Response<String> createAdditionalInfoColumnDefinition(BrAPIObjectProperty property) {

            String builder = "  " +
                property.getName() +
                " MAP<STRING,STRING>";

            return success(builder);
        }

        private Response<String> createSimpleColumnDefinition(BrAPIObjectProperty property, String type) {

            StringBuilder builder = new StringBuilder();
            builder.append("  ");
            builder.append(property.getName());
            builder.append(" ");

            return findSimpleColumnType(type)
                .mapResult(builder::append)
                .mapResult(StringBuilder::toString);
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
                        createObjectColumnType(brAPIObjectType).mapResult(columnType -> property.getName() + " " + columnType);
                    case ID -> createLinkObjectDefinition(parentType, property, brAPIObjectType);
                    default ->
                        fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for property '%s' with item type '%s'", linkType, property.getName(), brAPIObjectType.getName()));
                });
        }

        private Response<String> createLinkObjectDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
            List<BrAPIObjectProperty> linkPropertiesFor = options.getProperties().getLinkPropertiesFor(brAPIObjectType);

            if (linkPropertiesFor.isEmpty()) {
                return fail(Response.ErrorType.VALIDATION, String.format("No link properties for property '%s' in '%s' with item type '%s'", property.getName(), parentType.getName(), brAPIObjectType.getName()));
            }

            return createLinkObjectDefinition(linkPropertiesFor) ;
        }

        private Response<String> createLinkObjectDefinition(List<BrAPIObjectProperty> linkProperties) {
            return linkProperties.stream()
                .filter(p -> p.getType() instanceof BrAPIPrimitiveType)
                .map(p -> createSimpleColumnDefinition(p, p.getType().getName())).collect(Response.toList())
                .mapResult(columnDefinitions -> String.join(", ", columnDefinitions));
        }

        private Response<String> createObjectColumnType(BrAPIObjectType brAPIObjectType) {
            StringBuilder builder = new StringBuilder();
            builder.append(" STRUCT<");

            return createColumnDefinitions(brAPIObjectType)
                .mapResult(builder::append)
                .mapResult(b -> b.append(">"))
                .mapResult(StringBuilder::toString);
        }

        private Response<String> createOneOfTypeColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIOneOfType brAPIOneOfType) {

            int i = 1;

            List<Response<String>> responses = new ArrayList<>(brAPIOneOfType.getPossibleTypes().size());

            for (BrAPIType type : brAPIOneOfType.getPossibleTypes()) {
                StringBuilder builder = new StringBuilder();
                builder.append(property.getName());
                builder.append(i);

                builder.append(" STRUCT<");

                if (type instanceof BrAPIObjectType brAPIObjectType) {
                    responses.add(createColumnDefinitions(brAPIObjectType)
                        .mapResult(builder::append)
                        .mapResult(b -> b.append(">"))
                        .mapResult(StringBuilder::toString));
                } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
                    responses.add(findSimpleColumnType(brAPIPrimitiveType.getName())
                        .mapResult(builder::append)
                        .mapResult(b -> b.append(">"))
                        .mapResult(StringBuilder::toString));
                } else {
                    responses.add(fail(Response.ErrorType.VALIDATION, String.format("Unknown embedded one of type '%s'", type.getName())));
                }

                ++i;
            }

            return responses.stream().collect(Response.toList()).mapResult(s -> String.join(", ", s));
        }

        private Response<String> createArrayColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIArrayType brAPIArrayType) {
            BrAPIType dereferencedItemType = brAPIClassCache.dereferenceType(brAPIArrayType.getItems());

            if (dereferencedItemType == null) {
                return fail(Response.ErrorType.VALIDATION, String.format("Cannot dereference '%s'", brAPIArrayType.getItems().getName()));
            }

            return options.getProperties().getLinkTypeFor(parentType, property, dereferencedItemType).mapResultToResponse(
                linkType -> createArrayColumnDefinition(parentType, property, dereferencedItemType, linkType)
            );
        }

        private Response<String> createArrayColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIType dereferencedItemType, LinkType linkType) {
            StringBuilder builder = new StringBuilder();

            return switch (linkType) {
                case EMBEDDED -> {
                    builder.append(property.getName());
                    builder.append(" ARRAY<");

                    yield createArrayColumnType(dereferencedItemType)
                    .mapResult(builder::append)
                    .mapResult(b -> b.append(">"))
                    .mapResult(StringBuilder::toString);
                }
                case ID -> {
                    if (dereferencedItemType instanceof BrAPIObjectType dereferencedItemTypeObjectType) {
                        builder.append(options.getProperties().getIdsPropertyNameFor(property));
                        builder.append(" ARRAY<");

                        yield options.getProperties().getIdPropertyFor(dereferencedItemTypeObjectType)
                            .mapResultToResponse(p -> findSimpleColumnType(p.getType().getName()))
                            .mapResult(builder::append)
                            .mapResult(b -> b.append(">"))
                            .mapResult(StringBuilder::toString);
                    } else {
                        yield fail(Response.ErrorType.VALIDATION, String.format("Unknown link ID array type '%s'", dereferencedItemType.getName()));
                    }
                }
                case SUB_QUERY -> {
                    LinkTable linkedTable = new LinkTable(parentType, property, dereferencedItemType);
                    linkTables.add(linkedTable);
                    builder.append("-- Link table '");
                    builder.append(createLinkTableName(linkedTable));
                    builder.append("' will be created separately");
                    yield success(builder.toString()) ;
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

        private String createLinkTableFullName(LinkTable linkedTable) {
            return  metadata.getTablePrefix() != null ? metadata.getTablePrefix() + createLinkTableName(linkedTable) : createLinkTableName(linkedTable);
        }

        private String createLinkTableName(LinkTable linkedTable) {
            return linkedTable.getDereferenceItemType().getName() + "By" + linkedTable.getParentType().getName() ;
        }

        private String getLinkTableDescription(LinkTable linkedTable) {
            return String.format("Link table for %s to %s on property %s", linkedTable.getParentType().getName(), linkedTable.getDereferenceItemType().getName(), linkedTable.getProperty().getName());
        }
    }

    @AllArgsConstructor
    @Getter
    private static class LinkTable {
        private final BrAPIObjectType parentType ;
        private final BrAPIObjectProperty property ;
        private final BrAPIType dereferenceItemType ;
    }
}
