package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * A BrAPI Class that represents one and only one {@link #possibleTypes}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIOneOfType implements BrAPIClass {
    String name;
    String description;
    @Singular
    List<Object> examples ;
    String module;
    BrAPIMetadata metadata;
    List<BrAPIType> possibleTypes;
}