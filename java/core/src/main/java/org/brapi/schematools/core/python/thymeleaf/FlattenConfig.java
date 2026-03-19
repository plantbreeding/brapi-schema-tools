package org.brapi.schematools.core.python.thymeleaf;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FlattenConfig {
    List<String> relationshipFields ;
    List<String> arrayFields;
}
