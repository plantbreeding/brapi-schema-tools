package org.brapi.schematools.analyse.query;

import lombok.Builder;
import lombok.Value;

/**
 * Holds all the information require to query the Endpoints for an Entity.
 */
@Value
@Builder
public class Endpoints {
    String entityName;
    Endpoint singleEndpoint;
    Endpoint listEndpoint;
    Endpoint searchEndpoint;
    Endpoint searchResultEndpoint;
    Endpoint createEndpoint;
    Endpoint updateEndpoint;
    Endpoint deleteEndpoint;
    String idParam;

    /**
     * Builder class for Endpoints
     */
    public static class EndpointsBuilder {}

}
