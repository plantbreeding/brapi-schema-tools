package org.brapi.schematools.core.model;

import lombok.Builder;
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
    String module;
    BrAPIMetadata metadata;
    List<BrAPIObjectProperty> properties;
}