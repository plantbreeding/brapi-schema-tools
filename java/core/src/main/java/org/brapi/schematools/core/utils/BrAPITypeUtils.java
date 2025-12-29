package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.model.BrAPIArrayType;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIType;

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

    /**
     * Unwraps a BrAPIType, by finding the inner type
     *
     * @param type the type to be unwrapped
     * @return the unwrapped type
     */
    public static BrAPIType unwrapType(BrAPIType type) {
        if (type instanceof BrAPIArrayType brAPIArrayType) {
            return unwrapType(brAPIArrayType.getItems());
        }

        return type;
    }
}
