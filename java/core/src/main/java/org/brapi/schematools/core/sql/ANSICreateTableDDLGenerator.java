package org.brapi.schematools.core.sql;

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
        return createColumnDefinitions(brAPIObjectType).mapResultToResponse(columnDefinitions -> createTableDefinition(brAPIObjectType, columnDefinitions)) ;
    }

    private Response<String> createTableDefinition(BrAPIObjectType brAPIObjectType, String columnDefinitions) {

        StringBuilder builder = new StringBuilder() ;

        builder.append("\n\n") ;
        builder.append("CREATE TABLE ") ;
        builder.append(metadata.getTablePrefix()) ;
        builder.append(brAPIObjectType.getName()) ;
        builder.append(" (\n") ;

        builder.append(columnDefinitions) ;

        builder.append(") ") ;

        if (options.isClustering()) {
            List<String> clusterColumn = findClusterColumns(brAPIObjectType);

            if (!clusterColumn.isEmpty()) {
                builder.append(" CLUSTER BY ( ") ;
                builder.append(String.join(", ", findClusterColumns(brAPIObjectType))) ;
                builder.append(")") ;
            } else {
                log.warn("No clustering columns found for table {}", brAPIObjectType.getName());
            }
        }

        if (options.isAddingTableComments()) {
            builder.append(" COMMENT '") ;

            if (brAPIObjectType.getDescription() != null) {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(brAPIObjectType.getDescription())));
            } else {
                builder.append(removeCarriageReturns(escapeSingleSQLQuotes(options.getDescriptionFor(brAPIObjectType))));
            }

            builder.append("' ");
        }

        if (tableUsing != null) {
            builder.append(" USING ") ;
            builder.append(tableUsing) ;
        }

        if (tableProperties != null && !tableProperties.isEmpty()) {
            builder.append(" TBLPROPERTIES (") ;
            builder.append(tableProperties.entrySet().stream().map(this::tableProperty).collect(Collectors.joining())) ;
            builder.append(")") ;
        }

        builder.append("; \n") ;

        return success(builder.toString()) ;
    }

    private List<String> findClusterColumns(BrAPIObjectType brAPIObjectType) {

        List<String> clusterColumns = new ArrayList<>();

        options.getProperties().getIdPropertyFor(brAPIObjectType).ifPresentDoWithResult(brAPIObjectProperty -> clusterColumns.add(brAPIObjectProperty.getName()));

        for (BrAPIObjectProperty brAPIObjectProperty : brAPIObjectType.getProperties()) {

            BrAPIType dereferenceType = brAPIClassCache.dereferenceType(brAPIObjectProperty.getType());

            LinkType linkType = options.getProperties().getLinkTypeFor(brAPIObjectType, brAPIObjectProperty, dereferenceType).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) ;

            if (dereferenceType instanceof BrAPIObjectType brAPIObjectPropertyObjectType && linkType.equals(ID)) {
                options.getProperties().getLinkPropertiesFor(brAPIObjectPropertyObjectType).forEach(linkProperty -> clusterColumns.add(linkProperty.getName())); ;
            }
        }

        return clusterColumns ;
    }

    private void warn(Response<?> response) {
        log.warn(response.getMessagesCombined(", "));
    }

    private String tableProperty(Map.Entry<String, Object> entry) {
        StringBuilder builder = new StringBuilder() ;

        builder.append("'") ;
        builder.append(entry.getKey()) ;
        builder.append("' = ") ;

        if (entry.getValue() instanceof String) {
            builder.append("\"") ;
            builder.append(entry.getValue()) ;
            builder.append("\"") ;
        } else {
            builder.append(entry.getValue()) ;
        }

        return builder.toString() ;
    }

    private Response<String> createColumnDefinitions(BrAPIObjectType brAPIObjectType) {

        List<BrAPIObjectProperty> properties = brAPIObjectType.getProperties()
            .stream()
            .filter(brAPIObjectProperty -> options.getProperties().getLinkTypeFor(brAPIObjectType, brAPIObjectProperty).onFailDoWithResponse(this::warn).orElseResult(LinkType.NONE) != LinkType.NONE)
            .toList();

        return properties.stream().sorted(Comparator.comparing(BrAPIObjectProperty::getName)).map(property ->
            createColumnDefinition(brAPIObjectType, property)
                .conditionalMapResultToResponse(options.isAddingTableComments(), result -> addColumnComment(brAPIObjectType, property, result)))
            .collect(Response.toList()).mapResult(columns -> String.join(",\n", columns)) ;
    }

    private Response<String> addColumnComment(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property, String columnDefinition) {

        StringBuilder builder = new StringBuilder(columnDefinition) ;

        builder.append(" COMMENT '") ;

        if (property.getDescription() != null) {
            builder.append(removeCarriageReturns(escapeSingleSQLQuotes(property.getDescription())));
        } else {
            builder.append(removeCarriageReturns(escapeSingleSQLQuotes(options.getProperties().getDescriptionFor(brAPIObjectType, property))));
        }

        builder.append("' ");

        return success(builder.toString()) ;
    }

    private Response<String> createColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {
        BrAPIType dereferenceType = brAPIClassCache.dereferenceType(property.getType());

        if (property.getType().getName().equals("AdditionalInfo")) {
            return createAdditionalInfoColumnDefinition(property);
        } else if (dereferenceType instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
            return createSimpleColumnDefinition(property, brAPIPrimitiveType.getName()) ;
        } else if (dereferenceType instanceof BrAPIEnumType brAPIEnumType) {
            return createSimpleColumnDefinition(property, brAPIEnumType.getType()) ;
        } else if (dereferenceType instanceof BrAPIObjectType brAPIObjectType) {
            return createObjectColumnDefinition(parentType, property, brAPIObjectType) ;
        } else if (dereferenceType instanceof BrAPIOneOfType brAPIOneOfType) {
            return createOneOfTypeColumnDefinition(parentType, property, brAPIOneOfType) ;
        } else if (dereferenceType instanceof BrAPIAllOfType brAPIAllOfType) {
            return fail(Response.ErrorType.VALIDATION, "All-of-types are not supported, should have been removed at this point!") ;
        } else if (dereferenceType instanceof BrAPIArrayType brAPIArrayType) {
            return createArrayColumnDefinition(parentType, property, brAPIArrayType) ;
        }

        return fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", dereferenceType != null ? dereferenceType.getName() : "null")) ;
    }

    private Response<String> createAdditionalInfoColumnDefinition(BrAPIObjectProperty property) {

        String builder = "  " +
            property.getName() +
            " MAP<STRING,STRING>";

        return success(builder) ;
    }

    private Response<String> createSimpleColumnDefinition(BrAPIObjectProperty property, String type) {

        StringBuilder builder = new StringBuilder() ;
        builder.append("  ") ;
        builder.append(property.getName()) ;
        builder.append(" ") ;

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
                default ->  fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type));
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

        return linkPropertiesFor.stream()
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

        int i = 1 ;

        List<Response<String>> responses = new ArrayList<>(brAPIOneOfType.getPossibleTypes().size()) ;

        for (BrAPIType type: brAPIOneOfType.getPossibleTypes()) {
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

            ++i ;
        }

        return responses.stream().collect(Response.toList()).mapResult(s -> String.join(", ", s));
    }

    private Response<String> createArrayColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIArrayType brAPIArrayType) {
        BrAPIType dereferenceItemType = brAPIClassCache.dereferenceType(brAPIArrayType.getItems());

        if (dereferenceItemType == null) {
            return fail(Response.ErrorType.VALIDATION, String.format("Cannot deference '%s'", brAPIArrayType.getItems().getName()));
        }

        return options.getProperties().getLinkTypeFor(parentType, property, dereferenceItemType).mapResultToResponse(
            linkType -> createArrayColumnDefinition(property, dereferenceItemType, linkType)
        ) ;
    }

    private Response<String> createArrayColumnDefinition(BrAPIObjectProperty property, BrAPIType dereferenceItemType, LinkType linkType) {
        StringBuilder builder = new StringBuilder();
        builder.append(property.getName());
        builder.append(" ARRAY<");

        return switch (linkType) {
            case EMBEDDED -> createArrayColumnType(dereferenceItemType)
                .mapResult(builder::append)
                .mapResult(b -> b.append(">"))
                .mapResult(StringBuilder::toString);
            case ID -> {
                if (dereferenceItemType instanceof BrAPIObjectType brAPIObjectType) {
                    yield options.getProperties().getIdPropertyFor(brAPIObjectType)
                        .mapResultToResponse(p-> findSimpleColumnType(p.getType().getName()))
                        .mapResult(builder::append)
                        .mapResult(b -> b.append(">"))
                        .mapResult(StringBuilder::toString);
                } else {
                    yield fail(Response.ErrorType.VALIDATION, String.format("Unknown link ID array type '%s'", dereferenceItemType.getName()));
                }
            }
            default -> fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for Array with item type '%s'", linkType, dereferenceItemType.getName()));
        } ;
    }

    private Response<String> createArrayColumnType(BrAPIType itemType) {
        return switch (itemType) {
            case BrAPIObjectType brAPIObjectType -> createObjectColumnType(brAPIObjectType);
            case BrAPIPrimitiveType brAPIPrimitiveType -> findSimpleColumnType(brAPIPrimitiveType.getName());
            case BrAPIArrayType brAPIArrayType -> createArrayColumnType(brAPIArrayType.getItems());
            default ->
                fail(Response.ErrorType.VALIDATION, String.format("Unknown embedded array type '%s'", itemType.getName()));
        };
    }
}
