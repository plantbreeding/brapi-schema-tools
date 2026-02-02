package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;

/**
 * Abstract class for all Generator Options
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractGeneratorOptions implements Options {
    private Boolean generate;
    private String descriptionFormat;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> generateFor = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pluralFor = new HashMap<>();

    /**
     * Checks if the current options are valid, return a list of errors if the options are not valid
     *
     * @return a Validation object than can be used queried to find if the options are valid and any errors
     * if the options are not valid
     */
    @Override
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(generate, "'generate' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(generateFor, "'generateFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pluralFor, "'pluralFor' option on %s is null", this.getClass().getSimpleName()) ;
    }
    
    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractGeneratorOptions overrideOptions) {

        if (overrideOptions.generate != null) {
            setGenerate(overrideOptions.generate);
        }

        if (overrideOptions.descriptionFormat != null) {
            setDescriptionFormat(overrideOptions.descriptionFormat);
        }

        generateFor.putAll(overrideOptions.generateFor);
    
        if (overrideOptions.pluralFor != null) {
            pluralFor.putAll(overrideOptions.pluralFor);
        }
    }

    /**
     * Determines if the Endpoint/Query/Mutation/File is generated for any primary model. Returns {@code true} if
     * {@link #generate} is set to {@code true} or
     * {@link #generateFor} is set to {@code true} for any type
     * @return {@code true} if the Generator should generate any Output, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGenerating() {
        return generate != null && generate || generateFor.values().stream().anyMatch(value -> value);
    }

    /**
     * Determines if the Endpoint/Query/Mutation/File is generated for a specific primary model
     * @param name the name of the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGeneratingFor(@NonNull String name) {
        return generateFor.getOrDefault(name, generate) ;
    }

    /**
     * Determines if the Endpoint/Query/Mutation/File is generated for a specific primary model
     * @param type the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGeneratingFor(@NonNull BrAPIType type) {
        return isGeneratingFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint/Query/Mutation/File is generated for a specific primary model.
     * @param name the name of the primary model
     * @param generate {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractGeneratorOptions setGenerateFor(String name, boolean generate) {
        generateFor.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint/Query/Mutation/File is generated for a specific primary model.
     * @param type the primary model
     * @param generate {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractGeneratorOptions setGenerateFor(BrAPIType type, boolean generate) {
        return setGenerateFor(type.getName(), generate) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param name the name of the primary model
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull String name) {
        return String.format(descriptionFormat, name) ;
    }

    /**
     * Gets the description for a specific primary model
     * @param type the primary model
     * @return the description for a specific primary model
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull BrAPIType type) {
        return getDescriptionFor(type.getName());
    }
    
    /**
     * Gets the Pluralised name for a specific Primary Model. For example plural
     * name of Study, would be 'Studies' by default. Use {@link #setPluralFor} to override this value.
     * @param name the name of the Primary Model
     * @return the pluralised name for a specific Primary Model
     */
    @JsonIgnore
    public final String getPluralFor(@NonNull String name) {
        return pluralFor.getOrDefault(name, toPlural(name)) ;
    }

    /**
     * Gets the pluralised name for a specific Primary Model. For example plural
     * name of Study, would be 'Studies' by default. Use {@link #setPluralFor} to override this value.
     * @param type the Primary Model
     * @return the pluralised name for a specific Primary Model
     */
    @JsonIgnore
    public final String getPluralFor(@NonNull BrAPIType type) {
        return getPluralFor(type.getName()) ;
    }

    /**
     * Sets the pluralised name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default.
     * @param name the name of the primary model
     * @param pluralisedName the pluralised name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractGeneratorOptions setPluralFor(String name, String pluralisedName) {
        pluralFor.put(name, pluralisedName) ;

        return this ;
    }
}
