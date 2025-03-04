package org.brapi.schematools.analyse;

import lombok.Builder;
import lombok.Value;

/**
 * Defines the link between an OpenAPI Parameter in a query or path or body to a variable.
 */
@Value
@Builder
public class ParameterLink {
    io.swagger.v3.oas.models.parameters.Parameter parameter;
    String variableName ;

    /**
     * The name of the parameter
     * @return the name of the parameter
     */
    public String getParameterName() {
        return parameter.getName() ;
    }
}
