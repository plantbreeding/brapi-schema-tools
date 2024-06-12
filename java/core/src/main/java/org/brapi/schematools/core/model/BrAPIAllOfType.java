package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

/**
 * Represents a type that takes all of properties from the wrapped types
 */
@Builder(toBuilder = true)
@Value
public class BrAPIAllOfType implements BrAPIClass {
    String name;
    String description;
    String module;
    BrAPIMetadata metadata;
    List<BrAPIType> allTypes;
}