package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides metadata for the Put endpoints
 */
@Getter
@Setter
public class PutMetadata implements Metadata {
    Map<String, String> summaries = new HashMap<>() ;
    Map<String, String> descriptions = new HashMap<>() ;
}
