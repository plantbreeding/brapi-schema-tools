package org.brapi.schematools.core.model;

import lombok.Builder;
import lombok.Value;

/**
 * The BrAPI metadata associated with a {@link BrAPIClass}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIMetadata {
    boolean primaryModel ;
}
