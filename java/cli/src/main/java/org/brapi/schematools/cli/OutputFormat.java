package org.brapi.schematools.cli;

/**
 * Enumeration that provides the possible outputs for generator
 */
public enum OutputFormat {

    /**
     * Use this format to generate an OpenAPI specification in YAML
     */
    OPEN_API,
    /**
     * Use this format to generate an OpenAPI specification in JSON
     */
    OPEN_API_JSON,
    /**
     * Use this format to generate a GraphQL schema
     */
    GRAPHQL,
    /**
     * Use this format to generate a GraphQL schema in introspection format
     */
    GRAPHQL_INTROSPECTION,
    /**
     * Use this format to generate OWL specification in turtle format
     */
    OWL,
    /**
     * Use this format to generate Markdown for type and their field descriptions
     */
    MARKDOWN,
    /**
     * Use this format to generate R Client for types and their fields
     */
    R,
    /**
     * Use this format to generate SQL for types and their fields
     */
    SQL,

    /**
     * Use this format to generate Excel (xlsx) for types and their field descriptions
     */
    XLSX
}
