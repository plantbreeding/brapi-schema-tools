package org.brapi.schematools.analyse;

import lombok.Builder;
import lombok.Value;

/**
 * Defines a Variable and a json path to get the value of the variable from a response body, and
 * the parameter name for which this variable will be used, if different from the variable name.
 */
@Value
@Builder
public class Variable {
    String variableName ;
    String parameterName;
    String jsonPath ;
}
