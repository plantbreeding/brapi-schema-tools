package org.brapi.schematools.core.brapischema;

/**
 * Exception thrown during the reading of BrAPI JSON Schema
 */
public class BrAPISchemaReaderException extends Exception {
    public BrAPISchemaReaderException(Exception e) {
        super(e);
    }
}
