package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Provides options for the generation of Table query methods
 * (GET &lt;entity&gt;/table endpoints that return CSV text).
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class TableOptions extends AbstractSubOptions {
    private String pathFormat;

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(TableOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pathFormat != null) {
            setPathFormat(overrideOptions.pathFormat);
        }
    }

    /**
     * Gets the table path for a specific path-item name.
     *
     * @param pathItemName the path-item name (e.g. {@code /observations})
     * @return the table path (e.g. {@code /observations/table})
     */
    @JsonIgnore
    public final String getPathFor(@NonNull String pathItemName) {
        return String.format(pathFormat, pathItemName);
    }
}
