package org.brapi.schematools.cli;

import lombok.Getter;
import org.brapi.schematools.core.response.Response;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

@Getter
public class BrAPICommandException extends RuntimeException {
    Collection<Response.Error> allErrors ;
    public BrAPICommandException(String message) {
        super(message) ;

        this.allErrors = new ArrayList<>() ;
    }

    public BrAPICommandException(String message, Throwable cause) {
        super(message, cause) ;

        this.allErrors = new ArrayList<>() ;
    }

    public BrAPICommandException(String message, Collection<Response.Error> allErrors) {
        super(message) ;

        this.allErrors = allErrors ;
    }
}
