package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PropertyMapping {
    String pluralName ;
    String singularName ;

    public boolean isUnchanged() {
        return pluralName.equals(singularName);
    }
}
