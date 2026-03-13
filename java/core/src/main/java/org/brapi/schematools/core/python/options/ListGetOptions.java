package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of list-get query methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends AbstractPythonGeneratorSubOptions {
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();
    private Boolean propertiesFromRequest;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
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
     * Determines if the list endpoint is paged for the named primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the list endpoint is paged, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(String name) {
        return paged.getOrDefault(name, pagedDefault);
    }

    /**
     * Determines if the list endpoint is paged for the given primary model.
     *
     * @param type the primary model
     * @return {@code true} if the list endpoint is paged, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(BrAPIType type) {
        return isPagedFor(type.getName());
    }

    /**
     * Determines if the list endpoint accepts filter input for the named primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isInputFor(String name) {
        return inputFor.getOrDefault(name, true);
    }

    /**
     * Determines if the list endpoint accepts filter input for the given primary model.
     *
     * @param type the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isInputFor(BrAPIType type) {
        return isInputFor(type.getName());
    }

    /**
     * Determines if a specific request property should be included in the query parameters
     * for a given model.
     *
     * @param type     the primary model
     * @param property the request property
     * @return {@code true} if the property should be exposed as a query parameter
     */
    @JsonIgnore
    public boolean isUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        Map<String, Boolean> map = propertyFromRequestFor.get(type.getName());
        if (map != null) {
            return map.getOrDefault(property.getName(), propertiesFromRequest);
        }
        return propertiesFromRequest;
    }
}
