package org.brapi.schematools.analyse.query;

import io.swagger.models.HttpMethod;
import lombok.Builder;
import lombok.Value;

/**
 * Holds all the information require to query an Entity
 */
@Value
@Builder
public class Endpoint {
    String path;
    HttpMethod method;
    boolean paged;
}
