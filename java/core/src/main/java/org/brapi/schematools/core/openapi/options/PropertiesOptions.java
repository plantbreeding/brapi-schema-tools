package org.brapi.schematools.core.openapi.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.valdiation.Validation;

import java.util.ArrayList;
import java.util.List;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Provides options for the generation of ID, Name and PUI property and their usage
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions implements Options {
    private PropertyOptions id ;
    private PropertyOptions name ;
    private PropertyOptions pui ;

    public Validation validate() {
        return Validation.valid()
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
     * Gets the id parameter name for a type
     * @param type The BrAPI type
     * @return the id parameter name for a type
     */
    public String getIdPropertyNameFor(String type) {
        return id.getPropertyNameFor(type) ;
    }

    /**
     * Gets the id parameter name for a type
     * @param type The BrAPI type
     * @return the id parameter name for a type
     */
    public String getIdPropertyNameFor(BrAPIType type) {
        return id.getPropertyNameFor(type) ;
    }

    /**
     * Gets the ids parameter name for a type
     * @param type The BrAPI type
     * @return the id parameter name for a type
     */
    public String getIdsPropertyNameFor(BrAPIType type) {
        return id.getPluralPropertyNameFor(type) ;
    }

    /**
     * Gets the id parameter for a type
     * @param type The BrAPI type
     * @return the id parameter for a type
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
}
