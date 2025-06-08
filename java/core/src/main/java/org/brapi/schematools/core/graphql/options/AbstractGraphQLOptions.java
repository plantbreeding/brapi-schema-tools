package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractGeneratorSubOptions;
import org.brapi.schematools.core.validiation.Validation;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;


/**
 * Provides general options for the generation of Endpoints
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractGraphQLOptions extends AbstractGeneratorSubOptions {
    private Boolean pluralisingName;
    @Getter(AccessLevel.PRIVATE)
    private String nameFormat;

    public Validation validate() {
        return super.validate()
            .assertNotNull(pluralisingName, "'pluralisingName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(nameFormat, "'analyse' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(AbstractGraphQLOptions overrideOptions) {
        super.override(overrideOptions) ;

        if (overrideOptions.pluralisingName != null) {
            setPluralisingName(overrideOptions.pluralisingName); ;
        }

        if (overrideOptions.nameFormat != null) {
            setNameFormat(overrideOptions.nameFormat); ;
        }
    }

    /**
     * Gets the name of the query or mutation for a specific primary model
     * @param name the name of the primary model
     * @return the name of the query or mutation for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull String name) {
        return String.format(nameFormat, nameFormat.startsWith("%s") ? toParameterCase(name) : name) ;
    }

    /**
     * Gets the name of query or mutation for a specific primary model
     * @param type the primary model
     * @return the name of the query or mutation for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull BrAPIType type) {
        return getNameFor(type.getName());
    }

    /**
     * Determines if the query or mutation should have a plurialised name
     * @return {@code true} if the query or mutation should have a plurialised name, <code>false </code> otherwise
     */
    public boolean isPluralisingName() {
        return pluralisingName != null && pluralisingName ;
    }
}

