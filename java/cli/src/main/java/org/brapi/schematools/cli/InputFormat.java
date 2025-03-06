package org.brapi.schematools.cli;

/**
 * Enumeration that provides the possible inputs for generator
 */
public enum InputFormat {

    /**
     * Use this format to use an OpenAPI specification as input
     */
    OPEN_API,
    /**
     * Use this format to use an GraphQL schema as input
     */
    GRAPHQL;
}