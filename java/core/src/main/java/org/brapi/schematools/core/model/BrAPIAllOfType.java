package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIAllOfType implements BrAPIClass {
    String name;
    String description;
    String module;
    BrAPIMetadata metadata;
    List<BrAPIType> allTypes;
}