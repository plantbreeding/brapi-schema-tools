package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

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
    String type;
    boolean deprecated;
    boolean required;
    boolean nullable;
}
