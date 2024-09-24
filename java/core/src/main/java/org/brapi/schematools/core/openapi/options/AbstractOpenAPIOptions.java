package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractOptions;
import org.brapi.schematools.core.valdiation.Validation;


/**
 * Provides general options for the generation of Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractOpenAPIOptions extends AbstractOptions {
    private String summaryFormat;

    public Validation validate() {
        return super.validate().assertNotNull(summaryFormat, "'summaryFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Gets the summary for a specific primary model
     * @param name the name of the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    private final String getSummaryFor(@NonNull String name) {
        return String.format(summaryFormat, name) ;
    }

    /**
     * Gets the summary for a specific primary model
     * @param type the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type) {
        return getSummaryFor(type.getName());
    }
}

