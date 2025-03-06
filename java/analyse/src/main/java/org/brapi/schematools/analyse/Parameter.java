package org.brapi.schematools.analyse;

import lombok.Getter;
import lombok.Setter;

/**
 * Defines defines a parameter
 */
@Getter
@Setter
public class Parameter {
    String parameterName;
    String variableName ;
    String in;
    Object exampleValue;
}
