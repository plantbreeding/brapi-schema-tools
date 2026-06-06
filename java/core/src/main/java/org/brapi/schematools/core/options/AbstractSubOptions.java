package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for all sub-options (e.g. endpoint-level options within a generator).
 * Extends {@link AbstractGeneratorOptions} with a {@code summaryFormat} field.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractSubOptions extends AbstractGeneratorOptions {

    private String summaryFormat;
    private Boolean addNotFoundResponse;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> addNotFoundResponseForSingleFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(summaryFormat, "'summaryFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(addNotFoundResponse, "'addNotFoundResponse' option on %s is null", this.getClass().getSimpleName());
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);
        addNotFoundResponseForSingleFor.keySet().forEach(name ->
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'addNotFoundResponseForSingleFor' on %s",
                    name, this.getClass().getSimpleName())));
        return validation;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractSubOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.summaryFormat != null) {
            setSummaryFormat(overrideOptions.summaryFormat);
        }

        if (overrideOptions.addNotFoundResponse != null) {
            setAddNotFoundResponse(overrideOptions.addNotFoundResponse);
        }

        if (overrideOptions.addNotFoundResponseForSingleFor != null) {
            overrideOptions.addNotFoundResponseForSingleFor.forEach((key, value) -> {
                if (value == null) addNotFoundResponseForSingleFor.remove(key);
                else addNotFoundResponseForSingleFor.put(key, value);
            });
        }
    }

    /**
     * Determines if a 404 Not Found response should be added for the single endpoint of a specific
     * primary model. For non-update sub-options (GET, DELETE, Search, etc.) this controls the only
     * endpoint generated for that type; for PUT/POST, the multiple/bulk endpoint can be controlled
     * separately via {@link AbstractUpdateSubOptions#isAddingNotFoundResponseForMultipleFor(String)}.
     * Falls back to the {@code addNotFoundResponse} boolean default if not explicitly set.
     *
     * @param name the name of the primary model
     * @return {@code true} if 404 should be added on the single endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingNotFoundResponseForSingleFor(@NonNull String name) {
        Boolean value = addNotFoundResponseForSingleFor.get(name);
        return value != null ? value : addNotFoundResponse;
    }

    /**
     * Determines if a 404 Not Found response should be added for the single endpoint of a specific
     * primary model.
     *
     * @param type the primary model
     * @return {@code true} if 404 should be added on the single endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingNotFoundResponseForSingleFor(@NonNull BrAPIType type) {
        return isAddingNotFoundResponseForSingleFor(type.getName());
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull String name) {
        return String.format(summaryFormat, name);
    }

    /**
     * Gets the summary for a specific primary model.
     *
     * @param type the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type) {
        return getSummaryFor(type.getName());
    }
}

