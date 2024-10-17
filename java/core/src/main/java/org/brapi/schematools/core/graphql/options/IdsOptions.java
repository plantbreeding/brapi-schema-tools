package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.valdiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Ids
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdsOptions implements Options {
    @Getter(AccessLevel.PRIVATE)
    String nameFormat;
    @JsonProperty("useIDType")
    boolean usingIDType;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> fieldFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(nameFormat, "'nameFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(fieldFor, "'fieldFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Gets the name of the id for a specific primary model
     * @param name the name of the primary model
     * @return the name of the id for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull String name) {
        return String.format(nameFormat, StringUtils.toParameterCase(name)) ;
    }

    /**
     * Gets the name of id for a specific primary model. Use {@link #setIDFieldFor} to override this value.
     * @param type the primary model
     * @return the name of the id for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull BrAPIType type) {
        return getNameFor(type.getName());
    }

    /**
     * Gets the id field name for a specific primary model. For example the id field
     * name of Study, would be 'studyDbiId' by default. Use {@link #setIDFieldFor} to override this value.
     * @param name the name of the primary model
     * @return id parameter name for a specific primary model
     */
    @JsonIgnore
    public String getIDFieldFor(String name) {
        return fieldFor.getOrDefault(name, String.format(nameFormat, toParameterCase(name))) ;
    }

    /**
     * Sets the id field name for a specific primary model. For example the id field
     * name of Study, would be 'studyDbiId' by default.
     * @param name the name of the primary model
     * @param idField the id field name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public IdsOptions setIDFieldFor(String name, String idField) {
        fieldFor.put(name, idField) ;

        return this ;
    }
}
