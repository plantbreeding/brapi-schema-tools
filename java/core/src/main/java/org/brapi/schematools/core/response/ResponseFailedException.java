package org.brapi.schematools.core.response;

import lombok.Getter;

/**
 * Exception thrown when Response error is handled.
 */
@Getter
public class ResponseFailedException extends IllegalStateException {
    private final transient Response<?> failedResponse;

    /** Constructs a new exception with failed Response
     * @param failedResponse the failedResponse
     */
    public ResponseFailedException(Response<?> failedResponse) {
        super();
        this.failedResponse = failedResponse;
    }

}
