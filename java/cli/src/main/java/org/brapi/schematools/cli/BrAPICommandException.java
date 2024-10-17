package org.brapi.schematools.cli;

import lombok.Getter;
import org.brapi.schematools.core.response.Response;

import java.util.Collection;

@Getter
public class BrAPICommandException extends RuntimeException {
    Collection<Response.Error> allErrors ;

    public BrAPICommandException(String message, Collection<Response.Error> allErrors) {
        super(message) ;

        this.allErrors = allErrors ;
    }
}
