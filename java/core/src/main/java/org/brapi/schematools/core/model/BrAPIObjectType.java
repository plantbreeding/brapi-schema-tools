package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * An Object definition that has {@link #properties}
 */
@Builder(toBuilder = true)
@Value
public class BrAPIObjectType implements BrAPIClass {
    String name;
    String description;
    boolean deprecated;
    Boolean nullable;
    @Singular
    List<Object> examples;
    String module;
    BrAPIMetadata metadata;
    BrAPIAdditionalProperties additionalProperties;
    List<BrAPIObjectProperty> properties;
    List<BrAPIObjectType> interfaces;

    @Override
    public boolean isNullable() {
        return nullable != null && nullable ;
    }
}