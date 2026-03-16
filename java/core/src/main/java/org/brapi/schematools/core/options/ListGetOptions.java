package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of List GET Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends AbstractSubOptions {

    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();
    private Boolean propertiesFromRequest;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(inputFor, "'inputFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertiesFromRequest, "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor, "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(ListGetOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pagedDefault != null) {
            setPagedDefault(overrideOptions.pagedDefault);
        }

        paged.putAll(overrideOptions.paged);
        inputFor.putAll(overrideOptions.inputFor);

        if (overrideOptions.propertiesFromRequest != null) {
            setPropertiesFromRequest(overrideOptions.propertiesFromRequest);
        }

        if (overrideOptions.propertyFromRequestFor != null) {
            overrideOptions.propertyFromRequestFor.forEach((key, value) -> {
                if (propertyFromRequestFor.containsKey(key)) {
                    propertyFromRequestFor.get(key).putAll(value);
                } else {
                    propertyFromRequestFor.put(key, new HashMap<>(value));
                }
            });
        }
    }

    /**
     * Determines if the List Endpoint is paged for a specific primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if paged, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(@NonNull String name) {
        return paged.getOrDefault(name, pagedDefault);
    }

    /**
     * Determines if the List Endpoint is paged for a specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if paged, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(@NonNull BrAPIType type) {
        return isPagedFor(type.getName());
    }

    /**
     * Sets paging for a specific primary model.
     *
     * @param name   the name of the primary model
     * @param paging {@code true} if the Endpoint should be paged, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setPagingFor(@NonNull String name, boolean paging) {
        paged.put(name, paging);
        return this;
    }

    /**
     * Sets paging for a specific primary model.
     *
     * @param type   the primary model
     * @param paging {@code true} if the Endpoint should be paged, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setPagingFor(@NonNull BrAPIType type, boolean paging) {
        return setPagingFor(type.getName(), paging);
    }

    /**
     * Determines if the List Endpoint accepts filter input for a specific primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(@NonNull String name) {
        return inputFor.getOrDefault(name, pagedDefault);
    }

    /**
     * Determines if the List Endpoint accepts filter input for a specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(@NonNull BrAPIType type) {
        return hasInputFor(type.getName());
    }

    /**
     * Sets whether the Endpoint accepts filter input for a specific primary model.
     *
     * @param name     the name of the primary model
     * @param hasInput {@code true} if the Endpoint accepts filter input, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setInputFor(@NonNull String name, boolean hasInput) {
        inputFor.put(name, hasInput);
        return this;
    }

    /**
     * Sets whether the Endpoint accepts filter input for a specific primary model.
     *
     * @param type     the primary model
     * @param hasInput {@code true} if the Endpoint accepts filter input, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setInputFor(@NonNull BrAPIType type, boolean hasInput) {
        return setInputFor(type.getName(), hasInput);
    }

    /**
     * Determines if a specific request property should be included in the query parameters for a given model.
     *
     * @param type     the primary model
     * @param property the request property
     * @return {@code true} if the property should be exposed as a query parameter
     */
    @JsonIgnore
    public boolean isUsingPropertyFromRequestFor(@NonNull BrAPIObjectType type, @NonNull BrAPIObjectProperty property) {
        Map<String, Boolean> map = propertyFromRequestFor.get(type.getName());
        if (map != null) {
            return map.getOrDefault(property.getName(), propertiesFromRequest);
        }
        return propertiesFromRequest;
    }

    /**
     * Sets whether a specific request property should be included in the query parameters.
     *
     * @param type                  the primary model
     * @param property              the request property
     * @param usePropertyFromRequest {@code true} if the property should be exposed as a query parameter
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setUsingPropertyFromRequestFor(@NonNull BrAPIObjectType type,
                                                         @NonNull BrAPIObjectProperty property,
                                                         boolean usePropertyFromRequest) {
        propertyFromRequestFor
            .computeIfAbsent(type.getName(), k -> new HashMap<>())
            .put(property.getName(), usePropertyFromRequest);
        return this;
    }
}
