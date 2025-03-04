package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.model.Request;
import lombok.Builder;
import lombok.Singular;
import lombok.ToString;
import lombok.Value;

import java.util.List;

/**
 * Holds all the information required for an HTTP API request
 */
@Value
@Builder
@ToString
public class APIRequest {
    String name;
    int index ;
    String entityName;
    @Singular
    List<ParameterLink> pathParameters ;
    @Singular
    List<ParameterLink> queryParameters ;
    Request validatorRequest;
    @Singular
    List<Variable> cacheVariables ;
    Object body ;
    @Singular
    List<String> prerequisites;

    /**
     * Builder class for APIRequest
     */
    public static class APIRequestBuilder {

    }
}
