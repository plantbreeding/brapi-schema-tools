package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of controlled vocabulary endpoints.
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
     * Determines if any controlled vocabulary endpoint is being generated.
     *
     * @return {@code true} if generating for any primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGenerating() {
        return generate != null && generate ||
            generateFor.values().stream().flatMap(m -> m.values().stream()).anyMatch(v -> v);
    }

    /**
     * Determines if a controlled vocabulary endpoint is generated for a specific type and property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @return {@code true} if a controlled vocabulary endpoint should be generated
     */
    @JsonIgnore
    public boolean isGeneratingFor(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, Boolean> map = generateFor.get(typeName);
        if (map != null) {
            return map.getOrDefault(propertyName, generate);
        }
        return generate != null && generate;
    }

    /**
     * Determines if a controlled vocabulary endpoint is generated for a specific type and property.
     *
     * @param type         the primary model
     * @param propertyName the name of the property
     * @return {@code true} if a controlled vocabulary endpoint should be generated
     */
    @JsonIgnore
    public boolean isGeneratingFor(@NonNull BrAPIType type, @NonNull String propertyName) {
        return isGeneratingFor(type.getName(), propertyName);
    }
}
