package org.brapi.schematools.core.openapi.comparator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractGeneratorOptions;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

/**
 * AsciiDoc Render Options for the {@link OpenAPIComparator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class AsciiDocRenderOptions implements Options {

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AsciiDocRenderOptions overrideOptions) {

    }
}
