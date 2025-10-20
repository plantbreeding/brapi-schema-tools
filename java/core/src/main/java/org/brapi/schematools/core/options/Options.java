package org.brapi.schematools.core.options;

import org.brapi.schematools.core.utils.Version;
import org.brapi.schematools.core.validiation.Validatable;

/**
 * A class that provide options to a generator
 */
public interface Options extends Validatable {

    default String getSchemaToolsVersion() {
        return Version.getVersion();
    }
}
