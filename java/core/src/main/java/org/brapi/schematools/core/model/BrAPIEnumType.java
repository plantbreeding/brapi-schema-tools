package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIEnumType implements BrAPIClass {
    String name;
    String description;
    String module;
    BrAPIMetadata metadata;
    String type;
    List<BrAPIEnumValue> values;
}