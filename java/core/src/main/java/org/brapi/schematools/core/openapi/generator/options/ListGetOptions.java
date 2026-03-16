package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Extends the shared {@link org.brapi.schematools.core.options.ListGetOptions} with
 * OpenAPI-specific page-token support.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ListGetOptions extends org.brapi.schematools.core.options.ListGetOptions {

    private Boolean pagedTokenDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedToken = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pagedTokenDefault, "'pagedTokenDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedToken, "'pagedToken' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     * @param overrideOptions the options which will be used to override this Options Object
     */
    @Override
    public void override(org.brapi.schematools.core.options.ListGetOptions overrideOptions) {
        super.override(overrideOptions);
        if (overrideOptions instanceof ListGetOptions openApiOverride) {
            if (openApiOverride.pagedTokenDefault != null) {
                setPagedTokenDefault(openApiOverride.pagedTokenDefault);
            }
            if (openApiOverride.pagedToken != null) {
                pagedToken.putAll(openApiOverride.pagedToken);
            }
        }
    }

    /**
     * Determines if the list endpoint has a page token for the named primary model.
     * @param name the name of the primary model
     * @return {@code true} if the list endpoint has a page token, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasPageTokenFor(@NonNull String name) {
        return pagedToken.getOrDefault(name, pagedTokenDefault);
    }

    /**
     * Determines if the list endpoint has a page token for the given primary model.
     * @param type the primary model
     * @return {@code true} if the list endpoint has a page token, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean hasPageTokenFor(@NonNull BrAPIType type) {
        return hasPageTokenFor(type.getName());
    }

    /**
     * Sets the page token flag for the named primary model.
     * @param name       the name of the primary model
     * @param hasPageToken {@code true} if the list endpoint has a page token
     * @return this
     */
    @JsonIgnore
    public final ListGetOptions setHasPageTokenFor(@NonNull String name, boolean hasPageToken) {
        pagedToken.put(name, hasPageToken);
        return this;
    }

    /**
     * Sets the page token flag for the given primary model.
     * @param type         the primary model
     * @param hasPageToken {@code true} if the list endpoint has a page token
     * @return this
     */
    @JsonIgnore
    public final ListGetOptions setHasPageTokenFor(@NonNull BrAPIType type, boolean hasPageToken) {
        return setHasPageTokenFor(type.getName(), hasPageToken);
    }
}
