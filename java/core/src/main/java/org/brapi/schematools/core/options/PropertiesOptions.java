package org.brapi.schematools.core.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIRelationshipType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.LinkType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPITypeUtils;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Provides options for the generation of ID, Name and PUI property and their usage
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions implements Options {
    private PropertyOptions id ;
    private PropertyOptions name ;
    private PropertyOptions pui ;
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, String>> linkTypeFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .merge(linkTypeFor.values().stream().flatMap(map -> map.values().stream()).map(LinkType::fromNameOrLabels).collect(Response.toList()))
            .assertNotNull(id, "'id' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(PropertiesOptions overrideOptions) {
        if (overrideOptions.id != null) {
            id.override(overrideOptions.id) ;
        }

        if (overrideOptions.name != null) {
            name.override(overrideOptions.name) ;
        }

        if (overrideOptions.pui != null) {
            pui.override(overrideOptions.pui) ;
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
     * Gets the list of link properties that are used to generate links to the
     * provided object type.
     *
     * This is usually the object dbId, but can also be the name and/or PUI.
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of link properties that are used to generate links to the object
     */
    public List<BrAPIObjectProperty> getLinkPropertiesFor(BrAPIObjectType brAPIObjectType) {
        List<BrAPIObjectProperty> linkProperties = new ArrayList<>() ;

        if (id.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(id.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        if (name != null && name.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(name.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        if (pui != null && pui.isLink()) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(pui.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        return linkProperties ;
    }

    /**
     * Gets the id property name for a type
     * @param type The BrAPI type
     * @return the id property name for a type
     */
    public String getIdPropertyNameFor(String type) {
        return id.getPropertyNameFor(type) ;
    }

    /**
     * Gets the id property name for a type
     * @param type The BrAPI type
     * @return the id property name for a type
     */
    public String getIdPropertyNameFor(BrAPIType type) {
        return id.getPropertyNameFor(type) ;
    }

    /**
     * Gets the converted ids link property name for a property
     * @param property The BrAPI property
     * @return the converted property name that is used to return an array of ids
     */
    public String getIdsPropertyNameFor(BrAPIObjectProperty property) {
        return String.format("%sDbIds", StringUtils.toSingular(property.getName())) ;
    }

    /**
     * Gets the id property for a type
     * @param type The BrAPI type
     * @return the id property for a type
     */
    public Response<BrAPIObjectProperty> getIdPropertyFor(BrAPIType type) {

        if (type instanceof BrAPIObjectType brAPIObjectType) {
            String idPropertyName = getIdPropertyNameFor(type) ;
            return brAPIObjectType.getProperties()
                .stream()
                .filter(property -> property.getName().equals(idPropertyName))
                .findFirst()
                .map(Response::success)
                .orElseGet(() -> fail(Response.ErrorType.VALIDATION, String.format("Type '%s' does not have a property '%s'", type.getName(), idPropertyName))) ;
        } else {
            return fail(Response.ErrorType.VALIDATION, String.format("Type '%s' is not an object, is type '%s'", type.getName(), type.getClass().getSimpleName()));
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
            case ONE_TO_ONE -> BrAPITypeUtils.isPrimaryModel(type) ? LinkType.ID : LinkType.EMBEDDED ;
            case MANY_TO_ONE -> LinkType.ID ;
            case ONE_TO_MANY -> LinkType.ID ;
            case MANY_TO_MANY  -> LinkType.ID ;
        } ;
    }
}
