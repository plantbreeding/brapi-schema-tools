package org.brapi.schematools.analyse;

import com.atlassian.oai.validator.model.Request;
import lombok.Builder;
import lombok.Value;

/**
 * Defines an Endpoint
 */
@Value
@Builder
public class Endpoint {
    String path;
    Request.Method method;
    String entityName;
    String category;

    @Override
    public String toString() {
        return String.format("%s %s %s : %s" , entityName, category, method.name(), path) ;
    }
}
