package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Provides options for the generation of Table query methods
 * (GET &lt;entity&gt;/table endpoints that return CSV text).
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class TableOptions extends AbstractListOptions {
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
    public void override(TableOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pathFormat != null) {
            setSummaryFormat(overrideOptions.pathFormat);
        }
    }
}
