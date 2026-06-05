package org.brapi.schematools.core.options;

import lombok.AccessLevel;
import lombok.Getter;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Provides options for the generation of Search Table query methods
 * (POST search/&lt;entity&gt;/table endpoints that return a URL to a CSV/ZIP file).
 */
public class SearchTableOptions extends AbstractSubOptions {
    @Getter(AccessLevel.PUBLIC)
    private String pathFormat;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pathFormat, "'pathFormat' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(SearchTableOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pathFormat != null) {
            setSummaryFormat(overrideOptions.pathFormat);
        }
    }
}
