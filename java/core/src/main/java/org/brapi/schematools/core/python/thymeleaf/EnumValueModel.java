package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a single value within a Python enum generated from a {@code BrAPIEnumType}.
 */
@Value
@Builder
public class EnumValueModel {
    /** The Python identifier for this enum member (upper-snaked, e.g. {@code FIELD_STUDY}). */
    String name;
    /** The raw value as it appears in the JSON schema (e.g. {@code "fieldStudy"} or {@code 1}). */
    Object value;
    /**
     * {@code true} when the underlying BrAPI enum type is {@code "string"}, meaning the value
     * must be rendered with surrounding quotes in Python source (e.g. {@code "fieldStudy"}).
     * {@code false} for integer, number, boolean, etc.
     */
    boolean stringValue;
}
