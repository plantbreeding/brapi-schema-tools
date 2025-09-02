package org.brapi.schematools.core.openapi.generator;

import lombok.Builder;
import lombok.Getter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;

@Builder
@Getter
public class BrAPIObjectTypeWithProperty {
    BrAPIObjectType type ;
    BrAPIObjectProperty property ;
}
