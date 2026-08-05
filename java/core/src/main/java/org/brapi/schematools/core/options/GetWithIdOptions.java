package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of Get Endpoints/methods with ID.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class GetWithIdOptions extends AbstractSubOptions {

    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(GetWithIdOptions overrideOptions) {
        super.override(overrideOptions);

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
     * Determines if a specific request property should be included as a query parameter
     * on the GET /{id} endpoint for a given model.
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
            return value != null && value;
        }
        return false;
    }
}
