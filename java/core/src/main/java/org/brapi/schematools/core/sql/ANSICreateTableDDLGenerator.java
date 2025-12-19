package org.brapi.schematools.core.sql;

import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.BrAPIClassCacheUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

public class ANSICreateTableDDLGenerator implements CreateTableDDLGenerator {

    private final SQLGeneratorOptions options ;
    private final SQLGeneratorMetadata metadata ;
    private final Map<String, BrAPIClass> brAPIClasses ;

    public ANSICreateTableDDLGenerator(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, List<BrAPIClass> brAPIClasses) {
        this.options = options;
        this.metadata = metadata;
        this.brAPIClasses = new BrAPIClassCacheUtil().createMap(brAPIClasses) ;
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

        builder.append(")\n") ;

        builder.append(" USING delta\n") ;
        builder.append(" TBLPROPERTIES (\n") ;
        builder.append("  'delta.minReaderVersion' = '1', \n") ;
        builder.append("  'delta.minWriterVersion' = '2') ; \n") ;

        return success(builder.toString()) ;
    }

    private Response<String> createColumnDefinitions(BrAPIObjectType brAPIObjectType) {

        List<BrAPIObjectProperty> properties = brAPIObjectType.getProperties()
            .stream()
            .filter(brAPIObjectProperty -> options.getProperties().getLinkTypeFor(brAPIObjectType, brAPIObjectProperty) != LinkType.NONE)
            .toList();

        return properties.stream().sorted(Comparator.comparing(BrAPIObjectProperty::getName)).map(property -> createColumnDefinition(brAPIObjectType, property)).collect(Response.toList()).mapResult(columns -> String.join(",\n", columns)) ;
    }

    private Response<String> createColumnDefinition(BrAPIObjectType parentType, BrAPIObjectProperty property) {
        BrAPIType type = dereferenceType(property.getType());

        if (property.getType().getName().equals("AdditionalInfo")) {
            return createAdditionalInfoColumnDefinition(property);
        } else if (type instanceof BrAPIPrimitiveType brAPIPrimitiveType) {
            return createSimpleColumnDefinition(property, brAPIPrimitiveType.getName()) ;
        } else if (type instanceof BrAPIEnumType brAPIEnumType) {
            return createSimpleColumnDefinition(property, brAPIEnumType.getType()) ;
        } else if (type instanceof BrAPIObjectType brAPIObjectType) {
            return createObjectColumnDefinition(parentType, property, brAPIObjectType) ;
        } else if (type instanceof BrAPIOneOfType brAPIOneOfType) {
            return createOneOfTypeColumnDefinition(parentType, property, brAPIOneOfType) ;
        } else if (type instanceof BrAPIAllOfType brAPIAllOfType) {
            return fail(Response.ErrorType.VALIDATION, "All-of-types are not supported, should have been removed at this point!") ;
        } else if (type instanceof BrAPIArrayType brAPIArrayType) {
            return createArrayColumnDefinition(parentType, property, brAPIArrayType) ;
        }

        return fail(Response.ErrorType.VALIDATION, String.format("Unknown type '%s'", type != null ? type.getName() : "null")) ;
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
        LinkType linkType = options.getProperties().getLinkTypeFor(parentType, property, brAPIObjectType);

        return switch (linkType) {
            case EMBEDDED -> createObjectColumnType(brAPIObjectType).mapResult(columnType -> property.getName() + " " + columnType);
            case ID -> options.getProperties().getLinkPropertiesFor(brAPIObjectType)
                .stream()
                .filter(p -> p.getType() instanceof BrAPIPrimitiveType)
                .map(p -> createSimpleColumnDefinition(p, p.getType().getName())).collect(Response.toList())
                .mapResult(columnDefinitions -> String.join(", ", columnDefinitions));
            default -> fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for property '%s' with item type '%s'", linkType, property.getName(), brAPIObjectType.getName()));
        } ;
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
        BrAPIType itemType = dereferenceType(brAPIArrayType.getItems());

        LinkType linkType = options.getProperties().getLinkTypeFor(parentType, property, itemType) ;

        if (itemType == null) {
            return fail(Response.ErrorType.VALIDATION, String.format("Cannot deference '%s'", brAPIArrayType.getItems().getName()));
        }

        StringBuilder builder = new StringBuilder();
        builder.append(property.getName());
        builder.append(" ARRAY<");

        return switch (linkType) {
            case EMBEDDED -> createArrayColumnType(itemType)
                .mapResult(builder::append)
                    .mapResult(b -> b.append(">"))
                    .mapResult(StringBuilder::toString);
            case ID -> {
                if (itemType instanceof BrAPIObjectType brAPIObjectType) {
                    yield options.getProperties().getIdPropertyFor(brAPIObjectType)
                        .mapResultToResponse(p-> findSimpleColumnType(p.getType().getName()))
                        .mapResult(builder::append)
                        .mapResult(b -> b.append(">"))
                        .mapResult(StringBuilder::toString);
                } else {
                    yield fail(Response.ErrorType.VALIDATION, String.format("Unknown link ID array type '%s'", itemType.getName()));
                }
            }
            default -> fail(Response.ErrorType.VALIDATION, String.format("Unknown supported link type '%s' for Array with item type '%s'", linkType, itemType.getName()));
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

    private BrAPIType dereferenceType(BrAPIType type) {
        if (type instanceof BrAPIReferenceType) {
            return brAPIClasses.get(type.getName());
        } else {
            return type;
        }
    }
}
