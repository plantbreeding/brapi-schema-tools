package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

/**
 * The property definition for a BrAPI Object.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIObjectProperty {
    String name;
    String description;
    BrAPIType type;
    boolean required;
    String referencedAttribute;
    BrAPIRelationshipType relationshipType;
}
