package org.brapi.schematools.core.options;

import org.brapi.schematools.core.utils.Version;
import org.brapi.schematools.core.validiation.Validatable;

/**
 * A class that provide options to a generator or comparator
 */
public interface Options extends Validatable {

    /**
     * Get the version of Schema Tools that was used.
     * @return the version of Schema Tools
     */
    default String getSchemaToolsVersion() {
        return Version.getVersion();
    }
}
