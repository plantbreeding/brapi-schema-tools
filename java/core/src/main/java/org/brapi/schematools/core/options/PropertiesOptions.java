package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import org.brapi.schematools.core.model.*;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
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

    /**
     * Default SQL column type used when collapsing {@code oneOf} unions to a single opaque column.
     * Typical values: {@code STRING} (default) or {@code VARIANT}.
     * Field default keeps partial YAML loads (that skip sql-options merge) valid.
     */
    private String oneOfColumnType = "STRING";

    /**
     * Per-parent / per-property override for {@link #oneOfColumnType}.
     * Keyed as {@code ParentType -> propertyName -> sqlType}.
     */
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, Map<String, String>> oneOfColumnTypeFor = new LinkedHashMap<>();

    /**
     * Default SQL column type for {@code AdditionalInfo} properties.
     * Supported values: {@code MAP} (emits {@code MAP<STRING,STRING>}), {@code STRING}, {@code VARIANT}.
     * Field default keeps partial YAML loads valid when defaults are not merged.
     */
    private String additionalInfoColumnType = "MAP";

    /**
     * Per-parent / per-property override for {@link #additionalInfoColumnType}.
     * Keyed as {@code ParentType -> propertyName -> sqlType}.
     */
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, Map<String, String>> additionalInfoColumnTypeFor = new LinkedHashMap<>();

    /**
     * Force a schema type (by type name) to a single SQL column type instead of expanding it.
     * Example: {@code GeoJSON: STRING} or {@code GeoJSON: VARIANT}.
     */
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, String> columnTypeFor = new LinkedHashMap<>();

    /**
     * Force a specific parent property to a single SQL column type.
     * Keyed as {@code ParentType -> propertyName -> sqlType}.
     * Takes precedence over type-level {@link #columnTypeFor} and oneOf / AdditionalInfo defaults.
     */
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, Map<String, String>> columnTypePropertyFor = new LinkedHashMap<>();

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
            .assertNotNull(oneOfColumnType, "'oneOfColumnType' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(additionalInfoColumnType, "'additionalInfoColumnType' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(oneOfColumnTypeFor, "'oneOfColumnTypeFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(additionalInfoColumnTypeFor, "'additionalInfoColumnTypeFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(columnTypeFor, "'columnTypeFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(columnTypePropertyFor, "'columnTypePropertyFor' option on %s is null", this.getClass().getSimpleName())
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

            if (coreOverride.oneOfColumnType != null) {
                oneOfColumnType = coreOverride.oneOfColumnType;
            }

            if (coreOverride.additionalInfoColumnType != null) {
                additionalInfoColumnType = coreOverride.additionalInfoColumnType;
            }

            mergeNestedStringMap(oneOfColumnTypeFor, coreOverride.oneOfColumnTypeFor);
            mergeNestedStringMap(additionalInfoColumnTypeFor, coreOverride.additionalInfoColumnTypeFor);
            mergeNestedStringMap(columnTypePropertyFor, coreOverride.columnTypePropertyFor);

            if (coreOverride.columnTypeFor != null) {
                coreOverride.columnTypeFor.forEach((key, value) -> {
                    if (value == null) {
                        columnTypeFor.remove(key);
                    } else {
                        columnTypeFor.put(key, value);
                    }
                });
            }
        }
    }

    private static void mergeNestedStringMap(Map<String, Map<String, String>> target,
                                             Map<String, Map<String, String>> override) {
        if (override == null) {
            return;
        }
        override.forEach((key, value) -> {
            if (value == null) {
                target.remove(key);
            } else if (target.containsKey(key)) {
                value.forEach((innerKey, innerValue) -> {
                    if (innerValue == null) {
                        target.get(key).remove(innerKey);
                    } else {
                        target.get(key).put(innerKey, innerValue);
                    }
                });
                if (target.get(key).isEmpty()) {
                    target.remove(key);
                }
            } else {
                target.put(key, new LinkedHashMap<>(value));
            }
        });
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        clusteringFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'clusteringFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        oneOfColumnTypeFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'oneOfColumnTypeFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        additionalInfoColumnTypeFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'additionalInfoColumnTypeFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        columnTypePropertyFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.isValidBrAPIClass(name),
                String.format("Invalid BrAPI Class name '%s' set for 'columnTypePropertyFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;
        }) ;

        return validation.merge(id.validateAgainstCache(brAPIClassCache))
            .merge(name.validateAgainstCache(brAPIClassCache))
            .merge(pui.validateAgainstCache(brAPIClassCache)) ;
    }

    /**
     * Finds the preferred link property that are used to generate links to the
     * provided object type.
     * <p>
     * This is usually the object dbId, but can be the name and/or PUI.
     *
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return the preferred link property that are used to generate links provided object type, if available
     */
    public Response<BrAPIObjectProperty> findLinkPropertyFor(BrAPIObjectType brAPIObjectType) {

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
     * This is usually the object dbId, but can also be the name and/or PUI.
     * @param parentType the type which has the property
     * @param property the property for which the link properties will be obtained. This is used to determine the format of the converted ids link property name.
     * @param brAPIObjectType the object type from which the properties will be obtained
     * @return list of link properties that are used to generate links to the object
     */
    public List<BrAPIObjectProperty> getLinkPropertiesFor(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType) {
        List<BrAPIObjectProperty> linkProperties = new ArrayList<>() ;

        addLinkProperty(parentType, property, brAPIObjectType, linkProperties, id);

        addLinkProperty(parentType, property, brAPIObjectType, linkProperties, pui);

        addLinkProperty(parentType, property, brAPIObjectType, linkProperties, name);

        return linkProperties ;
    }

    private void addLinkProperty(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType, List<BrAPIObjectProperty> linkProperties, PropertyOptions options) {
        if (options.isLinkForTypeOrProperty(parentType, property, brAPIObjectType))  {
            String childPropertyName = options.getPropertyNameFor(brAPIObjectType);
            brAPIObjectType.getProperties().stream()
                .filter(childProperty -> childProperty.getName().equals(childPropertyName) )
                .findFirst()
                .map(childProperty -> buildLinkProperty(parentType, property, brAPIObjectType, childProperty, options))
                .ifPresentOrElse(linkProperties::add, () -> linkProperties.add(createStringProperty(String.format(options.getNameFormat(), property.getName()), parentType, property, options)));
        }
    }

    private BrAPIObjectProperty buildLinkProperty(BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType, BrAPIObjectProperty childProperty, PropertyOptions options) {
        String linkPropertyName = String.format(options.getNameFormat(), property.getName()) ;

        if (brAPIObjectType.getName().equalsIgnoreCase(property.getName())) {
            linkPropertyName = childProperty.getName() ;
        }

        BrAPIObjectProperty.BrAPIObjectPropertyBuilder builder = childProperty.toBuilder().name(linkPropertyName) ;

        builder.nullable(options.getNullableForProperty(parentType, property)) ;
        builder.required(options.getRequiredForProperty(parentType, property)) ;
        builder.description(compositeDescription(property.getDescription(), childProperty.getDescription())) ;
        if (property.isDeprecated()) {
            builder.deprecated(true) ;
        }

        return builder.build();
    }

    private BrAPIObjectProperty createStringProperty(String name, BrAPIObjectType parentType, BrAPIObjectProperty property, PropertyOptions options) {
        return BrAPIObjectProperty.builder()
            .name(name)
            .type(BrAPIPrimitiveType.stringType())
            .description(property.getDescription())
            .nullable(options.getNullableForProperty(parentType, property))
            .required(options.getRequiredForProperty(parentType, property))
            .build();
    }

    /**
     * Returns a copy of an array link property with a composite description built from the
     * array property's own description and the description of the linked item type's ID
     * property. If the item type cannot be resolved or has no ID property the original
     * property is returned unchanged.
     *
     * @param property        the array link property (e.g. {@code studies: ARRAY<Study>})
     * @param itemObjectType  the dereferenced item type (e.g. {@code Study})
     * @return property with composite description set, or the original if no ID property found
     */
    public BrAPIObjectProperty withArrayLinkDescription(BrAPIObjectProperty property, BrAPIObjectType itemObjectType) {
        return getIdPropertyFor(itemObjectType)
            .mapResult(idProp -> property.toBuilder()
                .description(compositeDescription(property.getDescription(), idProp.getDescription()))
                .build())
            .orElseResult(property);
    }

    /**
     * Builds a composite description from a parent property description (the relationship context)
     * and a child/linked property description (the value semantics). If either is null or blank the
     * other is used alone. When both are present they are joined with a single space, inserting a
     * period after the parent description when it does not already end with one.
     */
    private String compositeDescription(String parentDescription, String childDescription) {
        boolean hasParent = parentDescription != null && !parentDescription.isBlank();
        boolean hasChild  = childDescription  != null && !childDescription.isBlank();

        if (!hasParent && !hasChild) return null;
        if (!hasParent) return childDescription;
        if (!hasChild)  return parentDescription;

        String parent = parentDescription.stripTrailing();
        String child  = childDescription.strip();
        String separator = parent.endsWith(".") || parent.endsWith("!") || parent.endsWith("?") ? " " : ". ";
        return parent + separator + child;
    }

    private boolean isLink(PropertyOptions propertyOptions, BrAPIObjectType parentType, BrAPIObjectProperty property, BrAPIObjectType brAPIObjectType, BrAPIObjectProperty childProperty) {
        return propertyOptions.isLinkForProperty(parentType, property) || childProperty.getName().equals(propertyOptions.getPropertyNameFor(brAPIObjectType)) ;
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
            if (typeClusteringConfig == null || Boolean.TRUE.equals(typeClusteringConfig.getOrDefault(clusteringPropertyName, true))) {
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
            Boolean value = map.get(property.getName());
            return value != null ? value : clustering.contains(property.getName());
        }

        return clustering.contains(property.getName());
    }

    /**
     * Resolves the SQL column type for a property, honouring overrides in this order:
     * <ol>
     *   <li>{@code columnTypePropertyFor.<Parent>.<property>}</li>
     *   <li>{@code columnTypeFor.<TypeName>} (using the property's schema type name)</li>
     *   <li>empty if neither is set</li>
     * </ol>
     *
     * @param parentType the parent object type that owns the property
     * @param property   the property being generated
     * @return optional SQL type string such as {@code STRING}, {@code VARIANT}, or {@code MAP}
     */
    @JsonIgnore
    public java.util.Optional<String> findForcedColumnTypeFor(BrAPIObjectType parentType, BrAPIObjectProperty property) {
        Map<String, String> propertyMap = columnTypePropertyFor.get(parentType.getName());
        if (propertyMap != null) {
            String propertyType = propertyMap.get(property.getName());
            if (propertyType != null && !propertyType.isBlank()) {
                return java.util.Optional.of(propertyType.trim());
            }
        }

        String schemaTypeName = property.getType() != null ? property.getType().getName() : null;
        if (schemaTypeName != null) {
            String typeLevel = columnTypeFor.get(schemaTypeName);
            if (typeLevel != null && !typeLevel.isBlank()) {
                return java.util.Optional.of(typeLevel.trim());
            }
        }

        return java.util.Optional.empty();
    }

    /**
     * SQL type used when collapsing a {@code oneOf} property to a single opaque column.
     * Resolution order: {@code oneOfColumnTypeFor.<Parent>.<property>}, then {@code oneOfColumnType}.
     *
     * @param parentType the parent object type that owns the property
     * @param property   the oneOf property being generated
     * @return SQL type string (default {@code STRING})
     */
    @JsonIgnore
    public String getOneOfColumnTypeFor(BrAPIObjectType parentType, BrAPIObjectProperty property) {
        Map<String, String> propertyMap = oneOfColumnTypeFor.get(parentType.getName());
        if (propertyMap != null) {
            String propertyType = propertyMap.get(property.getName());
            if (propertyType != null && !propertyType.isBlank()) {
                return propertyType.trim();
            }
        }
        return oneOfColumnType != null && !oneOfColumnType.isBlank() ? oneOfColumnType.trim() : "STRING";
    }

    /**
     * SQL type used for {@code AdditionalInfo} properties.
     * Resolution order: {@code additionalInfoColumnTypeFor.<Parent>.<property>},
     * then {@code additionalInfoColumnType}.
     * Supported values: {@code MAP} (→ {@code MAP<STRING,STRING>}), {@code STRING}, {@code VARIANT}.
     *
     * @param parentType the parent object type that owns the property
     * @param property   the AdditionalInfo property being generated
     * @return configured type token (default {@code MAP})
     */
    @JsonIgnore
    public String getAdditionalInfoColumnTypeFor(BrAPIObjectType parentType, BrAPIObjectProperty property) {
        Map<String, String> propertyMap = additionalInfoColumnTypeFor.get(parentType.getName());
        if (propertyMap != null) {
            String propertyType = propertyMap.get(property.getName());
            if (propertyType != null && !propertyType.isBlank()) {
                return propertyType.trim();
            }
        }
        return additionalInfoColumnType != null && !additionalInfoColumnType.isBlank()
            ? additionalInfoColumnType.trim()
            : "MAP";
    }
}
