package org.brapi.schematools.core.graphql.options;

import lombok.*;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIRelationshipType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPITypeUtils;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.valdiation.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Provides options for the generation of properties and their usage
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions implements Options {
    @Getter
    private IdsOptions ids;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, String>> linkTypeFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .merge(linkTypeFor.values().stream().flatMap(map -> map.values().stream()).map(LinkType::fromNameOrLabels).collect(Response.toList()))
            .assertNotNull(ids, "'ids' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(PropertiesOptions overrideOptions) {
        if (overrideOptions.ids != null) {
            ids.override(overrideOptions.ids) ;
        }

        if (overrideOptions.linkTypeFor != null) {
            overrideOptions.linkTypeFor.forEach((key, value) -> {
                if (linkTypeFor.containsKey(key)) {
                    linkTypeFor.get(key).putAll(value) ;
                } else {
                    linkTypeFor.put(key, new HashMap<>(value)) ;
                }
            });
        }
    }

    /**
     * Gets the link type for a type property
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @return the link type for specified type property
     */
    public LinkType getLinkTypeFor(BrAPIObjectType type, BrAPIObjectProperty property) {

        Map<String, String> map = linkTypeFor.get(type.getName()) ;

        if (map != null) {
            return LinkType.findByNameOrLabel(map.get(property.getName())).orElseGet(() -> getDefaultLinkTypeFor(type, property)) ;
        }

        return getDefaultLinkTypeFor(type, property) ;
    }

    private LinkType getDefaultLinkTypeFor(BrAPIObjectType type, BrAPIObjectProperty property) {

        BrAPIRelationshipType relationshipType = property.getRelationshipType() != null ? property.getRelationshipType() : BrAPIRelationshipType.ONE_TO_ONE;

        return switch (relationshipType) {
            case ONE_TO_ONE -> LinkType.EMBEDDED ;
            case MANY_TO_ONE -> LinkType.EMBEDDED ;
            case ONE_TO_MANY -> LinkType.EMBEDDED ;
            case MANY_TO_MANY  -> LinkType.SUB_QUERY ;
        } ;
    }
}
