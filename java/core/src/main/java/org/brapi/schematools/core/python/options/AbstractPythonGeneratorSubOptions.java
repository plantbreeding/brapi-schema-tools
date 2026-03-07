package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractGeneratorOptions;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Provides general options for the generation of Python endpoints / query builders.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractPythonGeneratorSubOptions extends AbstractGeneratorOptions {
    private String summaryFormat;

    public Validation validate() {
        return super.validate()
            .assertNotNull(summaryFormat,
                "'summaryFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractPythonGeneratorSubOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.summaryFormat != null) {
            setSummaryFormat(overrideOptions.summaryFormat);
        }
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the summary string for the primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull String name) {
        return String.format(summaryFormat, name);
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param type the primary model
     * @return the summary string for the primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type) {
        return getSummaryFor(type.getName());
    }
}
