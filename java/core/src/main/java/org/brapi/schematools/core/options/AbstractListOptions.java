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
 * Provides options for the generation of anything that returns a list of entities
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class AbstractListOptions extends AbstractSubOptions {
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    private Boolean propertiesFromRequest;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertiesFromRequest, "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor, "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractListOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pagedDefault != null) {
            setPagedDefault(overrideOptions.pagedDefault);
        }

        overrideOptions.paged.forEach((key, value) -> {
            if (value == null) paged.remove(key);
            else paged.put(key, value);
        });

        if (overrideOptions.propertiesFromRequest != null) {
            setPropertiesFromRequest(overrideOptions.propertiesFromRequest);
        }

        if (overrideOptions.propertyFromRequestFor != null) {
            overrideOptions.propertyFromRequestFor.forEach((key, value) -> {
                if (value == null) {
                    propertyFromRequestFor.remove(key);
                } else if (propertyFromRequestFor.containsKey(key)) {
                    value.forEach((innerKey, innerValue) -> {
                        if (innerValue == null) propertyFromRequestFor.get(key).remove(innerKey);
                        else propertyFromRequestFor.get(key).put(innerKey, innerValue);
                    });
                    if (propertyFromRequestFor.get(key).isEmpty()) propertyFromRequestFor.remove(key);
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
        Boolean value = paged.get(name);
        return value != null ? value : pagedDefault;
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
    public AbstractListOptions setPagingFor(@NonNull String name, boolean paging) {
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
    public AbstractListOptions setPagingFor(@NonNull BrAPIType type, boolean paging) {
        return setPagingFor(type.getName(), paging);
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param typeName The BrAPI Object type name
     * @param propertyName The BrAPI property name
     * @return <code>true</code> if the property from the Request is used in the List query
     */
    public final boolean isUsingPropertyFromRequestFor(String typeName, String propertyName) {

        Map<String, Boolean> map = propertyFromRequestFor.get(typeName) ;

        if (map != null) {
            Boolean value = map.get(propertyName);
            return value != null ? value : propertiesFromRequest ;
        }

        return propertiesFromRequest ;
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
            Boolean value = map.get(property.getName());
            return value != null ? value : propertiesFromRequest;
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
    public AbstractListOptions setUsingPropertyFromRequestFor(@NonNull BrAPIObjectType type,
                                                         @NonNull BrAPIObjectProperty property,
                                                         boolean usePropertyFromRequest) {
        propertyFromRequestFor
            .computeIfAbsent(type.getName(), k -> new HashMap<>())
            .put(property.getName(), usePropertyFromRequest);
        return this;
    }

}
