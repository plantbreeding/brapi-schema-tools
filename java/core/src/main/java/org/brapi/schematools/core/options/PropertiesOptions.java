package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.util.ArrayList;
import java.util.List;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Provides options for the generation of ID, Name and PUI property and their usage
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions extends AbstractPropertiesOptions {
    private String descriptionFormat;
    private PropertyOptions id ;
    private PropertyOptions name ;
    private PropertyOptions pui ;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(id, "'id' option on %s is null", this.getClass().getSimpleName())
            .merge(id)
            .assertNotNull(id, "'name' option on %s is null", this.getClass().getSimpleName())
            .merge(name)
            .assertNotNull(id, "'pui' option on %s is null", this.getClass().getSimpleName())
            .merge(pui) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    @Override
    public void override(AbstractPropertiesOptions overrideOptions) {
        super.override(overrideOptions);
        if (overrideOptions instanceof PropertiesOptions coreOverride) {
            if (coreOverride.descriptionFormat != null) {
                descriptionFormat = coreOverride.descriptionFormat;
            }
            if (coreOverride.id != null) {
                id.override(coreOverride.id);
            }
            if (coreOverride.name != null) {
                name.override(coreOverride.name);
            }
            if (coreOverride.pui != null) {
                pui.override(coreOverride.pui);
            }
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

        if (id.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(id.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        if (name.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(name.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        if (pui.isLinkFor(brAPIObjectType)) {
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
     * Gets the description for a specific property in type
     * @param type the type
     * @param property the property
     * @return the description for a specific property
     */
    @JsonIgnore
    public final String getDescriptionFor(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return property.getDescription() != null ? property.getDescription() : String.format("%s: %s", property.getName(), type.getName()) ;
    }
}
