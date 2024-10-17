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
}
