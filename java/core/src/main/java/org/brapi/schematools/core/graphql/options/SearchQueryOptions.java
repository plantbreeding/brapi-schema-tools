package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.valdiation.Validation;

/**
 * Provides options for the generation of Search Queries
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SearchQueryOptions extends AbstractGraphQLQueryOptions {
    private String searchIdFieldName ;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(searchIdFieldName, "'searchIdFieldName' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(SearchQueryOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.searchIdFieldName != null) {
            setSearchIdFieldName(overrideOptions.searchIdFieldName); ;
        }
    }
}
