package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
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
public class AbstractListOptions extends AbstractRequestFilterOptions {
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> paged = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> useSubQueryPropertiesFor = new HashMap<>();
    private Boolean pagedTokenDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedToken = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(paged, "'paged' option on %s is null", this.getClass().getSimpleName())
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

        if (overrideOptions.pagedTokenDefault != null) {
            setPagedTokenDefault(overrideOptions.pagedTokenDefault);
        }

        if (overrideOptions.pagedToken != null) {
            overrideOptions.pagedToken.forEach((key, value) -> {
                if (value == null) pagedToken.remove(key);
                else pagedToken.put(key, value);
            });
        }

        if (overrideOptions.useSubQueryPropertiesFor != null) {
            overrideOptions.useSubQueryPropertiesFor.forEach((key, value) -> {
                if (value == null) useSubQueryPropertiesFor.remove(key);
                else useSubQueryPropertiesFor.put(key, value);
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

        pagedToken.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'pagedToken' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        useSubQueryPropertiesFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'useSubQueryPropertiesFor' on %s",
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

    /**
     * Determines if the top-level list GET should expose only the request class's
     * {@code subQueryProperties} as query parameters (instead of all request properties)
     * for the named primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if only {@code subQueryProperties} should be exposed, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isUsingSubQueryPropertiesFor(@NonNull String name) {
        Boolean value = useSubQueryPropertiesFor.get(name);
        return value != null ? value : false;
    }

    /**
     * Determines if the top-level list GET should expose only the request class's
     * {@code subQueryProperties} as query parameters for the given primary model.
     *
     * @param type the primary model
     * @return {@code true} if only {@code subQueryProperties} should be exposed, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isUsingSubQueryPropertiesFor(@NonNull BrAPIType type) {
        return isUsingSubQueryPropertiesFor(type.getName());
    }
}
