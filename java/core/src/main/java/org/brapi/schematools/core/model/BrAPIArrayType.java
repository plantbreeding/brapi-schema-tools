package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class BrAPIArrayType implements BrAPIType {
    String name;
    BrAPIType items;
}