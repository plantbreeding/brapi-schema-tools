package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.ValidatableAgainstCache;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;

/**
 * Provides options for the generation of Controlled Vocabulary
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class ControlledVocabularyOptions implements Options, ValidatableAgainstCache {
    private Boolean generate;
    private String summaryFormat;
    private String descriptionFormat;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, Boolean>> generateFor = new HashMap<>();
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, Boolean>> pagedFor = new HashMap<>();

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
            .assertNotNull(pagedFor, "'pagedFor' option on %s is null", this.getClass().getSimpleName())
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

        if (overrideOptions.pagedFor != null) {
            overrideOptions.pagedFor.forEach((key, value) -> {
                if (pagedFor.containsKey(key)) {
                    pagedFor.get(key).putAll(value) ;
                } else {
                    pagedFor.put(key, new HashMap<>(value)) ;
                }
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = Validation.valid() ;

        generateFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsPrimaryModel(name),
                String.format("Invalid Primary Model name '%s' set for 'generateFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            BrAPIClass brAPIClass = brAPIClassCache.getBrAPIClass(name);

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                generateFor.get(name).keySet().forEach(propertyName -> {
                    validation.assertTrue(brAPIObjectType.getProperties().stream().anyMatch(property -> propertyName.equals(property.getName())),
                        String.format("Invalid Property name '%s' for BrAPIObjectType '%s' set for 'generateFor' on %s. Possible properties are: %s",
                            propertyName,
                            name,
                            this.getClass().getSimpleName(),
                            String.join(", ",
                                brAPIObjectType.getProperties().stream().map(BrAPIObjectProperty::getName).toList())));
                }) ;
            }
        }) ;

        pagedFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsPrimaryModel(name),
                String.format("Invalid Primary Model name '%s' set for 'pagedFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            BrAPIClass brAPIClass = brAPIClassCache.getBrAPIClass(name);

            if (brAPIClass instanceof BrAPIObjectType brAPIObjectType) {
                pagedFor.get(name).keySet().forEach(propertyName -> {
                    validation.assertTrue(brAPIObjectType.getProperties().stream().anyMatch(property -> propertyName.equals(property.getName())),
                        String.format("Invalid Property name '%s' for BrAPIObjectType '%s' set for 'pagedFor' on %s. Possible properties are: %s",
                            propertyName,
                            name,
                            this.getClass().getSimpleName(),
                            String.join(", ",
                                brAPIObjectType.getProperties().stream().map(BrAPIObjectProperty::getName).toList())));
                }) ;
            }
        }) ;

        return validation ;
    }

    /**
     * Determines if the Endpoint is generated for any primary model. Returns {@code true} if
     * {@link ControlledVocabularyOptions#generate} is set to {@code true} or
     * {@link ControlledVocabularyOptions#generateFor} is set to {@code true} for any type
     * @return {@code true} if the Generator should generate the Endpoint, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGenerating() {
        return generate != null && generate || generateFor.values().stream().anyMatch(value -> value.containsValue(Boolean.TRUE));
    }

    /**
     * Determines if the Endpoint is generated for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return {@code true} if the Endpoint is generated for a specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, Boolean> map = generateFor.get(typeName) ;

        if (map != null) {
            return map.getOrDefault(propertyName, generate) ;
        }

        return generate ;
    }

    /**
     * Determines if the Endpoint is generated for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @return {@code true} if the Endpoint is generated for the specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return isGeneratingFor(type.getName(), property.getName()) ;
    }

    /**
     * Determines if the Endpoint is generated for a specific BrAPI Property
     * @param typeWithProperty the primary model with the property
     * @return {@code true} if the Endpoint is generated for the specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return isGeneratingFor(typeWithProperty.getType(), typeWithProperty.getProperty()) ;
    }

    /**
     * Sets if the Endpoint is generated for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @param generate {@code true} if the Endpoint is generated for the specific BrAPI Property, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setGenerateFor(String typeName, String propertyName, boolean generate) {
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
     * Sets if the Endpoint is generated for a specific primary model.
     * @param type the primary model
     * @param property the property
     * @param generate {@code true} if the Endpoint is generated for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setPagingFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, boolean generate) {
        return setGenerateFor(type.getName(), property.getName(), generate) ;
    }

    /**
     * Determines if the paging is used for the Endpoint that is generated for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return {@code true} if the Endpoint specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, Boolean> map = pagedFor.get(typeName) ;

        if (map != null) {
            return map.getOrDefault(propertyName, pagedDefault) ;
        }

        return pagedDefault ;
    }

    /**
     * Determines if the paging is used for the Endpoint that is generated for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @return {@code true} if the paging is used for the Endpoint that is generated for the specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return isPagedFor(type.getName(), property.getName()) ;
    }

    /**
     * Determines if the paging is used for the Endpoint is generated for a specific BrAPI Property
     * @param typeWithProperty the primary model with the property
     * @return {@code true} if the paging is used for the Endpoint that is generated for the specific BrAPI Property, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return isPagedFor(typeWithProperty.getType(), typeWithProperty.getProperty()) ;
    }

    /**
     * Sets if the paging is used for the Endpoint is generated for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @param generate {@code true} if the paging is used for the Endpoint that is generated for a specific BrAPI Property, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setPageFor(String typeName, String propertyName, boolean generate) {
        Map<String, Boolean> map = pagedFor.get(typeName) ;

        if (map != null) {
            map.put(propertyName, generate) ;
            return this ;
        } else {
            map = new HashMap<>() ;
            map.put(propertyName, generate) ;
            pagedFor.put(typeName, map) ;

            return this ;
        }
    }

    /**
     * Sets if the paging is used for the Endpoint is generated for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @param generate {@code true} if the paging is used for the Endpoint is generated for a specific BrAPI Property, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final ControlledVocabularyOptions setPageFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, boolean generate) {
        return setPageFor(type.getName(), property.getName(), generate) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull String typeName, @NonNull String propertyName) {
        return StringUtils.format(descriptionFormat, Map.of("type", typeName, "property", toPlural(propertyName))) ;
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
        return StringUtils.format(summaryFormat, Map.of("type", typeName, "property", toPlural(propertyName))) ;
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

