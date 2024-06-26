package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Search Post and Get Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class SearchOptions  extends AbstractOpenAPIOptions {
    private String submitDescriptionFormat;
    private String retrieveDescriptionFormat;

    public void validate() {
        super.validate();
        assert submitDescriptionFormat != null : String.format("'submitDescriptionFormat' option on %s is null", this.getClass().getSimpleName());
        assert retrieveDescriptionFormat != null : String.format("'retrieveDescriptionFormat' option on %s is null", this.getClass().getSimpleName());
    }


    /**
     * Gets the submit description for a specific primary model
     * @param type the primary model
     * @return the submit description for a specific primary model
     */
    @JsonIgnore
    public final String getSubmitDescriptionFormat(BrAPIType type) {
        return String.format(submitDescriptionFormat, type.getName(), toParameterCase(type.getName())) ;
    }

    /**
     * Gets the retrieve description for a specific primary model
     * @param type the primary model
     * @return the retrieve description for a specific primary model
     */
    @JsonIgnore
    public final String getRetrieveDescriptionFormat(BrAPIType type) {
        return String.format(retrieveDescriptionFormat, type.getName(), toParameterCase(type.getName())) ;
    }
}