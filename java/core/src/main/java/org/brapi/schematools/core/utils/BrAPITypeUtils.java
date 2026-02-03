package org.brapi.schematools.core.utils;

import org.brapi.schematools.core.model.BrAPIArrayType;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIMetadata;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.response.Response;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

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

    /**
     * Validates the BrAPI Metadata of a BrAPI Class
     * @param brAPIClass the BrAPI Class to be validated
     * @return A Response containing the BrAPI Metadata if valid, or an error if invalid
     */
    public static Response<BrAPIMetadata> validateBrAPIMetadata(BrAPIClass brAPIClass) {
        BrAPIMetadata metadata = brAPIClass.getMetadata();

        LinkedList<String> flags = new LinkedList<>();

        if (metadata != null) {
            if (metadata.isPrimaryModel()) {
                flags.add("primaryModel");
            }
            if (metadata.isRequest()) {
                flags.add("request");
            }
            if (metadata.isParameters()) {
                flags.add("parameters");
            }
            if (flags.size() > 1) {
                return fail(Response.ErrorType.VALIDATION,
                    String.format("In class '%s', '%s' are mutually exclusive, only one can be set to to 'true'", brAPIClass.getName(), String.join("', '", flags)));
            }
        }

        return success(metadata);
    }

    /**
     * Merges two metadata objects, taking into account mutually exclusive flags
     *
     * @param first the first metadata object
     * @param second the second metadata object
     * @return the merged metadata object
     */
    public static BrAPIMetadata mergeMetadata(BrAPIMetadata first, BrAPIMetadata second) {
        if (first != null && second != null) {
            return BrAPIMetadata.builder()
                .primaryModel(first.isPrimaryModel() || second.isPrimaryModel())
                .request(!first.isPrimaryModel() && (first.isRequest() || second.isRequest()))
                .parameters(!first.isPrimaryModel() && !first.isRequest() && (first.isParameters() || second.isParameters()))
                .interfaceClass(!first.isPrimaryModel() && (first.isInterfaceClass() || second.isInterfaceClass()))
                .controlledVocabularyProperties(mergePropertyNames(first.getControlledVocabularyProperties(), second.getControlledVocabularyProperties()))
                .subQueryProperties(mergePropertyNames(first.getSubQueryProperties(), second.getSubQueryProperties()))
                .updatableProperties(mergePropertyNames(first.getUpdatableProperties(), second.getUpdatableProperties()))
                .writableProperties(mergePropertyNames(first.getWritableProperties(), second.getWritableProperties()))
                .build() ;
        } else if (first != null) {
            return first ;
        } else {
            return second;
        }
    }

    /**
     * Merges two lists of property names, removing duplicates
     *
     * @param first the first list of property names
     * @param second the second list of property names
     * @return the merged list of property names
     */
    public static List<String> mergePropertyNames(List<String> first, List<String> second) {
        if (first != null && second != null) {
            Set<String> set = new LinkedHashSet<>();
            set.addAll(first);
            set.addAll(second);
            return new ArrayList<>(set);
        } else if (first != null) {
            return first ;
        } else {
            return second;
        }
    }
}
