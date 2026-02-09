package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Search Post and Get Endpoints
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class SearchOptions extends AbstractOpenAPISubOptions {

    @Getter(AccessLevel.PUBLIC)
    private String searchIdFieldName;
    @Getter(AccessLevel.PUBLIC)
    private String searchIdFieldDescription;
    private String submitDescriptionFormat;
    private String retrieveDescriptionFormat;
    private Boolean pagedDefault;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> pagedFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(searchIdFieldName, "'searchIdFieldName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(searchIdFieldDescription, "'searchIdFieldName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(submitDescriptionFormat, "'submitDescriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(retrieveDescriptionFormat,  "'retrieveDescriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedDefault, "'pagedDefault' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pagedFor, "'pagedFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(SearchOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.searchIdFieldName != null) {
            setSearchIdFieldName(overrideOptions.searchIdFieldName);
        }

        if (overrideOptions.searchIdFieldDescription != null) {
            setSearchIdFieldName(overrideOptions.searchIdFieldDescription);
        }

        if (overrideOptions.submitDescriptionFormat != null) {
            setSubmitDescriptionFormat(overrideOptions.submitDescriptionFormat);
        }

        if (overrideOptions.retrieveDescriptionFormat != null) {
            setRetrieveDescriptionFormat(overrideOptions.retrieveDescriptionFormat);
        }

        if (overrideOptions.pagedDefault != null) {
            setPagedDefault(overrideOptions.pagedDefault);
        }

        pagedFor.putAll(overrideOptions.pagedFor);
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

    /**
     * Determines if the Search Endpoint is pagedFor for any primary model. Returns {@code true} if
     * {@link SearchOptions#pagedFor} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param name the name of the primary model
     * @return {@code true} if the List Endpoint is pagedFor for any primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isPagedFor(String name) {
        return pagedFor.getOrDefault(name, pagedDefault) ;
    }

    /**
     * Determines if the Search Endpoint is pagedFor for any primary model. Returns {@code true} if
     * {@link SearchOptions#pagedFor} is set to {@code true} for any type or uses {@link ListGetOptions#pagedDefault}
     * @param type the primary model
     * @return {@code true} if the List Endpoint is pagedFor for any primary model, {@code false} otherwise
     */
    public final boolean isPagedFor(BrAPIType type) {
        return isPagedFor(type.getName()) ;
    }

    /**
     * Sets if the Endpoint is pagedFor for a specific primary model.
     * @param name the name of the primary model
     * @param paging {@code true} if the Endpoint is pagedFor for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final SearchOptions setPagingFor(String name, boolean paging) {
        pagedFor.put(name, paging) ;

        return this ;
    }

    /**
     * Sets if the Endpoint is pagedFor for a specific primary model.
     * @param type the primary model
     * @param paging {@code true} if the Endpoint is pagedFor for a specific primary model, {@code false}
     * @return the options for chaining
     */
    @JsonIgnore
    public final SearchOptions setPagingFor(BrAPIType type, boolean paging) {
        return setPagingFor(type.getName(), paging) ;
    }

}