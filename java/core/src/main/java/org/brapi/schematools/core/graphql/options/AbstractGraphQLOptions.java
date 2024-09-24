package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractOptions;
import org.brapi.schematools.core.valdiation.Validation;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;


/**
 * Provides general options for the generation of Endpoints
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractGraphQLOptions extends AbstractOptions {
    private boolean pluralisingName;
    @Getter(AccessLevel.PRIVATE)
    private String nameFormat;

    public Validation validate() {
        return super.validate().assertNotNull("'nameFormat' option on %s is null", this.getClass().getSimpleName());
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

