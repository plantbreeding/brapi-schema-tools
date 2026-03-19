package org.brapi.schematools.core.model;

import java.util.List;

/**
 * Interface implemented by types that are output data modules, but are not
 * simple scalar types or an array
 */
public interface BrAPIClass extends BrAPIType {

    /**
     * Indicates whether this class is deprecated
     * @return {{@code true} if this class is deprecated, {@code false} otherwise
     */
    boolean isDeprecated();

    /**
     * The class description
     * @return The class description
     */
    String getDescription();

    /**
     * Gets the module to which this class belongs
     * @return  the module to which this class belongs
     */
    String getModule();

    /**
     * Gets the metadata associated with this class
     * @return the metadata associated with this class
     */
    BrAPIMetadata getMetadata();

    /**
     * Gets the examples associated with this class
     * @return the examples associated with this class
     */
    List<Object> getExamples();
}
