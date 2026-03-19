package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.model.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;

/**
 * Provides options for the generation of Controlled Vocabulary endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ControlledVocabularyOptions implements Options {
    private Boolean generate;
    private String summaryFormat;
    private String descriptionFormat;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, Boolean>> generateFor = new HashMap<>();

    /**
     * Checks if the current options are valid.
     *
     * @return a Validation object that reports whether options are valid and any errors
     */
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(generate, "'generate' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(generateFor, "'generateFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(summaryFormat, "'summaryFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(ControlledVocabularyOptions overrideOptions) {
        if (overrideOptions.generate != null) {
            setGenerate(overrideOptions.generate);
        }
        if (overrideOptions.descriptionFormat != null) {
            setDescriptionFormat(overrideOptions.descriptionFormat);
        }
        if (overrideOptions.summaryFormat != null) {
            setSummaryFormat(overrideOptions.summaryFormat);
        }
        if (overrideOptions.generateFor != null) {
            overrideOptions.generateFor.forEach((key, value) -> {
                if (generateFor.containsKey(key)) {
                    generateFor.get(key).putAll(value);
                } else {
                    generateFor.put(key, new HashMap<>(value));
                }
            });
        }
    }

    /**
     * Determines if the endpoint/method is generated for any primary model.
     *
     * @return {@code true} if generating for any primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGenerating() {
        return generate != null && generate ||
            generateFor.values().stream().anyMatch(value -> value.containsValue(Boolean.TRUE));
    }

    /**
     * Determines if the endpoint/method is generated for a specific type and property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @return {@code true} if the endpoint/method should be generated
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, Boolean> map = generateFor.get(typeName);
        if (map != null) {
            return map.getOrDefault(propertyName, generate);
        }
        return generate;
    }

    /**
     * Determines if the endpoint/method is generated for a specific type and property.
     *
     * @param type     the primary model
     * @param property the property
     * @return {@code true} if the endpoint/method should be generated
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return isGeneratingFor(type.getName(), property.getName());
    }

    /**
     * Determines if the endpoint/method is generated for a specific type and property.
     *
     * @param type         the primary model
     * @param propertyName the name of the property
     * @return {@code true} if the endpoint/method should be generated
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type, @NonNull String propertyName) {
        return isGeneratingFor(type.getName(), propertyName);
    }

    /**
     * Determines if the endpoint/method is generated for a specific type and property.
     *
     * @param typeWithProperty the primary model with the property
     * @return {@code true} if the endpoint/method should be generated
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return isGeneratingFor(typeWithProperty.getType(), typeWithProperty.getProperty());
    }

    /**
     * Sets if the endpoint/method is generated for a specific type and property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @param generate     {@code true} if the endpoint/method should be generated
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setGenerateFor(String typeName, String propertyName, boolean generate) {
        generateFor.computeIfAbsent(typeName, k -> new HashMap<>()).put(propertyName, generate);
        return this;
    }

    /**
     * Sets if the endpoint/method is generated for a specific type and property.
     *
     * @param type     the primary model
     * @param property the property
     * @param generate {@code true} if the endpoint/method should be generated
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setGenerateFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, boolean generate) {
        return setGenerateFor(type.getName(), property.getName(), generate);
    }

    /**
     * Gets the description for a specific primary model and property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @return the formatted description
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull String typeName, @NonNull String propertyName) {
        return StringUtils.format(descriptionFormat, Map.of("type", typeName, "property", toPlural(propertyName)));
    }

    /**
     * Gets the description for a specific primary model and property.
     *
     * @param type     the primary model
     * @param property the property
     * @return the formatted description
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getDescriptionFor(type.getName(), property.getName());
    }

    /**
     * Gets the summary for a specific primary model and property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @return the formatted summary
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull String typeName, @NonNull String propertyName) {
        return StringUtils.format(summaryFormat, Map.of("type", typeName, "property", toPlural(propertyName)));
    }

    /**
     * Gets the summary for a specific primary model and property.
     *
     * @param type     the primary model
     * @param property the property
     * @return the formatted summary
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getSummaryFor(type.getName(), property.getName());
    }
}
