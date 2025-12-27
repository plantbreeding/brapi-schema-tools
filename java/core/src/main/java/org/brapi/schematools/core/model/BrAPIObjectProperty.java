package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.ArrayList;
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
    BrAPIType type;
    boolean required;
    boolean nullable;
    String referencedAttribute;
    BrAPIRelationshipType relationshipType;
}
