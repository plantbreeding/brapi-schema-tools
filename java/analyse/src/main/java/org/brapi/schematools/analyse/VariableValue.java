package org.brapi.schematools.analyse;

import lombok.Builder;
import lombok.Value;

/**
 * Defines a Value for a Variable, and
 * the parameter name for which this variable value will be used, if different from the variable name.
 */
@Value
@Builder
public class VariableValue {
    String variableName ;
    String parameterName;
    Object value ;
}
