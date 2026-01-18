package org.brapi.schematools.core.openapi.comparator.options;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;

/**
 * AsciiDoc Render Options for the {@link org.brapi.schematools.core.openapi.comparator.OpenAPIComparator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class AsciiDocOutputOptions implements Options {

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AsciiDocOutputOptions overrideOptions) {

    }
}
