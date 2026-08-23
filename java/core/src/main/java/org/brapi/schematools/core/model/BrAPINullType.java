package org.brapi.schematools.core.model;

/**
 * Represents an explicit JSON Schema {@code null} type, typically as one branch of a
 * nullable {@code oneOf}/{@code anyOf} union.
 */
public final class BrAPINullType implements BrAPIType {

    public static final String NAME = "null";

    private static final BrAPINullType INSTANCE = new BrAPINullType();

    private BrAPINullType() {
    }

    public static BrAPINullType instance() {
        return INSTANCE;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
