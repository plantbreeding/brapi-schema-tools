package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.valdiation.Validation;

/**
 * Provides options for the generation of the Query Type
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class QueryTypeOptions implements Options {
    private String name;
    private boolean partitionedByCrop;
    @Setter(AccessLevel.PRIVATE)
    private SingleQueryOptions singleQuery ;
    @Setter(AccessLevel.PRIVATE)
    private ListQueryOptions listQuery;
    @Setter(AccessLevel.PRIVATE)
    private SearchQueryOptions searchQuery;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(name, "'name' option on QueryType Options is null")
            .assertNotNull(singleQuery,  "SingleQuery Options are null")
            .assertNotNull(listQuery,  "ListQuery Options are null")
            .assertNotNull(searchQuery,  "SearchQuery Options are null")
            .merge(singleQuery)
            .merge(listQuery)
            .merge(searchQuery) ;
    }
}
