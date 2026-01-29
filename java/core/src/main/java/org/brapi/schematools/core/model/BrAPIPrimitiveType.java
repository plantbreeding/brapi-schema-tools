package org.brapi.schematools.core.model;

import lombok.Value;

/**
 * A scalar or primitive type, which can be one of 4 possible instances:
 * {@link #BOOLEAN}, {@link #INTEGER}, {@link #NUMBER}, or {@link #STRING}.
 */
@Value
public class BrAPIPrimitiveType implements BrAPIType {

    /**
     * Boolean Primitive Type
     */
    public static final String BOOLEAN = "boolean";
    /**
     * Boolean Integer Type
     */
    public static final String INTEGER = "integer";
    /**
     * Boolean Number Type
     */
    public static final String NUMBER = "number";
    /**
     * Boolean String Type
     */
    public static final String STRING = "string";

    String name;
    String format;

    private BrAPIPrimitiveType(String name) {
        this(name, null) ;
    }

    private BrAPIPrimitiveType(String name, String format) {
        this.name = name;
        this.format = format;
    }

    public static BrAPIPrimitiveType booleanType() {
        return new BrAPIPrimitiveType(BOOLEAN);
    }

    public static BrAPIPrimitiveType booleanType(String format) {
        return new BrAPIPrimitiveType(BOOLEAN, format);
    }

    public static BrAPIPrimitiveType integerType() {
        return new BrAPIPrimitiveType(INTEGER);
    }

    public static BrAPIPrimitiveType integerType(String format) {
        return new BrAPIPrimitiveType(INTEGER, format);
    }

    public static BrAPIPrimitiveType numberType() {
        return new BrAPIPrimitiveType(NUMBER);
    }

    public static BrAPIPrimitiveType numberType(String format) {
        return new BrAPIPrimitiveType(NUMBER, format);
    }

    public static BrAPIPrimitiveType stringType() {
        return new BrAPIPrimitiveType(STRING);
    }

    public static BrAPIPrimitiveType stringType(String format) {
        return new BrAPIPrimitiveType(STRING, format);
    }
}
