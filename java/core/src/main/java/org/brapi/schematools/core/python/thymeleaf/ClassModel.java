package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ClassModel {
    String name;
    String docstring;
    List<ClassModelField> requiredFields;
    List<ClassModelField> scalarFields;
    List<ClassModelField> nestedListFields;
    List<ClassModelField> relationshipFields;
}
