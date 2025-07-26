package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.model.BrAPIClass;

/**
 * Provides utility methods for BrAPI Types
 */
public class BrAPITypeUtils {
    /**
     * Determines if a BrAPI Class is a Not primary model
     * @param brAPIClass the BrAPI Class to be checked
     * @return {@code true} if the BrAPI Class is not a primary model
     */
    public static boolean isNonPrimaryModel(BrAPIClass brAPIClass) {
        return brAPIClass.getMetadata() == null || !brAPIClass.getMetadata().isPrimaryModel() ;
    }

    /**
     * Determines if a BrAPI Class is a primary model
     * @param brAPIClass the BrAPI Class to be checked
     * @return {@code true} if the BrAPI Class is a primary model
     */
    public static boolean isPrimaryModel(BrAPIClass brAPIClass) {
        return brAPIClass.getMetadata() != null && brAPIClass.getMetadata().isPrimaryModel() ;
    }
}
