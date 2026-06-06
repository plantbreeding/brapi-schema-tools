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
 * Provides options for the generation of DELETE Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class DeleteOptions extends AbstractSubOptions {
    @Getter(AccessLevel.PUBLIC)
    private String bulkPathFormat;
    private String bulkSummaryFormat;
    private String bulkDescriptionFormat;
    private String bulkResponseNameFormat;
    private String bulkResultFieldNameFormat;
    private String bulkResultFieldDescriptionFormat;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> bulkGenerateFor = new HashMap<>();

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(DeleteOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.bulkPathFormat != null) {
            setBulkPathFormat(overrideOptions.bulkPathFormat);
        }
        if (overrideOptions.bulkSummaryFormat != null) {
            setBulkSummaryFormat(overrideOptions.bulkSummaryFormat);
        }
        if (overrideOptions.bulkDescriptionFormat != null) {
            setBulkDescriptionFormat(overrideOptions.bulkDescriptionFormat);
        }
        if (overrideOptions.bulkResponseNameFormat != null) {
            setBulkResponseNameFormat(overrideOptions.bulkResponseNameFormat);
        }
        if (overrideOptions.bulkResultFieldNameFormat != null) {
            setBulkResultFieldNameFormat(overrideOptions.bulkResultFieldNameFormat);
        }
        if (overrideOptions.bulkResultFieldDescriptionFormat != null) {
            setBulkResultFieldDescriptionFormat(overrideOptions.bulkResultFieldDescriptionFormat);
        }

        if (overrideOptions.bulkGenerateFor != null) {
            overrideOptions.bulkGenerateFor.forEach((key, value) -> {
                if (value == null) bulkGenerateFor.remove(key);
                else bulkGenerateFor.put(key, value);
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        bulkGenerateFor.keySet().forEach(name ->
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'bulkGenerateFor' on %s",
                    name, this.getClass().getSimpleName())));

        return validation;
    }

    /**
     * Determines if a bulk (search-request-based) DELETE endpoint should be generated for a specific model.
     *
     * @param name the name of the primary model
     * @return {@code true} if a bulk delete endpoint should be generated, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isBulkGeneratingFor(@NonNull String name) {
        Boolean value = bulkGenerateFor.get(name);
        return value != null && value;
    }

    /**
     * Determines if a bulk (search-request-based) DELETE endpoint should be generated for a specific model.
     *
     * @param type the primary model
     * @return {@code true} if a bulk delete endpoint should be generated, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isBulkGeneratingFor(@NonNull BrAPIType type) {
        return isBulkGeneratingFor(type.getName());
    }

    /**
     * Sets whether a bulk DELETE endpoint should be generated for a specific model.
     *
     * @param name     the name of the primary model
     * @param generate {@code true} to enable bulk delete generation, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final DeleteOptions setBulkGeneratingFor(@NonNull String name, boolean generate) {
        bulkGenerateFor.put(name, generate);
        return this;
    }

    /**
     * Gets the bulk delete summary for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the bulk delete summary
     */
    @JsonIgnore
    public final String getBulkSummaryFor(@NonNull String name) {
        return String.format(bulkSummaryFormat, name);
    }

    /**
     * Gets the bulk delete summary for a specific primary model.
     *
     * @param type the primary model
     * @return the bulk delete summary
     */
    @JsonIgnore
    public final String getBulkSummaryFor(@NonNull BrAPIType type) {
        return getBulkSummaryFor(type.getName());
    }

    /**
     * Gets the bulk delete description for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the bulk delete description
     */
    @JsonIgnore
    public final String getBulkDescriptionFor(@NonNull String name) {
        return String.format(bulkDescriptionFormat, name);
    }

    /**
     * Gets the bulk delete description for a specific primary model.
     *
     * @param type the primary model
     * @return the bulk delete description
     */
    @JsonIgnore
    public final String getBulkDescriptionFor(@NonNull BrAPIType type) {
        return getBulkDescriptionFor(type.getName());
    }

    /**
     * Gets the bulk delete response schema name for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the bulk delete response schema name (e.g. {@code ImageDeleteResponse})
     */
    @JsonIgnore
    public final String getBulkResponseNameFor(@NonNull String name) {
        return String.format(bulkResponseNameFormat, name);
    }

    /**
     * Gets the bulk delete response schema name for a specific primary model.
     *
     * @param type the primary model
     * @return the bulk delete response schema name
     */
    @JsonIgnore
    public final String getBulkResponseNameFor(@NonNull BrAPIType type) {
        return getBulkResponseNameFor(type.getName());
    }

    /**
     * Gets the bulk delete result field name (i.e. the array field inside the {@code result} object that holds the
     * DbIds of the deleted entities) for a specific id property name.
     *
     * @param idPropertyName the id property name (e.g. {@code imageDbId})
     * @return the bulk delete result field name (e.g. {@code imageDbIds})
     */
    @JsonIgnore
    public final String getBulkResultFieldNameFor(@NonNull String idPropertyName) {
        return String.format(bulkResultFieldNameFormat, idPropertyName);
    }

    /**
     * Gets the bulk delete result field description for a specific primary model.
     *
     * @param name the name of the primary model
     * @return the bulk delete result field description
     */
    @JsonIgnore
    public final String getBulkResultFieldDescriptionFor(@NonNull String name) {
        return String.format(bulkResultFieldDescriptionFormat, name);
    }

    /**
     * Gets the bulk delete result field description for a specific primary model.
     *
     * @param type the primary model
     * @return the bulk delete result field description
     */
    @JsonIgnore
    public final String getBulkResultFieldDescriptionFor(@NonNull BrAPIType type) {
        return getBulkResultFieldDescriptionFor(type.getName());
    }
}
