package org.brapi.schematools.analyse.query;

import lombok.Builder;
import lombok.Value;

/**
 * Holds all the information require to query an Entity
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
}
