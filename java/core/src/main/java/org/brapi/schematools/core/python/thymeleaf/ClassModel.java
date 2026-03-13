package org.brapi.schematools.core.python.thymeleaf;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class ClassModel {
    String name;
    String nameSnakeCase ;
    String pluralNameSnakeCase ;
    String docstring ;
    String queryClassName ;
    String queryFunctionName ;

    String idPropertyName ;
    String idArgumentName ;

    Endpoints endpoints ;

    List<Filter> filters ;
    List<Filter> exampleFilters ;
    List<PropertyMapping> pluralToSingularGetParams ;
    List<PropertyMapping> unchangedGetParams ;
    List<PropertyMapping> ignoreGetParams ;

    List<ClassModel> exclusiveDependencies ;
    List<Dependency> commonDependencies ;
    List<Dependency> primaryDependencies ;

    FlattenConfig flattenConfig ;
    List<ClassModelField> requiredFields ;
    List<ClassModelField> scalarFields ;
    List<ClassModelField> nestedListFields ;
    List<ClassModelField> relationshipFields ;
}
