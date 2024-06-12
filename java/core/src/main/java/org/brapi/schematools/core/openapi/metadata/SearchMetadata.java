package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides metadata for the Search Post and Get endpoints
 */
@Getter
@Setter
public class SearchMetadata {
    Map<String, String> summaries = new HashMap<>() ;
    Map<String, String> descriptions = new HashMap<>() ;
}
