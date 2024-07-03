package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.options.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Ids
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdsOptions implements Options {
    private String nameFormat;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> parameterFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(nameFormat, "'nameFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(parameterFor, "'parameterFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Gets the id parameter name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default. Use {@link #setIDParameterFor} to override this value.
     * @param name the name of the primary model
     * @return id parameter name for a specific primary model
     */
    @JsonIgnore
    public String getIDParameterFor(String name) {
        return parameterFor.getOrDefault(name, String.format(nameFormat, toParameterCase(name))) ;
    }

    /**
     * Gets the id parameter name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default. Use {@link #setIDParameterFor} to override this value.
     * @param type the primary model
     * @return id parameter name for a specific primary model
     */
    public String getIDParameterFor(BrAPIType type) {
        return getIDParameterFor(type.getName()) ;
    }

    /**
     * Sets the id parameter name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default.
     * @param name the name of the primary model
     * @param idParameter the id parameter name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public IdsOptions setIDParameterFor(String name, String idParameter) {
        parameterFor.put(name, idParameter) ;

        return this ;
    }

    /**
     * Sets the id parameter name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default.
     * @param type the primary model
     * @param idParameter the id parameter name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public IdsOptions setIDParameterFor(BrAPIType type, String idParameter) {
        return setIDParameterFor(type.getName(), idParameter) ;
    }
}
