package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractOptions;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;


/**
 * Provides general options for the generation of Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractGraphQLOptions extends AbstractOptions {
    private String nameFormat;

    public void validate() {
        super.validate();
        assert nameFormat != null : String.format("'nameFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Gets the name of the query or mutation for a specific primary model
     * @param name the name of the primary model
     * @return the name of the query or mutation for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull String name) {
        return String.format(nameFormat, nameFormat.startsWith("%s") ? toParameterCase(name) : name) ;
    }

    /**
     * Gets the name of query or mutation for a specific primary model
     * @param type the primary model
     * @return the name of the query or mutation for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull BrAPIType type) {
        return getNameFor(type.getName());
    }


}

