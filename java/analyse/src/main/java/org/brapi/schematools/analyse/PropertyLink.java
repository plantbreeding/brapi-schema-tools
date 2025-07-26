package org.brapi.schematools.analyse;

import io.swagger.v3.oas.models.media.Schema;
import lombok.Builder;
import lombok.Value;

/**
 * Defines the link between an OpenAPI Property in a request body to a variable.
 */
@Value
@Builder
public class PropertyLink {
    String propertyName;
    Schema schema;
    String variableName ;
}
