package org.brapi.schematools.core.valdiation;

public interface Validatable {
    /**
     * Checks if the Validatable object is valid, return a list of errors if it is not valid
     *
     * @return a Validation object than can be used queried to find if the object is valid and any errors
     * if it is not valid
     */
    default Validation validate() {
        return Validation.valid() ;
    }
}
