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
 * Markdown Render Options for the {@link org.brapi.schematools.core.openapi.comparator.OpenAPIComparator}..
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class MarkdownOutputOptions implements Options {
    @Getter(AccessLevel.PRIVATE)
    private Boolean showChangedMetadata;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(showChangedMetadata, "'showChangedMetadata' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(MarkdownOutputOptions overrideOptions) {
        if (overrideOptions.showChangedMetadata != null) {
            this.showChangedMetadata = overrideOptions.showChangedMetadata;
        }
    }

    /**
     * Determines if the Markdown Render should show changes in the metadata such as summary and description
     * @return {@code true} if the Markdown Render should show changes in the metadata such as summary and description, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isShowingChangedMetadata() {
        return showChangedMetadata ;
    }
}
