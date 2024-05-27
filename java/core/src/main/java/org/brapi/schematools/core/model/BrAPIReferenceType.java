package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class BrAPIReferenceType implements BrAPIType {

  String name ;
}