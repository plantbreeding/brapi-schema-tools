package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * An Object definition that has {@link #properties}
 */
@Builder(toBuilder = true)
@Value
public class BrAPIObjectType implements BrAPIClass {
    String name;
    String description;
    @Singular
    List<Object> examples;
    String module;
    BrAPIMetadata metadata;
    List<BrAPIObjectProperty> properties;
    List<BrAPIObjectType> interfaces;
}