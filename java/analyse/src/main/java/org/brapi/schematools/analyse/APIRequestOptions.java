package org.brapi.schematools.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Provides API Request options for the analysis of endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class APIRequestOptions implements Options {
    private Boolean analyse;
    @Getter(AccessLevel.PRIVATE)
    private Map<String, Boolean> analyseFor = new HashMap<>();
    @Getter(AccessLevel.PRIVATE)
    private Map<String, List<String>> requiredParametersFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(analyse, "'analyse' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(analyseFor, "'analyseFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(requiredParametersFor, "'requiredParametersFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(APIRequestOptions overrideOptions) {
        if (overrideOptions.analyse != null) {
            setAnalyse(overrideOptions.analyse); ;
        }

        if (overrideOptions.analyseFor != null) {
            analyseFor.putAll(overrideOptions.analyseFor);
        }

        if (overrideOptions.requiredParametersFor != null) {
            requiredParametersFor.putAll(overrideOptions.requiredParametersFor);
        }
    }

    /**
     * Gets if to analysis for a specific primary mode. Use {@link #setAnalysingEntityFor(String, Boolean)}
     * to override this value.
     * @param name the name of the primary model
     * @return property name for a specific primary model
     */
    @JsonIgnore
    public Boolean isAnalysingEntity(String name) {
        return analyseFor.getOrDefault(name, analyse) ;
    }

    /**
     * Sets if to do analysis for a specific primary model.
     * @param name the name of the primary model
     * @param analyse {@code true} if to do analyse for this model, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public APIRequestOptions setAnalysingEntityFor(String name, Boolean analyse) {
        analyseFor.put(name, analyse) ;

        return this ;
    }

    /**
     * Gets the required parameters a specific primary model. Use {@link #setAnalysingEntityFor(String, Boolean)}
     * to override this value.
     * @param name the name of the primary model
     * @return the required parameters a specific primary model
     */
    @JsonIgnore
    public List<String> getRequiredParametersFor(String name) {
        return requiredParametersFor.getOrDefault(name, new LinkedList<>()) ;
    }

    /**
     * Sets the required parameters a specific primary model.
     * @param name the name of the primary model
     * @param requiredParameters the required parameters a specific primary model
     * @return the options for chaining
     */
    @JsonIgnore
    public APIRequestOptions setRequiredParametersFor(String name, List<String> requiredParameters) {
        requiredParametersFor.put(name, requiredParameters) ;

        return this ;
    }
}
