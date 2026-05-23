package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
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
    private Boolean pagedTokenDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedToken = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertiesFromRequest, "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor, "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedTokenDefault, "'pagedTokenDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedToken, "'pagedToken' option on %s is null", this.getClass().getSimpleName()) ;
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

        if (overrideOptions.pagedTokenDefault != null) {
            setPagedTokenDefault(overrideOptions.pagedTokenDefault);
        }

        if (overrideOptions.pagedToken != null) {
            overrideOptions.pagedToken.forEach((key, value) -> {
                if (value == null) pagedToken.remove(key);
                else pagedToken.put(key, value);
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        paged.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'paged' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        propertyFromRequestFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        pagedToken.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'pagedToken' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation ;
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

    /**
     * Determines if the get endpoint has a page token for the named primary model.
     * @param name the name of the primary model
     * @return {@code true} if the get endpoint has a page token, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasPageTokenFor(@NonNull String name) {
        Boolean value = pagedToken.get(name);
        return value != null ? value : pagedTokenDefault;
    }

    /**
     * Determines if the get endpoint has a page token for the given primary model.
     * @param type the primary model
     * @return {@code true} if the get endpoint has a page token, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasPageTokenFor(@NonNull BrAPIType type) {
        return hasPageTokenFor(type.getName());
    }

    /**
     * Sets the page token flag for the named primary model.
     * @param name       the name of the primary model
     * @param hasPageToken {@code true} if the get endpoint has a page token
     * @return this
     */
    @JsonIgnore
    public final AbstractListOptions setHasPageTokenFor(@NonNull String name, boolean hasPageToken) {
        pagedToken.put(name, hasPageToken);
        return this;
    }

    /**
     * Sets the page token flag for the given primary model.
     * @param type         the primary model
     * @param hasPageToken {@code true} if the get endpoint has a page token
     * @return this
     */
    @JsonIgnore
    public final AbstractListOptions setHasPageTokenFor(@NonNull BrAPIType type, boolean hasPageToken) {
        return setHasPageTokenFor(type.getName(), hasPageToken);
    }
}
