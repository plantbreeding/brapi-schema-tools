package org.brapi.schematools.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Value;

@Builder(toBuilder = true)
@Value
public class BrAPISchema {
  String name ;
  String module ;
  JsonNode schema ;
}
