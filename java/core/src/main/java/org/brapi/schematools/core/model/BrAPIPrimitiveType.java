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
    public static final BrAPIType BOOLEAN = new BrAPIPrimitiveType("boolean");
    /**
     * Boolean Integer Type
     */
    public static final BrAPIType INTEGER = new BrAPIPrimitiveType("integer");
    /**
     * Boolean Number Type
     */
    public static final BrAPIType NUMBER = new BrAPIPrimitiveType("number");
    /**
     * Boolean String Type
     */
    public static final BrAPIType STRING = new BrAPIPrimitiveType("string");

    String name;

    private BrAPIPrimitiveType(String name) {
        this.name = name;
    }
}
