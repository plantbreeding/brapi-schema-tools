package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of List Get Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends AbstractOpenAPISubOptions {
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedFor = new HashMap<>();
    private Boolean pagedTokenDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedToken = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();
    private Boolean propertiesFromRequest ;
    private Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedFor, "'pagedFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedTokenDefault, "'pagedTokenDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedToken, "'pagedToken' option on %s is null", this.getClass().getSimpleName())
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

        pagedFor.putAll(overrideOptions.pagedFor);

        if (overrideOptions.pagedTokenDefault != null) {
            setPagedDefault(overrideOptions.pagedTokenDefault);
        }

        pagedToken.putAll(overrideOptions.pagedToken);

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

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        pagedFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsPrimaryModel(name),
                String.format("Invalid Primary Model name '%s' set for 'pagedFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        inputFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsPrimaryModel(name),
                String.format("Invalid Primary Model name '%s' set for 'inputFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        propertyFromRequestFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid Primary Model name '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            BrAPIClass requestClass = brAPIClassCache.getBrAPIRequestClass(name);
            validation.assertNotNull(brAPIClassCache.containsPrimaryModel(name),
                String.format("Can not find Request class for '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            if (requestClass instanceof BrAPIObjectType brAPIObjectType) {
                propertyFromRequestFor.get(name).keySet().forEach(propertyName -> {
                    validation.assertTrue(brAPIObjectType.getProperties().stream().anyMatch(property -> propertyName.equals(property.getName())),
                        String.format("Invalid Property name '%s' for BrAPIObjectType '%s' set for 'propertyFromRequestFor' on %s. Possible properties are: '%s'",
                            propertyName,
                            requestClass.getName(),
                            this.getClass().getSimpleName(),
                            String.join(", ",
                                brAPIObjectType.getProperties().stream().map(BrAPIObjectProperty::getName).toList())));
                }) ;
            }
        }) ;

        return validation ;
    }

    /**
     * Determines if the List Endpoint is pagedFor for any primary model. Returns {@code true} if
     * {@link ListGetOptions#pagedFor} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param name the name of the primary model
     * @return {@code true} if the List Endpoint is pagedFor for any primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(String name) {
        return pagedFor.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint is pagedFor for any primary model. Returns {@code true} if
     * {@link ListGetOptions#pagedFor} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param type the primary model
     * @return {@code true} if the List Endpoint is pagedFor for any primary model, {@code false} otherwise
     */
    public final boolean isPagedFor(BrAPIType type) {
        return isPagedFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint is pagedFor for a specific primary model.
     * @param name the name of the primary model
     * @param paging {@code true} if the Endpoint is pagedFor for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListGetOptions setPagingFor(String name, boolean paging) {
        pagedFor.put(name, paging) ;

        return this ;
    }

    /**
     * Sets if the Endpoint is pagedFor for a specific primary model.
     * @param type the primary model
     * @param paging {@code true} if the Endpoint is pagedFor for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListGetOptions setPagingFor(BrAPIType type, boolean paging) {
        return setPagingFor(type.getName(), paging) ;
    }

    /**
     * Determines if the List Endpoint use a page token for a model. Returns {@code true} if
     * {@link ListGetOptions#pagedToken} is set to {@code true} for any type or uses {@link ListGetOptions#pagedTokenDefault}
     * @param name the name of the model
     * @return {@code true} if the List Endpoint is pagedFor for any model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasPageTokenFor(String name) {
        return pagedToken.getOrDefault(name, pagedTokenDefault) ;
    }

    /**
     * Determines if the List Endpoint use a page token for a model. Returns {@code true} if
     * {@link ListGetOptions#pagedToken} is set to {@code true} for any type or uses {@link ListGetOptions#pagedTokenDefault}
     * @param type the model
     * @return {@code true} if the List Endpoint is pagedFor for any primary model, {@code false} otherwise
     */
    public final boolean hasPageTokenFor(BrAPIType type) {
        return hasPageTokenFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint use a page token for a model
     * @param name the name of the model
     * @param hasPageToken {@code true} if the Endpoint use a page token for a model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListGetOptions setHasPageTokenFor(String name, boolean hasPageToken) {
        pagedToken.put(name, hasPageToken) ;

        return this ;
    }

    /**
     * Sets if the Endpoint use a page token for a model
     * @param type the model
     * @param hasPageToken {@code true} if the Endpoint use a page token for a model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListGetOptions setHasPageTokenFor(BrAPIType type, boolean hasPageToken) {
        return setHasPageTokenFor(type.getName(), hasPageToken) ;
    }


    /**
     * Determines if the List Endpoint is has an input for any primary model. Returns {@code true} if
     * {@link ListGetOptions#inputFor} is set to {@code true} for the primary model
     * @param name the name of the primary model
     * @return {@code true} if the List Endpoint has an input for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasInputFor(String name) {
        return inputFor.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the List Endpoint has an input for any primary model. Returns {@code true} if
     * {@link ListGetOptions#inputFor} is set to {@code true} for the primary model
     * @param type the primary model
     * @return {@code true} if the List Endpoint has an input for the primary model, {@code false} otherwise
     */
    public final boolean hasInputFor(BrAPIType type) {
        return hasInputFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint has an input for a specific primary model.
     * @param name the name of the primary model
     * @param generate {@code true} if the Endpoint has an input for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ListGetOptions setInputFor(String name, boolean generate) {
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
    public final ListGetOptions setInputFor(BrAPIType type, boolean generate) {
        return setInputFor(type.getName(), generate) ;
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @return <code>true</code> if the property from the Request is used in the List query
     */
    public final boolean isUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return isUsingPropertyFromRequestFor(type.getName(), property.getName()) ;
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
            return map.getOrDefault(propertyName, propertiesFromRequest) ;
        }

        return propertiesFromRequest ;
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @param propertiesFromRequest <code>true</code> if the property from the Request is used in the List query
     * @return the options for chaining
     */
    public final ListGetOptions setUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property, Boolean propertiesFromRequest) {

        Map<String, Boolean> map = propertyFromRequestFor.get(type.getName()) ;

        if (map != null) {
            map.put(property.getName(), propertiesFromRequest) ;
            return this ;
        } else {
            map = new HashMap<>() ;
            map.put(property.getName(), propertiesFromRequest) ;
            propertyFromRequestFor.put(type.getName(), map) ;

            return this ;
        }
    }
}
