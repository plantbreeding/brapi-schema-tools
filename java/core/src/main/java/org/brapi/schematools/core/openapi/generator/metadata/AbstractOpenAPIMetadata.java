package org.brapi.schematools.core.openapi.generator.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.model.BrAPIType;

import java.util.HashMap;
import java.util.Map;

/**
 * Base class for OpenAPI metadata
 */
public class AbstractOpenAPIMetadata implements Metadata {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    Map<String, String> summaries = new HashMap<>() ;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    Map<String, String> descriptions = new HashMap<>() ;

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideMetadata the options which will be used to override this Options Object
     */
    public void override(AbstractOpenAPIMetadata overrideMetadata) {
        if (overrideMetadata.summaries != null) {
            summaries.putAll(overrideMetadata.summaries);
        }

        if (overrideMetadata.descriptions != null) {
            descriptions.putAll(overrideMetadata.descriptions);
        }
    }

    /**
     * Gets the summary for a specific primary model
     * @param name the name of the primary model
     * @param defaultValue default value used if model is not found in map {@link #summaries}
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryOrDefault(@NonNull String name, String defaultValue) {
        return summaries.getOrDefault(name, defaultValue) ;
    }

    /**
     * Gets the summary for a specific primary model
     * @param type the primary model
     * @param defaultValue default value used if model is not found in map {@link #summaries}
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryOrDefault(@NonNull BrAPIType type, String defaultValue) {
        return summaries.getOrDefault(type.getName(), defaultValue) ;
    }

    /**
     * Sets the summary for a specific primary model.
     * @param name the name of the primary model
     * @param description the summary for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOpenAPIMetadata setSummaryFor(String name, String description) {
        summaries.put(name, description) ;

        return this ;
    }

    /**
     * Gets the description for a specific primary model
     * @param name the name of the primary model
     * @param defaultValue default value used if model is not found in map {@link #descriptions}
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionOrDefault(@NonNull String name, String defaultValue) {
        return descriptions.getOrDefault(name, defaultValue) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param type the primary model
     * @param defaultValue default value used if model is not found in map {@link #descriptions}
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionOrDefault(@NonNull BrAPIType type, String defaultValue) {
        return getDescriptionOrDefault(type.getName(), defaultValue) ;
    }

    /**
     * Sets the description for a specific primary model.
     * @param name the name of the primary model
     * @param description the description for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOpenAPIMetadata setDescriptionFor(String name, String description) {
        descriptions.put(name, description) ;

        return this ;
    }
}
