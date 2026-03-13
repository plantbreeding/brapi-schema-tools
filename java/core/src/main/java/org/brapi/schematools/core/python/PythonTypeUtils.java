package org.brapi.schematools.core.python;

import org.brapi.schematools.core.model.BrAPIPrimitiveType;
import org.brapi.schematools.core.response.Response;

import static org.brapi.schematools.core.response.Response.fail;

public final class PythonTypeUtils {
    public static Response<String> findPyType(BrAPIPrimitiveType type) {
        return switch (type.getName()) {
            case "string" -> {
                String format = type.getFormat();
                if (format == null) {
                    yield Response.success("str");
                }
                yield switch (format) {
                    case "date-time" -> Response.success("datetime");
                    case "date" -> Response.success("date");
                    default -> Response.success("str");
                };
            }
            case "integer" -> Response.success("int");
            case "number" -> Response.success("float");
            case "boolean" -> Response.success("bool");
            default -> fail(Response.ErrorType.VALIDATION, String.format("Type '%s' not supported yet", type.getName()));
        };
    }
}

