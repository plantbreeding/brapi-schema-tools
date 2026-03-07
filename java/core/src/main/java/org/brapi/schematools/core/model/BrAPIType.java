package org.brapi.schematools.core.model;

/**
 * Base class for all BrAPI Types
 */
public interface BrAPIType extends Comparable<BrAPIType>  {
    /**
     * Gets the name of the BrAPI Type
     * @return the name of the BrAPI Type
     */
    String getName();

    @Override
    default int compareTo(BrAPIType otherType) {
        return otherType.getName().compareTo(this.getName());
    }
}
