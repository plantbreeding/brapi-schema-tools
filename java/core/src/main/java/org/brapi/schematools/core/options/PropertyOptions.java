package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;

/**
 * Provides options for the generation of property and their usage
 */
@Getter(AccessLevel.PRIVATE)
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PropertyOptions implements Options {
    private String nameFormat;
    @Getter(AccessLevel.PUBLIC)
    private Boolean link;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> linkFor = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> propertyFor = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pluralPropertyFor = new HashMap<>();

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(nameFormat, "'analyse' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(link, "'link' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(linkFor, "'linkFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(propertyFor, "'propertyFor' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(pluralPropertyFor, "'pluralPropertyFor' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(PropertyOptions overrideOptions) {
        if (overrideOptions.nameFormat != null) {
            setNameFormat(overrideOptions.nameFormat); ;
        }

        if (overrideOptions.link != null) {
            setLink(overrideOptions.link); ;
        }

        linkFor.putAll(overrideOptions.linkFor);
        propertyFor.putAll(overrideOptions.propertyFor);
        pluralPropertyFor.putAll(overrideOptions.pluralPropertyFor);
    }

    /**
     * Get if this property is used as a foreign key link between entities
     * @return {@code true} if this property is used as a foreign key link between entities, {@code false} otherwise
     */
    public boolean isLink() {
        return link != null && link ;
    }

    /**
     * Gets if this property is used as a foreign key link between entities for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setLinkFor(String, String)}
     * to override this value.
     * @param name the name of the primary model
     * @return {@code true} if this property is used as a foreign key link between entities
     * for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isLinkFor(String name) {
        return linkFor.getOrDefault(name, link) ;
    }

    /**
     * Gets if this property is used specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setLinkFor(BrAPIType, String)}
     * to override this value.
     * @param type the primary model
     * @return {@code true} if this property is used as a foreign key link between entities
     * for a specific primary model, {@code false} otherwise
     */
    public boolean isLinkFor(BrAPIType type) {
        return isLinkFor(type.getName()) ;
    }

    /**
     * Sets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default.
     * @param name the name of the primary model
     * @param isLink {@code true} if this property is used as a foreign key link between entities
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setLinkFor(String name, Boolean isLink) {
        linkFor.put(name, isLink) ;

        return this ;
    }

    /**
     * Sets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default.
     * @param type the primary model
     * @param isLink {@code true} if this property is used as a foreign key link between entities
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setLinkFor(BrAPIType type, Boolean isLink) {
        return setLinkFor(type.getName(), isLink) ;
    }

    /**
     * Gets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setPropertyNameFor(String, String)}
     * to override this value.
     * @param name the name of the primary model
     * @return property name for a specific primary model
     */
    @JsonIgnore
    public String getPropertyNameFor(String name) {
        return propertyFor.getOrDefault(name, String.format(nameFormat, toParameterCase(name))) ;
    }

    /**
     * Gets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setPropertyNameFor(BrAPIType, String)}
     * to override this value.
     * @param type the primary model
     * @return property name for a specific primary model
     */
    public String getPropertyNameFor(BrAPIType type) {
        return getPropertyNameFor(type.getName()) ;
    }

    /**
     * Sets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default.
     * @param name the name of the primary model
     * @param parameterName the property name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setPropertyNameFor(String name, String parameterName) {
        propertyFor.put(name, parameterName) ;

        return this ;
    }

    /**
     * Sets the property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default.
     * @param type the primary model
     * @param parameterName the property name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setPropertyNameFor(BrAPIType type, String parameterName) {
        return setPropertyNameFor(type.getName(), parameterName) ;
    }

    /**
     * Gets the plural property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setPluralPropertyNameFor} to
     * override this value.
     * @param name the name of the primary model
     * @return property name for a specific primary model
     */
    @JsonIgnore
    public String getPluralPropertyNameFor(String name) {
        return propertyFor.getOrDefault(name, toPlural(String.format(nameFormat, toParameterCase(name)))) ;
    }

    /**
     * Gets the plural property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbId' by default. Use {@link #setPluralPropertyNameFor}
     * to override this value.
     * @param type the primary model
     * @return property name for a specific primary model
     */
    public String getPluralPropertyNameFor(BrAPIType type) {
        return getPluralPropertyNameFor(type.getName()) ;
    }

    /**
     * Sets the plural property name for a specific primary model. For example the ids property (or field)
     * name of Study, would be 'studyDbIds' by default.
     * @param name the name of the primary model
     * @param parameterName the plural property name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setPluralPropertyNameFor(String name, String parameterName) {
        propertyFor.put(name, parameterName) ;

        return this ;
    }

    /**
     * Sets the plural property name for a specific primary model. For example the id property (or field)
     * name of Study, would be 'studyDbIds' by default.
     * @param type the primary model
     * @param idParameter the plural property name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public PropertyOptions setPluralPropertyNameFor(BrAPIType type, String idParameter) {
        return setPluralPropertyNameFor(type.getName(), idParameter) ;
    }
}
