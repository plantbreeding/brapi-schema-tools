package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

/**
 * A reference to another named type with {@link #name}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIReferenceType implements BrAPIType {

    String name;
}