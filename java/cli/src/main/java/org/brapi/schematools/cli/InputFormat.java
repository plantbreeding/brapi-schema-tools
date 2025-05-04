package org.brapi.schematools.cli;

/**
 * Enumeration that provides the possible inputs
 */
public enum InputFormat {

    /**
     * Use this format to use an OpenAPI specification as input
     */
    OPEN_API,
    /**
     * Use this format to use a GraphQL schema as input
     */
    GRAPHQL,
    /**
     * Use this format to use an OWL specification in turtle format as input
     */
    OWL ;
}