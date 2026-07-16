package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;
import java.util.Set;

/**
 * A BrAPI wrapper around AdditionalProperties
 */
@Builder(toBuilder = true)
@Value
public class BrAPIAdditionalProperties {
    String name;
    String description;
    @Singular
    List<Object> examples ;
    Set<String> type;
    boolean deprecated;
    boolean required;
    Boolean nullable;

    public boolean isNullable() {
        return nullable != null && nullable ;
    }
}
