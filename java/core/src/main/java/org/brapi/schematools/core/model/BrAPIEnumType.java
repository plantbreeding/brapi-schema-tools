package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIEnumType implements BrAPIType {

    String name;
    String type;
    String description;
    String module;
    List<BrAPIEnumValue> values;
}