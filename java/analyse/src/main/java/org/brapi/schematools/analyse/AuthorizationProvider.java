package org.brapi.schematools.analyse;

import org.brapi.schematools.core.response.Response;

/**
 * Interface for authorisation
 */
public interface AuthorizationProvider {

    /**
     * Determines if authorisation is required no not
     * @return {@code true} if Authorisation is required or {@code false} if not.
     */
    default boolean required() {
        return false ;
    }

    /**
     * Gets a response containing the Authorisation Header is {@link #required()} is {@code true},
     * otherwise an empty response
     * @return a response containing the Authorisation Header.
     */
    default Response<String> getAuthorization() {
        return Response.empty() ;
    }
}
