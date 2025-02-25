package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.model.Request;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;

import java.util.List;

/**
 * Holds all the information required for an HTTP API request
 */
@Value
@Builder
public class APIRequest {
    String name;
    String entityName;
    @Singular
    List<String> pathParameters;
    Request validatorRequest;
}
