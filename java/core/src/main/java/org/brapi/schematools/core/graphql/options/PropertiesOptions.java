package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIRelationshipType;
import org.brapi.schematools.core.options.AbstractPropertiesOptions;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of properties and their usage
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions extends AbstractPropertiesOptions {
    @Getter
    private IdsOptions ids;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(ids, "'ids' option on %s is null", this.getClass().getSimpleName());
    }

    @Override
    public void override(AbstractPropertiesOptions overrideOptions) {
        super.override(overrideOptions);
        if (overrideOptions instanceof PropertiesOptions gqlOverride) {
            if (gqlOverride.ids != null) {
                ids.override(gqlOverride.ids);
            }
        }
    }
}
