package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class FlattenConfig {
    List<String> relationshipFields ;
    List<String> arrayFields;
}
