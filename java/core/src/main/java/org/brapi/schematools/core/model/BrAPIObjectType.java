package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIObjectType implements BrAPIClass {
    String name;
    String description;
    String module;
    BrAPIMetadata metadata;
    boolean request;
    List<BrAPIObjectProperty> properties;
}