package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ClassModelField {
    String name;
    String type;
    String itemType;
}
