package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;

public class AbstractGeneratorOptions {

    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pluralFor = new HashMap<>();

    /**
     * Gets the Pluralised name for a specific Primary Model. For example plural
     * name of Study, would be 'Studies' by default. Use {@link #setIDParameterFor} to override this value.
     * @param name the name of the Primary Model
     * @return the pluralised name for a specific Primary Model
     */
    @JsonIgnore
    public final String getPluralFor(@NonNull String name) {
        return pluralFor.getOrDefault(name, toPlural(name)) ;
    }

    /**
     * Gets the pluralised name for a specific Primary Model. For example plural
     * name of Study, would be 'Studies' by default. Use {@link #setIDParameterFor} to override this value.
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
