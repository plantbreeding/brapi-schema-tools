package org.brapi.schematools.core.python;

import org.brapi.schematools.core.model.BrAPIPrimitiveType;

public final class PythonTypeUtils {
    public static String findPyType(BrAPIPrimitiveType type) {
        return switch (type.getName()) {
            case "string" -> {
                String format = type.getFormat();
                if (format == null) {
                    yield "str";
                }
                yield switch (format) {
                    case "date-time" -> "datetime";
                    case "date" -> "date";
                    default -> "str";
                };
            }
            case "integer" -> "int";
            case "number" -> "float";
            case "boolean" -> "bool";
            default -> throw new RuntimeException(String.format("Type '%s' not supported yet", type.getName()));
        };
    }
}

