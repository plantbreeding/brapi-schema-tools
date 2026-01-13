package org.brapi.schematools.core.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPITypeUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;
import static org.brapi.schematools.core.utils.BrAPITypeUtils.unwrapType;

public abstract class AbstractPropertiesOptions implements Options {
    @Getter(AccessLevel.PRIVATE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, String>> linkTypeFor = new HashMap<>();

    public Validation validate() {
        // Only validates linkTypeFor by default; subclasses should extend
        return Validation.valid()
            .assertNotNull(linkTypeFor, "'linkTypeFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    public void override(AbstractPropertiesOptions overrideOptions) {
        if (overrideOptions.linkTypeFor != null) {
            overrideOptions.linkTypeFor.forEach((key, value) -> {
                if (linkTypeFor.containsKey(key)) {
                    linkTypeFor.get(key).putAll(value);
                } else {
                    linkTypeFor.put(key, new HashMap<>(value));
                }
            });
        }
    }

    public Response<LinkType> getLinkTypeFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        Map<String, String> map = linkTypeFor.get(type.getName());
        if (map != null) {
            return LinkType.fromNameOrLabels(map.get(property.getName())).or(() -> getDefaultLinkTypeFor(property, property.getType()));
        }
        return getDefaultLinkTypeFor(property, property.getType());
    }

    /**
     * Gets the link type for a type property. The property type which needs to be dereferenced first.
     * @param parentType The BrAPI Object parent type
     * @param property The BrAPI property
     * @param dereferencedType The BrAPI property type, which has been dereferenced first.
     * @return the link type for specified type property
     */
    public Response<LinkType> getLinkTypeFor(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIType dereferencedType) {
        Map<String, String> map = linkTypeFor.get(parentType.getName()) ;

        if (map != null) {
            return LinkType.findByNameOrLabel(map.get(property.getName())).map(Response::success).orElseGet(() -> getDefaultLinkTypeFor(property, dereferencedType)) ;
        }

        return getDefaultLinkTypeFor(property, dereferencedType) ;
    }

    private Response<LinkType> getDefaultLinkTypeFor(BrAPIObjectProperty property, BrAPIType dereferencedType) {

        BrAPIType unwrappedType = unwrapType(dereferencedType);

        if (unwrappedType instanceof BrAPIReferenceType) {
            return fail(Response.ErrorType.VALIDATION, String.format("The type '%s' needs to be dereferenced first", unwrappedType.getName())) ;
        }

        BrAPIRelationshipType relationshipType = property.getRelationshipType() != null ? property.getRelationshipType() : BrAPIRelationshipType.ONE_TO_ONE;

        return success(switch (relationshipType) {
            case ONE_TO_ONE, MANY_TO_ONE, ONE_TO_MANY -> unwrappedType instanceof BrAPIClass && BrAPITypeUtils.isPrimaryModel((BrAPIClass)unwrappedType) ? LinkType.ID : LinkType.EMBEDDED ;
            case MANY_TO_MANY  -> LinkType.SUB_QUERY ;
        }) ;
    }
}