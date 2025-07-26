package org.brapi.schematools.core.openapi.generator.options;

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
 * Provides options for the generation of List Get Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends AbstractOpenAPIOptions {
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();
    private Boolean propertiesFromRequest ;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(inputFor,  "'inputFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertiesFromRequest,  "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor,  "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
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
                    propertyFromRequestFor.get(key).putAll(value) ;
                } else {
                    propertyFromRequestFor.put(key, new HashMap<>(value)) ;
                }
            });
        }
    }

    /**
     * Determines if the List Endpoint is paged for any primary model. Returns {@code true} if
     * {@link ListGetOptions#paged} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param name the name of the primary model
     * @return {@code true} if the List Endpoint is paged for any primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(String name) {
        return paged.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint is paged for any primary model. Returns {@code true} if
     * {@link ListGetOptions#paged} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param type the primary model
     * @return {@code true} if the List Endpoint is paged for any primary model, {@code false} otherwise
     */
    public boolean isPagedFor(BrAPIType type) {
        return isPagedFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint is paged for a specific primary model.
     * @param name the name of the primary model
     * @param generate {@code true} if the Endpoint is paged for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setPagingFor(String name, boolean generate) {
        paged.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint is paged for a specific primary model.
     * @param type the primary model
     * @param generate {@code true} if the Endpoint is paged for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setPagingFor(BrAPIType type, boolean generate) {
        return setPagingFor(type.getName(), generate) ;
    }

    /**
     * Determines if the List Endpoint is has an input for any primary model. Returns {@code true} if
     * {@link ListGetOptions#inputFor} is set to {@code true} for the primary model
     * @param name the name of the primary model
     * @return {@code true} if the List Endpoint has an input for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(String name) {
        return inputFor.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint has an input for any primary model. Returns {@code true} if
     * {@link ListGetOptions#inputFor} is set to {@code true} for the primary model
     * @param type the primary model
     * @return {@code true} if the List Endpoint has an input for the primary model, {@code false} otherwise
     */
    public boolean hasInputFor(BrAPIType type) {
        return hasInputFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint has an input for a specific primary model.
     * @param name the name of the primary model
     * @param generate {@code true} if the Endpoint has an input for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setInputFor(String name, boolean generate) {
        inputFor.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint has an input for a specific primary model.
     * @param type the primary model
     * @param generate {@code true} if the Endpoint has an input for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ListGetOptions setInputFor(BrAPIType type, boolean generate) {
        return setInputFor(type.getName(), generate) ;
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @return <code>true</code> if the property from the Request is used in the List query
     */
    public boolean isUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property) {

        Map<String, Boolean> map = propertyFromRequestFor.get(type.getName()) ;

        if (map != null) {
            return map.getOrDefault(property.getName(), propertiesFromRequest) ;
        }

        return propertiesFromRequest ;
    }
}
