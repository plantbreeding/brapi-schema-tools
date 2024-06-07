package org.brapi.schematools.core.model;

import lombok.Value;

@Value
public class BrAPIPrimitiveType implements BrAPIType {

    public static final BrAPIType BOOLEAN = new BrAPIPrimitiveType("boolean");
    public static final BrAPIType INTEGER = new BrAPIPrimitiveType("integer");
    public static final BrAPIType NUMBER = new BrAPIPrimitiveType("number");
    public static final BrAPIType STRING = new BrAPIPrimitiveType("string");

    String name;

    private BrAPIPrimitiveType(String name) {
        this.name = name;
    }
}
