package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;

import java.util.HashMap;
import java.util.Map;


/**
 * Provides general options
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractOptions implements Options {
    private boolean generate;
    private String descriptionFormat;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> generateFor = new HashMap<>();

    /**
     * Checks if the current options are valid, return a list of errors if the options are not valid
     *
     * @return a Validation object than can be used queried to find if the options are valid and any errors
     * if the options are not valid
     */
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(generateFor, "'generateFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Determines if the Endpoint/Query/Mutation is generated for any primary model. Returns <code>true</code> if
     * {@link AbstractOptions#generate} is set to <code>true</code> or
     * {@link AbstractOptions#generateFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Endpoints/Queries/Mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isGenerating() {
        return generate || generateFor.values().stream().anyMatch(value -> value);
    }

    /**
     * Determines if the Endpoint/Query/Mutation is generated for a specific primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the Endpoint/Query/Mutation is generated for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String name) {
        return generateFor.getOrDefault(name, generate) ;
    }

    /**
     * Determines if the Endpoint/Query/Mutation is generated for a specific primary model
     * @param type the primary model
     * @return <code>true</code> if the Endpoint/Query/Mutation is generated for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type) {
        return isGeneratingFor(type.getName()) ;
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
     * Sets if the Endpoint/Query/Mutation is generated for a specific primary model.
     * @param name the name of the primary model
     * @param generate <code>true</code> if the Endpoint/Query/Mutation is generated for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setGenerateFor(String name, boolean generate) {
        generateFor.put(name, generate) ;

        return this ;
    }

    /**
     * Sets if the Endpoint/Query/Mutation is generated for a specific primary model.
     * @param type the primary model
     * @param generate <code>true</code> if the Endpoint/Query/Mutation is generated for a specific primary model, <code>false</code>
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractOptions setGenerateFor(BrAPIType type, boolean generate) {
        return setGenerateFor(type.getName(), generate) ;
    }

}

