package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.valdiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toSentenceCase;

/**
 * Abstract base class for all GraphQL Options
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AbstractGraphQLQueryOptions extends AbstractGraphQLOptions {
    @Getter(AccessLevel.PRIVATE)
    private String responseTypeNameFormat;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> input = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(responseTypeNameFormat, "'responseTypeNameFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(input,  "'input' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractGraphQLQueryOptions overrideOptions) {
        super.override(overrideOptions) ;

        if (overrideOptions.responseTypeNameFormat != null) {
            setResponseTypeNameFormat(overrideOptions.responseTypeNameFormat);
        }

        input.putAll(overrideOptions.input);
    }

    /**
     * Gets the response type name of the query for a specific primary model
     * @param queryName the name of the query
     * @return the response type name of the query for a specific primary model
     */
    @JsonIgnore
    public final String getResponseTypeNameForQuery(@NonNull String queryName) {
        return String.format(responseTypeNameFormat, toSentenceCase(queryName)) ;
    }

    /**
     * Determines if the Query accepts an input object for a specific primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the Query accepts an input object for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean hasInputFor(@NonNull String name) {
        return input.getOrDefault(name, true) ;
    }

    /**
     * Determines if the Query accepts an input object for a specific primary model
     * @param type the primary model
     * @return <code>true</code> if the Query accepts an input object for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean hasInputFor(@NonNull BrAPIType type) {
        return hasInputFor(type.getName()) ;
    }

    /**
     * Sets if the Query accepts an input object for a specific primary model
     * @param name the name of the primary model
     * @param hasInput <code>true</code> if the Query accepts an input object for a specific primary model, <code>false</code> otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractGraphQLQueryOptions setInputFor(String name, boolean hasInput) {
        input.put(name, hasInput) ;

        return this ;
    }
}
