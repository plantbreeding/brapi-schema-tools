package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Search POST and result-retrieval GET Endpoints/methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class SearchOptions extends AbstractListOptions {

    @Getter(AccessLevel.PUBLIC)
    private String searchIdFieldName;
    private String submitDescriptionFormat;
    private String retrieveDescriptionFormat;
    private Boolean list;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> listFor = new HashMap<>();

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(searchIdFieldName,
                "'searchIdFieldName' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(submitDescriptionFormat,
                "'submitDescriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(retrieveDescriptionFormat,
                "'retrieveDescriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(list, "'list' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(listFor, "'listFor' option on %s is null", this.getClass().getSimpleName());
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

        if (overrideOptions.list != null) {
            setList(overrideOptions.list);
        }

        if (overrideOptions.listFor != null) {
            overrideOptions.listFor.forEach((key, value) -> {
                if (value == null) listFor.remove(key);
                else listFor.put(key, value);
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        if (!brAPIClassCache.isValidating()) {
            return validation;
        }

        listFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'listFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation ;
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

    /**
     * Determines if the get endpoint returns a list for the primary model
     * @param name the name of the primary model
     * @return {@code true} if the get endpoint returns a list for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isReturningListFor(@NonNull String name) {
        Boolean value = listFor.get(name);
        return value != null ? value : list;
    }

    /**
     * Determines if the get endpoint returns a list for the primary model.
     * @param type the primary model
     * @return {@code true} if the get endpoint returns a list for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isReturningListFor(@NonNull BrAPIType type) {
        return isReturningListFor(type.getName());
    }

    /**
     * Sets if the get endpoint returns a list for the primary model
     * @param name       the name of the primary model
     * @param returnsListFor {@code true} if the get endpoint returns a list for the primary model
     * @return this
     */
    @JsonIgnore
    public final SearchOptions setReturnsListFor(@NonNull String name, boolean returnsListFor) {
        listFor.put(name, returnsListFor);
        return this;
    }

    /**
     * Sets if the get endpoint returns a list for the primary model
     * @param type         the primary model
     * @param returnsListFor {@code true} if the get endpoint returns a list for the primary model
     * @return this
     */
    @JsonIgnore
    public final SearchOptions setReturnsListFor(@NonNull BrAPIType type, boolean returnsListFor) {
        return setReturnsListFor(type.getName(), returnsListFor);
    }
}
