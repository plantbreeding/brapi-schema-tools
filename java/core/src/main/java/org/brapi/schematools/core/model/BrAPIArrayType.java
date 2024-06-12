package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

/**
 * Array type that represents an array of items of type {@link #items}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIArrayType implements BrAPIType {
    String name;
    BrAPIType items;
}