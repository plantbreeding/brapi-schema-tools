package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of Controlled Vocabulary Endpoints
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
     * Checks if the current options are valid, return a list of errors if the options are not valid
     *
     * @return a Validation object than can be used queried to find if the options are valid and any errors
     * if the options are not valid
     */
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(generate, "'generate' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(generateFor, "'generateFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(summaryFormat, "'summaryFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
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
            setSummaryFormat(overrideOptions.summaryFormat) ;
        }

        if (overrideOptions.generateFor != null) {
            overrideOptions.generateFor.forEach((key, value) -> {
                if (generateFor.containsKey(key)) {
                    generateFor.get(key).putAll(value) ;
                } else {
                    generateFor.put(key, new HashMap<>(value)) ;
                }
            });
        }
    }

    /**
     * Determines if the Endpoint/Query/Mutation is generated for any primary model. Returns {@code true} if
     * {@link ControlledVocabularyOptions#generate} is set to {@code true} or
     * {@link ControlledVocabularyOptions#generateFor} is set to {@code true} for any type
     * @return {@code true} if the Generator should generate any Endpoints/Queries/Mutations, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGenerating() {
        return generate != null && generate || generateFor.values().stream().anyMatch(value -> value.containsValue(Boolean.TRUE));
    }

    /**
     * Determines if the Endpoint is generated for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @return {@code true} if the Endpoint specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        Map<String, Boolean> map = generateFor.get(type.getName()) ;

        if (map != null) {
            return map.getOrDefault(property.getName(), generate) ;
        }

        return generate ;
    }

    /**
     * Determines if the Endpoint is generated for a specific BrAPI Property
     * @param typeWithProperty the primary model with the property
     * @return {@code true} if the Endpoint specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGeneratingFor(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return isGeneratingFor(typeWithProperty.getType(), typeWithProperty.getProperty()) ;
    }

    /**
     * Sets if the Endpoint is generated for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @param generate {@code true} if the Endpoint/Query/Mutation is generated for a specific BrAPI Property, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ControlledVocabularyOptions setGenerateFor(String typeName, String propertyName, boolean generate) {
        Map<String, Boolean> map = generateFor.get(typeName) ;

        if (map != null) {
            map.put(propertyName, generate) ;
            return this ;
        } else {
            map = new HashMap<>() ;
            map.put(propertyName, generate) ;
            generateFor.put(typeName, map) ;

            return this ;
        }
    }

    /**
     * Sets if the Endpoint/Query/Mutation is generated for a specific primary model.
     * @param type the primary model
     * @param property the property
     * @param generate {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public ControlledVocabularyOptions setGenerateFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, boolean generate) {
        return setGenerateFor(type.getName(), property.getName(), generate) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull String typeName, @NonNull String propertyName) {
        return String.format(descriptionFormat, typeName, propertyName) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param type the primary model
     * @param property the property
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getDescriptionFor(type.getName(), property.getName());
    }

    /**
     * Gets the summary for a specific primary model
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull String typeName, @NonNull String propertyName) {
        return String.format(summaryFormat, typeName, propertyName) ;
    }

    /**
     * Gets the summary for a specific primary model
     * @param type the primary model
     * @return the summary for a specific primary model
     */
    @JsonIgnore
    public final String getSummaryFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getSummaryFor(type.getName(), property.getName());
    }
}

