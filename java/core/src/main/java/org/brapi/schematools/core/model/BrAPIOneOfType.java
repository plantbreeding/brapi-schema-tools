package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIOneOfType implements BrAPIType {

  String name ;
  String description ;
  List<BrAPIType> possibleTypes ;
}