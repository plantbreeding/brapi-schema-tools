package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.brapi.schematools.core.response.Response.fail;
import static org.brapi.schematools.core.response.Response.success;

/**
 * Provides options for the generation of ID, Name and PUI property and their usage
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertiesOptions extends AbstractPropertiesOptions {
    private String descriptionFormat;
    private PropertyOptions id;
    private PropertyOptions name;
    private PropertyOptions pui;
    private List<String> clustering = new ArrayList<>();
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, Map<String, Boolean>> clusteringFor = new LinkedHashMap<>();
    private int maximumClusteringProperties;

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(descriptionFormat, "'descriptionFormat' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(id, "'id' option on %s is null", this.getClass().getSimpleName())
            .merge(id)
            .assertNotNull(id, "'name' option on %s is null", this.getClass().getSimpleName())
            .merge(name)
            .assertNotNull(id, "'pui' option on %s is null", this.getClass().getSimpleName())
            .assertGreaterThan(maximumClusteringProperties, 0.0, "'maximumClusteringProperties' option on %s must be greater than 0", this.getClass().getSimpleName())
            .merge(pui);
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
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

            if (coreOverride.clustering != null && !coreOverride.clustering.isEmpty()) {
                clustering.addAll(coreOverride.clustering.stream()
                    .filter(e -> !clustering.contains(e))
                    .toList());
            }

            if (coreOverride.clusteringFor != null) {
                coreOverride.clusteringFor.forEach((key, value) -> {
                    if (value == null) {
                        clusteringFor.remove(key);
                    } else if (clusteringFor.containsKey(key)) {
                        value.forEach((innerKey, innerValue) -> {
                            if (innerValue == null) clusteringFor.get(key).remove(innerKey);
                            else clusteringFor.get(key).put(innerKey, innerValue);
                        });
                        if (clusteringFor.get(key).isEmpty()) clusteringFor.remove(key);
                    } else {
                        clusteringFor.put(key, new LinkedHashMap<>(value));
                    }
                });
            }
        }
    }

    /**
     * Gets preferred link property that are used to generate links to the
     * provided object type.
     * <p>
     * This is usually the object dbId, but can be the name and/or PUI.
     *
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of link properties that are used to generate links to the object
     */
    public Response<BrAPIObjectProperty> getLinkPropertyFor(BrAPIObjectType brAPIObjectType) {

        List<BrAPIObjectProperty> linkProperties = getLinkPropertiesFor(brAPIObjectType);

        if (linkProperties.isEmpty()) {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("No link property found for type '%s'", brAPIObjectType.getName()));
        } else {
            return success(linkProperties.getFirst());
        }
    }

    /**
     * Determine if a property is the primary link property for the provided object type.
     *
     * @param brAPIObjectType the object type from which the properties will be checked
     * @param property        the property to check
     * @return {code}true{code} if the provided property is the primary link property for the provided object type.
     */
    public boolean isPrimaryLinkPropertyFor(BrAPIObjectType brAPIObjectType, BrAPIObjectProperty property) {
        List<BrAPIObjectProperty> linkProperties = new ArrayList<>();

        if (id.isLinkFor(brAPIObjectType) && property.getName().equals(id.getPropertyNameFor(brAPIObjectType))) {
            return true;
        }

        if (pui.isLinkFor(brAPIObjectType) && property.getName().equals(pui.getPropertyNameFor(brAPIObjectType))) {
            return true;
        }

        return name.isLinkFor(brAPIObjectType) && property.getName().equals(name.getPropertyNameFor(brAPIObjectType));
    }

    /**
     * Gets the list of primary properties that are usually listed first in definitions
     * <p>
     * This is usually the object DbId, PUI and name.
     *
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of primary properties that are usually listed first in definitions
     */
    public List<BrAPIObjectProperty> getPrimaryPropertiesFor(BrAPIObjectType brAPIObjectType) {
        return brAPIObjectType.getProperties().stream()
            .filter(property -> isPrimaryProperty(property, brAPIObjectType))
            .toList();
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

        if (pui.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(pui.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        if (name.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(name.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .ifPresent(linkProperties::add);
        }

        return linkProperties ;
    }

    /**
     * Gets the list of link properties that are used to generate links to the
     * provided object type for the specific property.
     *
     * This is usually the object dbId, but can also be the name and/or PUI.
     * @param property the property for which the link properties will be obtained. This is used to determine the format of the converted ids link property name.
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of link properties that are used to generate links to the object
     */
    public List<BrAPIObjectProperty> getLinkPropertiesFor(BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
        List<BrAPIObjectProperty> linkProperties = new ArrayList<>() ;

        if (id.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(id.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .map(childProperty -> childProperty.toBuilder().name(String.format(id.getNameFormat(), property.getName())).build())
                .ifPresent(linkProperties::add);
        }

        if (pui.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(pui.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .map(childProperty -> childProperty.toBuilder().name(String.format(pui.getNameFormat(), property.getName())).build())
                .ifPresent(linkProperties::add);
        }

        if (name.isLinkFor(brAPIObjectType)) {
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(name.getPropertyNameFor(brAPIObjectType)))
                .findFirst()
                .map(childProperty -> childProperty.toBuilder().name(String.format(name.getNameFormat(), property.getName())).build())
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
     * Gets the converted id link property name for a property
     * @param property The BrAPI property
     * @return the converted property name that is used to return an array of ids
     */
    public String getIdsPropertyNameFor(BrAPIObjectProperty property) {

        String format = "%s" ;

        if (id.isLinkFor(property.getType())) {
            format = id.getPluralNameFormat() ;
        } else if (pui.isLinkFor(property.getType())) {
            format = pui.getPluralNameFormat() ;
        } else if (name.isLinkFor(property.getType())) {
            format = name.getPluralNameFormat() ;
        }

        return String.format(format, StringUtils.toSingular(property.getName())) ;
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

    /**
     * Gets the list of link properties that are used to generate links to the
     * provided object type. Maximum of 4 properties are returned, even if more properties are configured as link properties
     *
     *
     * This is usually the object dbId, but can also be the name and/or PUI.
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of link properties that are used to generate links to the object
     */
    public List<BrAPIObjectProperty> getClusteringPropertiesFor(BrAPIObjectType brAPIObjectType) {
        List<BrAPIObjectProperty> properties = new ArrayList<>();

        // Create a map of property names to property objects for quick lookup
        Map<String, BrAPIObjectProperty> propertyMap = new HashMap<>();
        for (BrAPIObjectProperty property : brAPIObjectType.getProperties()) {
            propertyMap.put(property.getName(), property);
        }

        // Check if there's a specific clusteringFor configuration for this type
        Map<String, Boolean> typeClusteringConfig = clusteringFor.get(brAPIObjectType.getName());

        // First, add properties from the base clustering list that aren't explicitly set to false
        for (String clusteringPropertyName : clustering) {
            if (typeClusteringConfig == null || typeClusteringConfig.getOrDefault(clusteringPropertyName, true)) {
                if (propertyMap.containsKey(clusteringPropertyName)) {
                    properties.add(propertyMap.get(clusteringPropertyName));
                }
            }
        }

        // Then add type-specific properties in the order they appear in the config
        if (typeClusteringConfig != null) {
            for (Map.Entry<String, Boolean> entry : typeClusteringConfig.entrySet()) {
                String propertyName = entry.getKey();
                Boolean include = entry.getValue();

                // Only add if true and not already added from base clustering
                if (include && propertyMap.containsKey(propertyName) &&
                    properties.stream().noneMatch(p -> p.getName().equals(propertyName))) {
                    properties.add(propertyMap.get(propertyName));
                }
            }
        }

        return properties.subList(0, Math.min(maximumClusteringProperties, properties.size())) ;
    }

    private boolean isPrimaryProperty(BrAPIObjectProperty property, BrAPIType brAPIObjectType) {
        return property.getName().equals(id.getPropertyNameFor(brAPIObjectType))
            || property.getName().equals(name.getPropertyNameFor(brAPIObjectType))
            || property.getName().equals(pui.getPropertyNameFor(brAPIObjectType)) ;
    }

    private Boolean isClusteringProperty(BrAPIObjectProperty property, BrAPIType brAPIObjectType) {

        Map<String, Boolean> map = clusteringFor.get(brAPIObjectType.getName());

        if (map != null) {
            return map.getOrDefault(property.getName(), clustering.contains(property.getName()));
        }

        return clustering.contains(property.getName());
    }
}
