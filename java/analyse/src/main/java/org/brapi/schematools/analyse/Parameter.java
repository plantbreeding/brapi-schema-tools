package org.brapi.schematools.analyse;

import lombok.Builder;
import lombok.Value;

/**
 * Defines a Parameter, which links a parameter in a query, path or body to a variable.
 */
@Value
@Builder
public class Parameter {
    String variableName ;
    String parameterName;
}
