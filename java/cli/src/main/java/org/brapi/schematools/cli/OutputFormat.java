package org.brapi.schematools.cli;

/**
 * Enumeration that provides the possible outputs for generator
 */
public enum OutputFormat {

    /**
     * Use this format to generate an OpenAPI specification
     */
    OPEN_API,
    /**
     * Use this format to generate an GraphQL schema
     */
    GRAPHQL,
    /**
     * Use this format to generate OWL specification in turtle format
     */
    OWL,
    /**
     * Use this format to generate Markdown for type and their field descriptions
     */
    MARKDOWN,
    /**
     * Use this format to generate Excel (xlsx) for types and their field descriptions
     */
    XLSX
}
