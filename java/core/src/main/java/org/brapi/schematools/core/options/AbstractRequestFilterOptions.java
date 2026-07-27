package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for filtering which properties from a Request class are included
 * when generating a request schema (e.g. search or table-search request bodies).
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public abstract class AbstractRequestFilterOptions extends AbstractSubOptions {
    private Boolean propertiesFromRequest;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(propertiesFromRequest, "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor, "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractRequestFilterOptions overrideOptions) {
        super.override(overrideOptions);

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

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        propertyFromRequestFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation ;
    }

    /**
     * Gets whether a property from the Request is used in the generated request schema.
     * @param typeName The BrAPI Object type name
     * @param propertyName The BrAPI property name
     * @return {@code true} if the property from the Request is used
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
     * Determines if a specific request property should be included for a given model.
     *
     * @param type     the primary model
     * @param property the request property
     * @return {@code true} if the property should be included
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
     * Sets whether a specific request property should be included.
     *
     * @param type                   the primary model
     * @param property               the request property
     * @param usePropertyFromRequest {@code true} if the property should be included
     * @return this
     */
    @JsonIgnore
    public AbstractRequestFilterOptions setUsingPropertyFromRequestFor(@NonNull BrAPIObjectType type,
                                                                       @NonNull BrAPIObjectProperty property,
                                                                       boolean usePropertyFromRequest) {
        propertyFromRequestFor
            .computeIfAbsent(type.getName(), k -> new HashMap<>())
            .put(property.getName(), usePropertyFromRequest);
        return this;
    }
}
