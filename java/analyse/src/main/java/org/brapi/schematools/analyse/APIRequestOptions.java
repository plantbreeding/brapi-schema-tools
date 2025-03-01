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

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(analyse, "'analyse' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(analyseFor, "'analyseFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(APIRequestOptions overrideOptions) {
        if (overrideOptions.analyse != null) {
            setAnalyse(overrideOptions.analyse); ;
        }

        analyseFor.putAll(overrideOptions.analyseFor);
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
     * Sets if to analysis for a specific primary model.
     * @param name the name of the primary model
     * @param analyse {@code true} if to analyse for this model, {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public APIRequestOptions setAnalysingEntityFor(String name, Boolean analyse) {
        analyseFor.put(name, analyse) ;

        return this ;
    }
}
