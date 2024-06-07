package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class SingleGetMetadata {
    Map<String, String> summaries = new HashMap<>() ;
    Map<String, String> descriptions = new HashMap<>() ;
}
