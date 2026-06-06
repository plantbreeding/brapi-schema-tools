package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of Table query methods
 * (GET &lt;entity&gt;/table endpoints that return CSV text).
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class TableOptions extends AbstractListOptions {
    @Getter(AccessLevel.PUBLIC)
    private String pathFormat;

    private Map<String, Map<String, String>> propertyTypeOverrideFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pathFormat, "'pathFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyTypeOverrideFor, "'propertyTypeOverrideFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        propertyTypeOverrideFor.keySet().forEach(name ->
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'propertyTypeOverrideFor' on %s",
                    name, this.getClass().getSimpleName()))) ;

        return validation ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(TableOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pathFormat != null) {
            setSummaryFormat(overrideOptions.pathFormat);
        }

        if (overrideOptions.propertyTypeOverrideFor != null) {
            overrideOptions.propertyTypeOverrideFor.forEach((key, value) -> {
                if (value == null) {
                    propertyTypeOverrideFor.remove(key);
                } else if (propertyTypeOverrideFor.containsKey(key)) {
                    value.forEach((innerKey, innerValue) -> {
                        if (innerValue == null) propertyTypeOverrideFor.get(key).remove(innerKey);
                        else propertyTypeOverrideFor.get(key).put(innerKey, innerValue);
                    });
                    if (propertyTypeOverrideFor.get(key).isEmpty()) propertyTypeOverrideFor.remove(key);
                } else {
                    propertyTypeOverrideFor.put(key, new HashMap<>(value));
                }
            });
        }
    }

    /**
     * Gets the primitive type name override for a request property on the table endpoint of a primary model,
     * or {@code null} if no override is configured. Supported values are {@code "string"}, {@code "integer"},
     * {@code "number"} and {@code "boolean"}. When set, the table endpoint exposes the named property as a
     * query parameter with the overridden primitive schema instead of the property's declared (possibly
     * complex) type.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the request property
     * @return the primitive type name to use, or {@code null} if no override
     */
    @JsonIgnore
    public String getPropertyTypeOverrideFor(String typeName, String propertyName) {
        Map<String, String> map = propertyTypeOverrideFor.get(typeName);
        return map != null ? map.get(propertyName) : null;
    }

    /**
     * Gets the primitive type name override for a request property on the table endpoint of a primary model,
     * or {@code null} if no override is configured.
     *
     * @param type     the primary model
     * @param property the request property
     * @return the primitive type name to use, or {@code null} if no override
     */
    @JsonIgnore
    public String getPropertyTypeOverrideFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getPropertyTypeOverrideFor(type.getName(), property.getName());
    }
}
