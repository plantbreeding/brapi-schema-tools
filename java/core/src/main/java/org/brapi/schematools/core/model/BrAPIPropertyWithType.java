package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BrAPIPropertyWithType {
    BrAPIObjectProperty property ;
    BrAPIType type ;
}
