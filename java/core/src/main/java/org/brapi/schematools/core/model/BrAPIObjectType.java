package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Builder(toBuilder = true)
@Value
public class BrAPIObjectType implements BrAPIType {

  String name ;
  String description ;
  String module ;
  boolean primaryModel ;
  List<BrAPIObjectProperty> properties ;
}