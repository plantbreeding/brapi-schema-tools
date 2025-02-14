package org.brapi.schematools.cli;

import lombok.Getter;
import org.brapi.schematools.core.response.Response;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Exception for BrAPI Commands
 */
@Getter
public class BrAPICommandException extends RuntimeException {
    private final Collection<Response.Error> allErrors ;

    /**
     * Create a BrAPICommandException with a simple message
     * @param message the error message
     */
    public BrAPICommandException(String message) {
        super(message) ;

        this.allErrors = new ArrayList<>() ;
    }

    /**
     * Create a BrAPICommandException from another Exception
     * @param message the error message
     * @param cause the cause of the exception
     */
    public BrAPICommandException(String message, Throwable cause) {
        super(message, cause) ;

        this.allErrors = new ArrayList<>() ;
    }

    /**
     * Create a BrAPICommandException from a collection of response errors
     * @param message the error message
     * @param errors a collection of response errors
     */
    public BrAPICommandException(String message, Collection<Response.Error> errors) {
        super(message) ;

        this.allErrors = errors ;
    }
}
