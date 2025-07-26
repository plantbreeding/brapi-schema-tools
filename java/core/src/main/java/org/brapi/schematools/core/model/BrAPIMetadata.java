package org.brapi.schematools.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * The BrAPI metadata associated with a {@link BrAPIClass}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIMetadata {
    boolean primaryModel ;
    boolean request ;
    boolean parameters ;
    @JsonProperty("interface")
    boolean interfaceClass ;
}
