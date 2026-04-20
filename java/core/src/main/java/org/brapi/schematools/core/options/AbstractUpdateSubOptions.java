package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of PUT/POST (update) Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class AbstractUpdateSubOptions extends AbstractSubOptions {
    private Boolean multiple;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> multipleFor = new HashMap<>();

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractUpdateSubOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.multiple != null) {
            setMultiple(overrideOptions.multiple);
        }

        if (overrideOptions.multipleFor != null) {
            multipleFor.putAll(overrideOptions.multipleFor);
        }
    }

    /**
     * Determines if the PUT/POST Endpoint/method accepts multiple entities for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the PUT/POST accepts multiple entities, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull String name) {
        return multipleFor.getOrDefault(name, multiple);
    }

    /**
     * Determines if the PUT/POST Endpoint/method accepts multiple entities for a specific model.
     *
     * @param type the primary model
     * @return {@code true} if the PUT/POST accepts multiple entities, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull BrAPIType type) {
        return isMultipleFor(type.getName());
    }

    /**
     * Sets if the PUT/POST Endpoint/method accepts multiple entities for a specific model.
     *
     * @param name     the name of the primary model
     * @param multiple {@code true} if the PUT/POST accepts multiple entities, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractUpdateSubOptions setMultipleFor(@NonNull String name, boolean multiple) {
        multipleFor.put(name, multiple);
        return this;
    }

    /**
     * Sets if the PUT/POST Endpoint/method accepts multiple entities for a specific model.
     *
     * @param type     the primary model
     * @param multiple {@code true} if the PUT/POST accepts multiple entities, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractUpdateSubOptions setMultipleFor(@NonNull BrAPIType type, boolean multiple) {
        return setMultipleFor(type.getName(), multiple);
    }

    /**
     * Determines if generating a PUT/POST endpoint/method with no ID parameter (bulk update) for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if generating a bulk-update endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointFor(@NonNull String name) {
        return isGeneratingFor(name) && isMultipleFor(name);
    }

    /**
     * Determines if generating a PUT/POST endpoint/method with no ID parameter (bulk update) for a specific model.
     *
     * @param type the primary model
     * @return {@code true} if generating a bulk-update endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointFor(@NonNull BrAPIObjectType type) {
        return isGeneratingEndpointFor(type.getName());
    }

    /**
     * Determines if generating a PUT/POST endpoint/method with an ID parameter (single update) for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if generating a single-update endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull String name) {
        return isGeneratingFor(name) && !isMultipleFor(name);
    }

    /**
     * Determines if generating a PUT/POST endpoint/method with an ID parameter (single update) for a specific model.
     *
     * @param type the primary mode
     * @return {@code true} if generating a single-update endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull BrAPIObjectType type) {
        return isGeneratingEndpointNameWithIdFor(type.getName());
    }

}
