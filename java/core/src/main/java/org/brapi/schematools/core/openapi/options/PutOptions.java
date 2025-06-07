package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of Put Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class PutOptions extends AbstractOpenAPIOptions {

    private Boolean multiple;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> multipleFor = new HashMap<>();

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(PutOptions overrideOptions) {
        super.override(overrideOptions) ;

        if (overrideOptions.multiple != null) {
            setMultiple(overrideOptions.multiple); ;
        }

        if (overrideOptions.multipleFor != null) {
            multipleFor.putAll(overrideOptions.multipleFor); ;
        }
    }

    /**
     * Determines if the PUT Endpoint accepts multiple entities for a specific model
     * @param name the name of the primary model
     * @return {@code true} if the PUT Endpoint accepts multiple entities for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull String name) {
        return multipleFor.getOrDefault(name, multiple) ;
    }

    /**
     * Determines if the PUT Endpoint accepts multiple entities for a specific model
     * @param type the primary model
     * @return {@code true} if the PUT Endpoint accepts multiple entities for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull BrAPIType type) {
        return isMultipleFor(type.getName()) ;
    }

    /**
     * Sets if the PUT Endpoint accepts multiple entities for a specific model
     * @param name the name of the primary model
     * @param multiple set to {@code true} if the PUT Endpoint accepts multiple entities for a specific model
     *                 {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public PutOptions setMultipleFor(String name, boolean multiple) {
        multipleFor.put(name, multiple) ;

        return this ;
    }

    /**
     * Sets if the PUT Endpoint accepts multiple entities for a specific model
     * @param type the primary model
     * @param multiple set to {@code true} if the PUT Endpoint accepts multiple entities for a specific model
     *                 {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public PutOptions setMultipleFor(BrAPIType type, boolean multiple) {
        return setMultipleFor(type.getName(), multiple) ;
    }

    /**
     * Determines if generating a PUT endpoint with no ID parameter for a specific model
     * @param name the name of the primary model
     * @return {@code true} if generating a PUT endpoint with no ID parameter for a specific model, {@code false} otherwise
     */
    public boolean isGeneratingEndpointFor(String name) {
        return isGeneratingFor(name) && isMultipleFor(name) ;
    }

    /**
     * Determines if generating a PUT endpoint with no ID parameter for a specific model
     * @param type the primary model
     * @return {@code true} if generating a PUT endpoint with no ID parameter for a specific model, {@code false} otherwise
     */
    public boolean isGeneratingEndpointFor(BrAPIObjectType type) {
        return isGeneratingEndpointFor(type.getName()) ;
    }

    /**
     * Determines if generating a PUT endpoint with an ID parameter for a specific model
     * @param name the name of the primary model
     * @return {@code true} if generating a PUT endpoint with an ID parameter for a specific model, {@code false} otherwise
     */
    public boolean isGeneratingEndpointNameWithIdFor(String name) {
        return isGeneratingFor(name) && !isMultipleFor(name) ;
    }

    /**
     * Determines if generating a PUT endpoint with an ID parameter for a specific model
     * @param type the primary model
     * @return {@code true} if generating a PUT endpoint with an ID parameter for a specific model, {@code false} otherwise
     */
    public boolean isGeneratingEndpointNameWithIdFor(BrAPIObjectType type) {
        return isGeneratingEndpointNameWithIdFor(type.getName()) ;
    }
}
