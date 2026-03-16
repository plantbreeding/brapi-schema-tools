package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrAPIObjectTypeWithProperty {
    BrAPIObjectType type ;
    BrAPIObjectProperty property ;
}
