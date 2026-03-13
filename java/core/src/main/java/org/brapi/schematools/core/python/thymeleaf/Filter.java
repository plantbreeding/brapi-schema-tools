package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Filter {
    String methodName ;
    String paramName ;
    String argName ;
    String type ;
    String itemType ;
    boolean required;
    String docstring ;
    String groupComment ;
    String exampleArg ;
    String exampleArg2 ;
    String exampleArgs ;
    String exampleMultipleArgs;
}
