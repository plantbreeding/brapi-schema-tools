package org.brapi.schematools.core.options;

/**
 * An class that provider options to a generator
 */
public interface Options {

    /**
     * Checks if the current options are valid, return a list of errors if the options are not valid
     *
     * @return a Validation object than can be used queried to find if the options are valid and any errors
     * if the options are not valid
     */
    default Validation validate() {
        return Validation.valid() ;
    }
}
