package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryTypeOptions {
    @JsonProperty("generate")
    boolean generating;
    String name;
    boolean partitionedByCrop;
    SingleQueryOptions singleQuery;
    ListQueryOptions listQuery;
    SearchQueryOptions searchQuery;
}
