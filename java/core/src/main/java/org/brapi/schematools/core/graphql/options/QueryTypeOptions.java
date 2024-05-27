package org.brapi.schematools.core.graphql.options;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QueryTypeOptions {
  boolean generate ;
  String name ;
  SingleQueryOptions singleQuery ;
}
