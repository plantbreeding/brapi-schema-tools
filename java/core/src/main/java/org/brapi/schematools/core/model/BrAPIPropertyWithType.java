package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrAPIPropertyWithType {
    BrAPIObjectType parentType ;
    BrAPIObjectProperty property ;
    BrAPIType type ;
}
