package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

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
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> useAdditionalProperties = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> singleAlsoFor = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> addNotFoundResponseForMultipleFor = new HashMap<>();
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
            overrideOptions.multipleFor.forEach((key, value) -> {
                if (value == null) multipleFor.remove(key);
                else multipleFor.put(key, value);
            });
        }

        if (overrideOptions.useAdditionalProperties != null) {
            overrideOptions.useAdditionalProperties.forEach((key, value) -> {
                if (value == null) useAdditionalProperties.remove(key);
                else useAdditionalProperties.put(key, value);
            });
        }

        if (overrideOptions.singleAlsoFor != null) {
            overrideOptions.singleAlsoFor.forEach((key, value) -> {
                if (value == null) singleAlsoFor.remove(key);
                else singleAlsoFor.put(key, value);
            });
        }

        if (overrideOptions.addNotFoundResponseForMultipleFor != null) {
            overrideOptions.addNotFoundResponseForMultipleFor.forEach((key, value) -> {
                if (value == null) addNotFoundResponseForMultipleFor.remove(key);
                else addNotFoundResponseForMultipleFor.put(key, value);
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        multipleFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'multipleFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        useAdditionalProperties.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'useAdditionalProperties' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        singleAlsoFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'singleAlsoFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        addNotFoundResponseForMultipleFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'addNotFoundResponseForMultipleFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation ;
    }

    /**
     * Determines if the PUT/POST Endpoint/method accepts multiple entities for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the PUT/POST accepts multiple entities, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull String name) {
        Boolean value = multipleFor.get(name);
        return value != null ? value : multiple;
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
     * Determines if the PUT/POST Endpoint/method uses additionalProperties to wrap the entity.
     *
     * @param name the name of the primary model
     * @return {@code true} if the PUT/POST uses additionalProperties to wrap the entity, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isUsingAdditionalProperties(@NonNull String name) {
        Boolean value = useAdditionalProperties.get(name);
        return value != null ? value : multiple;
    }


    /**
     * Determines if the PUT/POST Endpoint/method uses additionalProperties to wrap the entity.
     *
     * @param type the primary model
     * @return {@code true} if the PUT/POST uses additionalProperties to wrap the entity, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isUsingAdditionalProperties(@NonNull BrAPIType type) {
        return isUsingAdditionalProperties(type.getName());
    }

    /**
     * Sets if the PUT/POST Endpoint/method uses additionalProperties to wrap the entity.
     *
     * @param name     the name of the primary model
     * @param usesAdditionalProperties {@code true} if the PUT/POST uses additionalProperties to wrap the entity, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractUpdateSubOptions setUsingAdditionalProperties(@NonNull String name, boolean usesAdditionalProperties) {
        useAdditionalProperties.put(name, usesAdditionalProperties);
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
     * Determines if the PUT/POST Endpoint/method also generates the single (by-ID) endpoint alongside the
     * multiple/bulk endpoint for a specific model. Only has an effect when {@link #isMultipleFor(String)} is true.
     *
     * @param name the name of the primary model
     * @return {@code true} if the single by-ID endpoint should also be generated, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isSingleAlsoFor(@NonNull String name) {
        Boolean value = singleAlsoFor.get(name);
        return value != null ? value : false;
    }

    /**
     * Determines if the PUT/POST Endpoint/method also generates the single (by-ID) endpoint alongside the
     * multiple/bulk endpoint for a specific model.
     *
     * @param type the primary model
     * @return {@code true} if the single by-ID endpoint should also be generated, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isSingleAlsoFor(@NonNull BrAPIType type) {
        return isSingleAlsoFor(type.getName());
    }

    /**
     * Sets if the PUT/POST Endpoint/method also generates the single (by-ID) endpoint alongside the
     * multiple/bulk endpoint for a specific model.
     *
     * @param name        the name of the primary model
     * @param singleAlso  {@code true} if the single by-ID endpoint should also be generated, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractUpdateSubOptions setSingleAlsoFor(@NonNull String name, boolean singleAlso) {
        singleAlsoFor.put(name, singleAlso);
        return this;
    }

    /**
     * Determines if generating a PUT/POST endpoint/method with an ID parameter (single update) for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if generating a single-update endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull String name) {
        return isGeneratingFor(name) && (!isMultipleFor(name) || isSingleAlsoFor(name));
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

    /**
     * Determines if a 404 Not Found response should be added for the multiple/bulk PUT/POST endpoint for a
     * specific primary model. Falls back to {@link #isAddingNotFoundResponseFor(String)} if not explicitly set.
     *
     * @param name the name of the primary model
     * @return {@code true} if 404 should be added on the multiple/bulk endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingNotFoundResponseForMultipleFor(@NonNull String name) {
        Boolean value = addNotFoundResponseForMultipleFor.get(name);
        return value != null ? value : isAddingNotFoundResponseFor(name);
    }

    /**
     * Determines if a 404 Not Found response should be added for the multiple/bulk PUT/POST endpoint for a
     * specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if 404 should be added on the multiple/bulk endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingNotFoundResponseForMultipleFor(@NonNull BrAPIType type) {
        return isAddingNotFoundResponseForMultipleFor(type.getName());
    }

}
