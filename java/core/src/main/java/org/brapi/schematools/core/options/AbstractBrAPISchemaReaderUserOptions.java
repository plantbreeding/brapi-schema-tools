package org.brapi.schematools.core.options;

import org.brapi.schematools.core.brapischema.BrAPISchemaReaderOptions;
import org.brapi.schematools.core.validiation.Validation;

public class AbstractBrAPISchemaReaderUserOptions implements Options {

    private BrAPISchemaReaderOptions brAPISchemaReader ;

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractBrAPISchemaReaderUserOptions overrideOptions) {
        if (overrideOptions.brAPISchemaReader != null) {
            brAPISchemaReader.override(overrideOptions.brAPISchemaReader);
        }
    }

    /**
     * Checks if the current options are valid, return a list of errors if the options are not valid
     *
     * @return a Validation object than can be used queried to find if the options are valid and any errors
     * if the options are not valid
     */
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(brAPISchemaReader, "'brAPISchemaReader' options on %s is null", this.getClass().getSimpleName()) ;
    }
}
