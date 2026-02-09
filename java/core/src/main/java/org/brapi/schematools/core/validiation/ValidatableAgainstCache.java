package org.brapi.schematools.core.validiation;

import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;

/**
 * Interface that markers a class that can be against the BrAPI Schema Cache.
 */
public interface ValidatableAgainstCache {

    /**
     * Checks if the Validatable object is valid, return a list of errors if it is not valid
     *
     * @return a Validation object than can be used queried to find if the object is valid and any errors
     * if it is not valid
     */
    default Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        return Validation.valid() ;
    }
}

