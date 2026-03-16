package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Search POST and result-retrieval GET Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class SearchOptions extends AbstractSubOptions {

    @Getter(AccessLevel.PUBLIC)
    private String searchIdFieldName;
    private String submitDescriptionFormat;
    private String retrieveDescriptionFormat;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(searchIdFieldName,
                "'searchIdFieldName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(submitDescriptionFormat,
                "'submitDescriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(retrieveDescriptionFormat,
                "'retrieveDescriptionFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(SearchOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.searchIdFieldName != null) {
            setSearchIdFieldName(overrideOptions.searchIdFieldName);
        }

        if (overrideOptions.submitDescriptionFormat != null) {
            setSubmitDescriptionFormat(overrideOptions.submitDescriptionFormat);
        }

        if (overrideOptions.retrieveDescriptionFormat != null) {
            setRetrieveDescriptionFormat(overrideOptions.retrieveDescriptionFormat);
        }
    }

    /**
     * Gets the submit description for a specific primary model.
     *
     * @param type the primary model
     * @return the formatted submit description string
     */
    @JsonIgnore
    public final String getSubmitDescriptionFormat(@NonNull BrAPIType type) {
        return String.format(submitDescriptionFormat, type.getName(), toParameterCase(type.getName()));
    }

    /**
     * Gets the retrieve description for a specific primary model.
     *
     * @param type the primary model
     * @return the formatted retrieve description string
     */
    @JsonIgnore
    public final String getRetrieveDescriptionFormat(@NonNull BrAPIType type) {
        return String.format(retrieveDescriptionFormat, type.getName(), toParameterCase(type.getName()));
    }
}
