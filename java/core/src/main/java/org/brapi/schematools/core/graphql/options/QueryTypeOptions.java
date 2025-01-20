package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private Boolean partitionedByCrop;
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

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(QueryTypeOptions overrideOptions) {
        if (overrideOptions.name != null) {
            setName(overrideOptions.name); ;
        }

        if (overrideOptions.partitionedByCrop != null) {
            setPartitionedByCrop(overrideOptions.partitionedByCrop); ;
        }

        if (overrideOptions.searchQuery != null) {
            searchQuery.override(overrideOptions.searchQuery) ;
        }

        if (overrideOptions.listQuery != null) {
            listQuery.override(overrideOptions.listQuery) ;
        }

        if (overrideOptions.searchQuery != null) {
            searchQuery.override(overrideOptions.searchQuery) ;
        }
    }

    /**
     * Determines if the query is partition by crop, so that queries are not across crops
     * @return <code>true</code> if the query is partition by crop, so that queries are not across crops, <code>false </code> otherwise
     */
    public boolean isPartitionedByCrop() {
        return partitionedByCrop != null && partitionedByCrop ;
    }
}
