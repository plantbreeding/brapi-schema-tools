package org.brapi.schematools.core.brapischema;

/**
 * Exception thrown during the reading of BrAPI JSON Schema
 */
public class BrAPISchemaReaderException extends Exception {
    /** Constructs a new exception with the specified cause and a detail message of
     * (cause==null ? null : cause.toString()) (which typically contains the class and detail message of cause).
     * This constructor is useful for exceptions that are little more than wrappers for other throwables
     * (for example, java.security.PrivilegedActionException).
     * @param cause the cause (which is saved for later retrieval by the {@link #getCause} method).
     *              (A null value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    public BrAPISchemaReaderException(Exception cause) {
        super(cause) ;
    }
}
