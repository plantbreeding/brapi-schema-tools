package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

/**
 * Provides options for the generation of Action endpoints, which are
 * collection-scoped POST endpoints declared via the {@code actionProperties}
 * field of a class' {@code brapi-metadata} block. For each action property on
 * a primary model, a {@code POST /<entity-plural>/<actionName>} endpoint is
 * generated with the property's referenced type as the request body and the
 * owning type's standard single-entity response as the response.
 * <p>
 * The {@code actionSummaryFormat} and {@code actionDescriptionFormat} accept
 * two {@code %s} placeholders — the action name and the type name (in that
 * order). The inherited {@code summaryFormat} (single placeholder) is unused
 * for action endpoints but must still be set to satisfy parent validation.
 */
@Getter
@Setter
public class ActionsOptions extends AbstractSubOptions {
    private String pathFormat;
    private String actionSummaryFormat;
    private String actionDescriptionFormat;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pathFormat, "'pathFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(actionSummaryFormat, "'actionSummaryFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(actionDescriptionFormat, "'actionDescriptionFormat' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(ActionsOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.pathFormat != null) {
            pathFormat = overrideOptions.pathFormat;
        }
        if (overrideOptions.actionSummaryFormat != null) {
            actionSummaryFormat = overrideOptions.actionSummaryFormat;
        }
        if (overrideOptions.actionDescriptionFormat != null) {
            actionDescriptionFormat = overrideOptions.actionDescriptionFormat;
        }
    }

    /**
     * Gets the path-item name for an action endpoint on a primary model.
     *
     * @param typePathItemName the path-item name of the owning primary model (e.g. {@code /variantsets})
     * @param actionName       the action property name (e.g. {@code extract})
     * @return the action path-item name (e.g. {@code /variantsets/extract})
     */
    @JsonIgnore
    public String getPathItemNameFor(@NonNull String typePathItemName, @NonNull String actionName) {
        return String.format(pathFormat, typePathItemName, actionName);
    }

    /**
     * Gets the summary for an action endpoint, given the action name and the owning primary model name.
     *
     * @param actionName the action property name (e.g. {@code extract})
     * @param typeName   the primary model name (e.g. {@code VariantSet})
     * @return the formatted summary
     */
    @JsonIgnore
    public String getActionSummaryFor(@NonNull String actionName, @NonNull String typeName) {
        return String.format(actionSummaryFormat, actionName, typeName);
    }

    /**
     * Gets the description for an action endpoint, given the action name and the owning primary model name.
     *
     * @param actionName the action property name (e.g. {@code extract})
     * @param typeName   the primary model name (e.g. {@code VariantSet})
     * @return the formatted description
     */
    @JsonIgnore
    public String getActionDescriptionFor(@NonNull String actionName, @NonNull String typeName) {
        return String.format(actionDescriptionFormat, actionName, typeName);
    }

    /**
     * Determines if the action endpoint should be generated for the given primary model.
     * Falls back to {@link #isGeneratingFor(BrAPIType)} which honours the {@code generate}
     * default and the {@code generateFor} map.
     *
     * @param type the primary model
     * @return {@code true} if action endpoints should be generated for this type
     */
    @JsonIgnore
    public boolean isGeneratingActionsFor(@NonNull BrAPIType type) {
        return isGeneratingFor(type);
    }
}
