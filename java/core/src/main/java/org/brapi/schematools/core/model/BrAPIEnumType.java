package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * A BrAPI Class that provides a list of possible {@link #values} of a specific {@link #type}.
 */
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