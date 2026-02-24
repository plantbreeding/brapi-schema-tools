package org.brapi.schematools.core.openapi.generator.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

/**
 * Sub Options class that makes use of the Request objects
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AbstractOpenAPIRequestSubOptions extends AbstractOpenAPISubOptions {
    protected Boolean propertiesFromRequest ;
    protected Map<String, Map<String, Boolean>> propertyFromRequestFor = new HashMap<>();

    public Validation validate() {
        return super.validate()
            .assertNotNull(propertiesFromRequest,  "'propertiesFromRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFromRequestFor,  "'propertyFromRequestFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(ListGetOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.propertyFromRequestFor != null) {
            overrideOptions.propertyFromRequestFor.forEach((key, value) -> {
                if (propertyFromRequestFor.containsKey(key)) {
                    propertyFromRequestFor.get(key).putAll(value) ;
                } else {
                    propertyFromRequestFor.put(key, new HashMap<>(value)) ;
                }
            });
        }
    }

    @Override
    public Validation validateAgainstCache(BrAPIClassCacheBuilder.BrAPIClassCache brAPIClassCache) {
        Validation validation = super.validateAgainstCache(brAPIClassCache);

        propertyFromRequestFor.keySet().forEach(name -> {
            validation.assertTrue(brAPIClassCache.containsBrAPIClass(name),
                String.format("Invalid Primary Model name '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            BrAPIClass requestClass = brAPIClassCache.getBrAPIRequestClass(name);
            validation.assertNotNull(brAPIClassCache.containsPrimaryModel(name),
                String.format("Can not find Request class for '%s' set for 'propertyFromRequestFor' on %s",
                    name,
                    this.getClass().getSimpleName()
                )) ;

            if (requestClass instanceof BrAPIObjectType brAPIObjectType) {
                propertyFromRequestFor.get(name).keySet().forEach(propertyName -> {
                    validation.assertTrue(brAPIObjectType.getProperties().stream().anyMatch(property -> propertyName.equals(property.getName())),
                        String.format("Invalid Property name '%s' for BrAPIObjectType '%s' set for 'propertyFromRequestFor' on %s. Possible properties are: '%s'",
                            propertyName,
                            requestClass.getName(),
                            this.getClass().getSimpleName(),
                            String.join(", ",
                                brAPIObjectType.getProperties().stream().map(BrAPIObjectProperty::getName).toList())));
                }) ;
            }
        }) ;

        return validation ;
    }


    /**
     * Gets whether a property from the Request is used in the List query
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @return <code>true</code> if the property from the Request is used in the List query
     */
    public final boolean isUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return isUsingPropertyFromRequestFor(type.getName(), property.getName()) ;
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param typeName The BrAPI Object type name
     * @param propertyName The BrAPI property name
     * @return <code>true</code> if the property from the Request is used in the List query
     */
    public final boolean isUsingPropertyFromRequestFor(String typeName, String propertyName) {

        Map<String, Boolean> map = propertyFromRequestFor.get(typeName) ;

        if (map != null) {
            return map.getOrDefault(propertyName, propertiesFromRequest) ;
        }

        return propertiesFromRequest ;
    }

    /**
     * Gets whether a property from the Request is used in the List query
     * @param type The BrAPI Object type
     * @param property The BrAPI property
     * @param propertiesFromRequest <code>true</code> if the property from the Request is used in the List query
     * @return the options for chaining
     */
    public final AbstractOpenAPIRequestSubOptions setUsingPropertyFromRequestFor(BrAPIObjectType type, BrAPIObjectProperty property, Boolean propertiesFromRequest) {

        Map<String, Boolean> map = propertyFromRequestFor.get(type.getName()) ;

        if (map != null) {
            map.put(property.getName(), propertiesFromRequest) ;
        } else {
            map = new HashMap<>() ;
            map.put(property.getName(), propertiesFromRequest) ;
            propertyFromRequestFor.put(type.getName(), map) ;

        }
        return this ;
    }
}
