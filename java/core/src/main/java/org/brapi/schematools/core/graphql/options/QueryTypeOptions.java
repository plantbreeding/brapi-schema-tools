package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides options for the generation of the Query Type
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryTypeOptions {
    private String name;
    private boolean partitionedByCrop;
    @Setter(AccessLevel.PRIVATE)
    private SingleQueryOptions singleQuery ;
    @Setter(AccessLevel.PRIVATE)
    private ListQueryOptions listQuery;
    @Setter(AccessLevel.PRIVATE)
    private SearchQueryOptions searchQuery;

    public void validate() {
        assert name != null : "Name option on QueryType Options is null";

        assert singleQuery != null : "SingleQuery Options are null";
        assert listQuery != null : "ListQuery Options are null";
        assert searchQuery != null : "SearchQuery Options are null";

        singleQuery.validate() ;
        listQuery.validate() ;
        searchQuery.validate() ;
    }
}
