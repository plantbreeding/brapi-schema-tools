package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of List Queries
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListQueryOptions extends AbstractGraphQLQueryOptions {
    private String dataFieldName;

    @Getter(AccessLevel.PRIVATE)
    private boolean pagedDefault;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> input = new HashMap<>();

    private String pagingInputName;
    private String pageInputTypeName;
    private String pageTypeName;
    private String pageFieldName;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(dataFieldName, "'dataFieldName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(input,  "'input' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagingInputName, "'pagingInputName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pageInputTypeName, "'pageInputTypeName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pageTypeName, "'pageTypeName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pageFieldName,  "'pageFieldName' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Determine if any list query has paging
     * @return <code>true</code> if any list query has paging. <code>false</code> otherwise
     */
    public boolean hasPaging() {
        return pagedDefault || paged.values().stream().anyMatch(paged -> paged) ;
    }


    /**
     * Determines if the Query is paged for a specific primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the Query is paged for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(@NonNull String name) {
        return paged.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the Query is paged for a specific primary model
     * @param type the primary model
     * @return <code>true</code> if the Query is paged for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(@NonNull BrAPIType type) {
        return isPagedFor(type.getName()) ;
    }

    /**
     * Sets if the Query is paged for a specific primary model
     * @param name the name of the primary model
     * @param hasInput <code>true</code> if the Query is paged for a specific primary model, <code>false</code> otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListQueryOptions setPagedFor(String name, boolean hasInput) {
        paged.put(name, hasInput) ;

        return this ;
    }
}
