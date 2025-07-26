package org.brapi.schematools.core.openapi.comparator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

/**
 * HTML Options for the {@link org.brapi.schematools.core.openapi.comparator.OpenAPIComparator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class HTMLRenderOptions implements Options {
    private String title;
    private String linkCss;
    @Getter(AccessLevel.PRIVATE)
    private Boolean showAllChanges;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(showAllChanges, "'showAllChanges' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(HTMLRenderOptions overrideOptions) {
        if (overrideOptions.title != null) {
            this.title = overrideOptions.title;
        }

        if (overrideOptions.title != null) {
            this.linkCss = overrideOptions.linkCss;
        }

        if (overrideOptions.title != null) {
            this.showAllChanges = overrideOptions.showAllChanges;
        }
    }

    /**
     * Determines if the Comparator HTML render should show all changes
     * @return {@code true} if the Comparator HTML render should show all changes, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isShowingAllChanges() {
        return showAllChanges ;
    }
}
