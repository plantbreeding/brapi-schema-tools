package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Abstract base class for all sub-options (e.g. endpoint-level options within a generator).
 * Extends {@link AbstractGeneratorOptions} with a {@code summaryFormat} field.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSubOptions extends AbstractGeneratorOptions {

    private String summaryFormat;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(summaryFormat, "'summaryFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractSubOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.summaryFormat != null) {
            setSummaryFormat(overrideOptions.summaryFormat);
        }
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull String name) {
        return String.format(summaryFormat, name);
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param type the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type) {
        return getSummaryFor(type.getName());
    }
}

