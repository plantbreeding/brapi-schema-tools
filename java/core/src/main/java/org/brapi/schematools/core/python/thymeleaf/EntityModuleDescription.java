package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class EntityModuleDescription {
    String moduleName;
    List<String> classNames;
}
