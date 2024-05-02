package org.brapi.schematools.core;

import lombok.Getter;

@Getter
public class ResponseFailedException extends IllegalStateException {
    private final transient Response<?> failedResponse;

    public ResponseFailedException(Response<?> failedResponse) {
        super();
        this.failedResponse = failedResponse;
    }

}
