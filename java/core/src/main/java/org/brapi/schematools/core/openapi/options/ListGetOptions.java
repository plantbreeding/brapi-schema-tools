package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractOptions;
import org.brapi.schematools.core.options.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of List Get Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends AbstractOpenAPIOptions {
    private boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(inputFor,  "'inputFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Determines if the List Endpoint is paged for any primary model. Returns <code>true</code> if
     * {@link ListGetOptions#paged} is set to <code>true</code> for any type or uses {@link ListGetOptions#pagedDefault}
     * @param name the name of the primary model
     * @return <code>true</code> if the List Endpoint is paged for any primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isPagedFor(String name) {
        return paged.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint is paged for any primary model. Returns <code>true</code> if
     * {@link ListGetOptions#paged} is set to <code>true</code> for any type or uses {@link ListGetOptions#pagedDefault}
     * @param type the primary model
     * @return <code>true</code> if the List Endpoint is paged for any primary model, <code>false</code> otherwise
     */
    public boolean isPagedFor(BrAPIType type) {
        return isPagedFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint is paged for a specific primary model.
     * @param name the name of the primary model
     * @param generate <code>true</code> if the Endpoint is paged for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setPagingFor(String name, boolean generate) {
        paged.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint is paged for a specific primary model.
     * @param type the primary model
     * @param generate <code>true</code> if the Endpoint is paged for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setPagingFor(BrAPIType type, boolean generate) {
        return setPagingFor(type.getName(), generate) ;
    }

    /**
     * Determines if the List Endpoint is has an input for any primary model. Returns <code>true</code> if
     * {@link ListGetOptions#inputFor} is set to <code>true</code> for the primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the List Endpoint has an input for the primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(String name) {
        return inputFor.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint has an input for any primary model. Returns <code>true</code> if
     * {@link ListGetOptions#inputFor} is set to <code>true</code> for the primary model
     * @param type the primary model
     * @return <code>true</code> if the List Endpoint has an input for the primary model, <code>false</code> otherwise
     */
    public boolean hasInputFor(BrAPIType type) {
        return hasInputFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint has an input for a specific primary model.
     * @param name the name of the primary model
     * @param generate <code>true</code> if the Endpoint has an input for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setInputFor(String name, boolean generate) {
        inputFor.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint has an input for a specific primary model.
     * @param type the primary model
     * @param generate <code>true</code> if the Endpoint has an input for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setInputFor(BrAPIType type, boolean generate) {
        return setInputFor(type.getName(), generate) ;
    }
}
