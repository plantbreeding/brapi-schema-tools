package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * The property definition for a BrAPI Object.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIObjectProperty {
    String name;
    String description;
    @Singular
    List<Object> examples ;
    Object defaultValue ;
    BrAPIType type;
    boolean deprecated;
    boolean required;
    Boolean nullable;
    String referencedAttribute;
    BrAPIRelationshipType relationshipType;

    public boolean isNullable() {
        return nullable != null && nullable ;
    }
}
