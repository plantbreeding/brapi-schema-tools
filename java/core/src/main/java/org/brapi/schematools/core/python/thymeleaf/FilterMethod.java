package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class FilterMethod {
    String methodName ;
    String paramName ;
    String argName ;
    String type ;
    String docstring ;
    String groupComment ;
    String exampleArg ;
}
