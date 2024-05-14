package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class BrAPIObjectProperty {
  String name ;
  String description ;
  BrAPIType type ;
  boolean required ;
}
