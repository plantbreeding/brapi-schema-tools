package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class AbstractOpenAPIMetadata implements Metadata {
    Map<String, String> summaries = new HashMap<>() ;
    Map<String, String> descriptions = new HashMap<>() ;


    public void override(AbstractOpenAPIMetadata overrideMetadata) {
        if (overrideMetadata.summaries != null) {
            summaries.putAll(overrideMetadata.summaries);
        }

        if (overrideMetadata.descriptions != null) {
            descriptions.putAll(overrideMetadata.descriptions);
        }
    }
}
