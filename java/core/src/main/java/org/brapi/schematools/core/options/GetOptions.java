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
 * Provides options for the generation of List GET Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class GetOptions extends AbstractListOptions {

    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> inputFor = new HashMap<>();
    private Boolean pagedTokenDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedToken = new HashMap<>();
    private Boolean list;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> listFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(inputFor, "'inputFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedTokenDefault, "'pagedTokenDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedToken, "'pagedToken' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(list, "'list' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(listFor, "'listFor' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(GetOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.inputFor != null) {
            overrideOptions.inputFor.forEach((key, value) -> {
                if (value == null) inputFor.remove(key);
                else inputFor.put(key, value);
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

        if (overrideOptions.list != null) {
            setPagedTokenDefault(overrideOptions.list);
        }
        if (overrideOptions.listFor != null) {
            overrideOptions.listFor.forEach((key, value) -> {
                if (value == null) listFor.remove(key);
                else listFor.put(key, value);
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        inputFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'inputFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        pagedToken.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'pagedToken' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        listFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'listFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation ;
    }

    /**
     * Determines if the List Endpoint accepts filter input for a specific primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(@NonNull String name) {
        Boolean value = inputFor.get(name);
        return value != null ? value : true;
    }

    /**
     * Determines if the List Endpoint accepts filter input for a specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if the endpoint accepts filter input, {@code false} otherwise
     */
    @JsonIgnore
    public boolean hasInputFor(@NonNull BrAPIType type) {
        return hasInputFor(type.getName());
    }

    /**
     * Sets whether the Endpoint accepts filter input for a specific primary model.
     *
     * @param name     the name of the primary model
     * @param hasInput {@code true} if the Endpoint accepts filter input, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public GetOptions setInputFor(@NonNull String name, boolean hasInput) {
        inputFor.put(name, hasInput);
        return this;
    }

    /**
     * Sets whether the Endpoint accepts filter input for a specific primary model.
     *
     * @param type     the primary model
     * @param hasInput {@code true} if the Endpoint accepts filter input, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public GetOptions setInputFor(@NonNull BrAPIType type, boolean hasInput) {
        return setInputFor(type.getName(), hasInput);
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
    public final GetOptions setHasPageTokenFor(@NonNull String name, boolean hasPageToken) {
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
    public final GetOptions setHasPageTokenFor(@NonNull BrAPIType type, boolean hasPageToken) {
        return setHasPageTokenFor(type.getName(), hasPageToken);
    }

    /**
     * Determines if the get endpoint returns a list for the primary model
     * @param name the name of the primary model
     * @return {@code true} if the get endpoint returns a list for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isReturningListFor(@NonNull String name) {
        Boolean value = listFor.get(name);
        return value != null ? value : list;
    }

    /**
     * Determines if the get endpoint returns a list for the primary model.
     * @param type the primary model
     * @return {@code true} if the get endpoint returns a list for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isReturningListFor(@NonNull BrAPIType type) {
        return isReturningListFor(type.getName());
    }

    /**
     * Sets if the get endpoint returns a list for the primary model
     * @param name       the name of the primary model
     * @param returnsListFor {@code true} if the get endpoint returns a list for the primary model
     * @return this
     */
    @JsonIgnore
    public final GetOptions setReturnsListFor(@NonNull String name, boolean returnsListFor) {
        listFor.put(name, returnsListFor);
        return this;
    }

    /**
     * Sets if the get endpoint returns a list for the primary model
     * @param type         the primary model
     * @param returnsListFor {@code true} if the get endpoint returns a list for the primary model
     * @return this
     */
    @JsonIgnore
    public final GetOptions setReturnsListFor(@NonNull BrAPIType type, boolean returnsListFor) {
        return setReturnsListFor(type.getName(), returnsListFor);
    }
}
