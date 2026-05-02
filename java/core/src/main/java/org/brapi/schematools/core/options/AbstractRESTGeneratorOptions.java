package org.brapi.schematools.core.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.model.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.validiation.Validation;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toLowerCase;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;

/**
 * Abstract base class for generator options that produce REST-style endpoints
 * (e.g. OpenAPI, R, Python). Holds the shared endpoint-independent options and
 * path-item helper methods that are otherwise duplicated across those generators.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Accessors(chain = true)
public abstract class AbstractRESTGeneratorOptions extends AbstractMainGeneratorOptions {

    private Boolean overwrite;
    private Boolean addGeneratorComments;

    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pathItemNameFor = new HashMap<>();

    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, String>> pathItemNameForProperty = new HashMap<>();

    @Setter(AccessLevel.PRIVATE)
    private SingleGetOptions singleGet;
    @Setter(AccessLevel.PRIVATE)
    private ListGetOptions listGet;
    @Setter(AccessLevel.PRIVATE)
    private PostOptions post;
    @Setter(AccessLevel.PRIVATE)
    private PutOptions put;
    @Setter(AccessLevel.PRIVATE)
    private DeleteOptions delete;
    @Setter(AccessLevel.PRIVATE)
    private SearchOptions search;
    @Setter(AccessLevel.PRIVATE)
    private PropertiesOptions properties;
    @Setter(AccessLevel.PRIVATE)
    private ControlledVocabularyOptions controlledVocabulary;
    @Setter(AccessLevel.PRIVATE)
    private TableOptions table;
    @Setter(AccessLevel.PRIVATE)
    private SearchTableOptions searchTable;

    // -------------------------------------------------------------------------
    // validate
    // -------------------------------------------------------------------------

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(pathItemNameFor, "'pathItemNameFor' option is null")
            .assertNotNull(singleGet, "Single Get Endpoint Options are null")
            .assertNotNull(listGet,  "List Get Endpoint Options are null")
            .assertNotNull(post, "Post Endpoint Options are null")
            .assertNotNull(put, "Put Endpoint Options are null")
            .assertNotNull(delete, "Delete Endpoint Options are null")
            .assertNotNull(search, "Search Endpoint Options are null")
            .assertNotNull(properties, "Properties Options are null")
            .assertNotNull(controlledVocabulary, "Controlled Vocabulary Options are null")
            .assertNotNull(table, "Table Options are null")
            .assertNotNull(searchTable, "Search Table Options are null")
            .merge(singleGet)
            .merge(post)
            .merge(put)
            .merge(delete)
            .merge(search)
            .merge(properties)
            .merge(controlledVocabulary);
    }

    // -------------------------------------------------------------------------
    // override
    // -------------------------------------------------------------------------

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public AbstractRESTGeneratorOptions override(AbstractRESTGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
        }

        if (overrideOptions.pathItemNameFor != null) {
            overrideOptions.pathItemNameFor.forEach((key, value) -> {
                if (value == null) pathItemNameFor.remove(key);
                else pathItemNameFor.put(key, value);
            });
        }

        if (overrideOptions.pathItemNameForProperty != null) {
            overrideOptions.pathItemNameForProperty.forEach((key, value) -> {
                if (value == null) {
                    pathItemNameForProperty.remove(key);
                } else if (pathItemNameForProperty.containsKey(key)) {
                    value.forEach((innerKey, innerValue) -> {
                        if (innerValue == null) pathItemNameForProperty.get(key).remove(innerKey);
                        else pathItemNameForProperty.get(key).put(innerKey, innerValue);
                    });
                    if (pathItemNameForProperty.get(key).isEmpty()) pathItemNameForProperty.remove(key);
                } else {
                    pathItemNameForProperty.put(key, new HashMap<>(value));
                }
            });
        }

        if (overrideOptions.singleGet != null) {
            singleGet.override(overrideOptions.singleGet);
        }
        if (overrideOptions.listGet != null) {
            listGet.override(overrideOptions.getListGet()) ;
        }
        if (overrideOptions.post != null) {
            post.override(overrideOptions.post);
        }
        if (overrideOptions.put != null) {
            put.override(overrideOptions.put);
        }
        if (overrideOptions.delete != null) {
            delete.override(overrideOptions.delete);
        }
        if (overrideOptions.search != null) {
            search.override(overrideOptions.search);
        }
        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.properties);
        }
        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary.override(overrideOptions.controlledVocabulary);
        }
        if (overrideOptions.table != null) {
            table.override(overrideOptions.table);
        }
        if (overrideOptions.searchTable != null) {
            searchTable.override(overrideOptions.searchTable);
        }

        return this;
    }

    // -------------------------------------------------------------------------
    // Convenience methods
    // -------------------------------------------------------------------------

    /**
     * Determines if the Generator should overwrite existing files.
     *
     * @return {@code true} if the Generator should overwrite existing files, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isOverwritingExistingFiles() {
        return overwrite != null && overwrite;
    }

    /**
     * Determines if the Generator should add generator comments to each generated file.
     *
     * @return {@code true} if the Generator should add generator comments, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingGeneratorComments() {
        return addGeneratorComments != null && addGeneratorComments;
    }

    /**
     * Gets the path item name for a specific Primary Model.
     *
     * @param name the name of the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getPathItemNameFor(String name) {
        return pathItemNameFor.getOrDefault(name, String.format("/%s", toLowerCase(getPluralFor(name))));
    }

    /**
     * Gets the path item name for a specific Primary Model.
     *
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getPathItemNameFor(BrAPIType type) {
        return getPathItemNameFor(type.getName());
    }

    /**
     * Gets the search path item name for a specific Primary Model.
     *
     * @param name the name of the Primary Model
     * @return the search path item name for a specific Primary Model
     */
    public final String getSearchPathItemNameFor(String name) {
        return String.format("/search%s", getPathItemNameFor(name));
    }

    /**
     * Gets the search path item name for a specific Primary Model.
     *
     * @param type the Primary Model
     * @return the search path item name for a specific Primary Model
     */
    public final String getSearchPathItemNameFor(BrAPIType type) {
        return String.format("/search%s", getPathItemNameFor(type));
    }

    /**
     * Gets the path item name for a specific BrAPI Property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, String> map = pathItemNameForProperty.get(typeName);
        String defaultName = String.format("%s/%s", getPathItemNameFor(typeName), toPlural(propertyName));
        return map != null ? map.getOrDefault(propertyName, defaultName) : defaultName;
    }

    /**
     * Gets the path item name for a specific BrAPI Property.
     *
     * @param type     the primary model
     * @param property the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getPathItemNameForProperty(type.getName(), property.getName());
    }

    /**
     * Gets the path item name for a specific BrAPI Property.
     *
     * @param typeWithProperty the primary model with the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return getPathItemNameForProperty(typeWithProperty.getType(), typeWithProperty.getProperty());
    }

    /**
     * Sets the path item name for a specific BrAPI Property.
     *
     * @param typeName     the name of the primary model
     * @param propertyName the name of the property
     * @param pathItemName the path item name
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractRESTGeneratorOptions setPathItemNameForProperty(String typeName, String propertyName, String pathItemName) {
        pathItemNameForProperty
            .computeIfAbsent(typeName, k -> new HashMap<>())
            .put(propertyName, pathItemName);
        return this;
    }

    /**
     * Sets the path item name for a specific BrAPI Property.
     *
     * @param type         the primary model
     * @param property     the property
     * @param pathItemName the path item name
     * @return the options for chaining
     */
    @JsonIgnore
    public final AbstractRESTGeneratorOptions setPathItemNameForProperty(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, String pathItemName) {
        return setPathItemNameForProperty(type.getName(), property.getName(), pathItemName);
    }

    /**
     * Determines if a Python class is generated for a specific primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if a class is generated for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    @Override
    public boolean isGeneratingFor(@NonNull String name) {
        return super.isGeneratingFor(name) && (getSingleGet().isGeneratingFor(name) ||
            getListGet().isGeneratingFor(name) ||
            getPost().isGeneratingFor(name) ||
            getPut().isGeneratingFor(name) ||
            getDelete().isGeneratingFor(name) ||
            getSearch().isGeneratingFor(name));
    }

    /**
     * Determines if a class is generated for a specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if a class is generated for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    @Override
    public boolean isGeneratingFor(@NonNull BrAPIType type) {
        return getSingleGet().isGeneratingFor(type) ||
            getListGet().isGeneratingFor(type) ||
            getPost().isGeneratingFor(type) ||
            getPut().isGeneratingFor(type) ||
            getDelete().isGeneratingFor(type) ||
            getSearch().isGeneratingFor(type);
    }

    /**
     * Gets the singular name for a pluralised property name.
     *
     * @param propertyName the pluralised property name
     * @return the singular name for the property
     */
    @JsonIgnore
    public final String getSingularForProperty(@NonNull String propertyName) {
        return toSingular(propertyName);
    }

    /**
     * Determines if a specific property should be exposed as a separate sub-path endpoint.
     *
     * @param type     the Object type
     * @param property the Object type property
     * @return {@code true} if a separate endpoint will be created for the property, {@code false} otherwise
     */
    public boolean isGeneratingSubPathFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return getProperties().getLinkTypeFor(type, property).mapResult(LinkType.SUB_QUERY::equals).orElseResult(false);
    }

    /**
     * Gets the name of the sub-path endpoint for a property.
     *
     * @param pathItemName the path prefix
     * @param property     the Object type property
     * @return the name of the sub-path endpoint for the property
     */
    public final String getSubPathItemNameFor(String pathItemName, BrAPIObjectProperty property) {
        return String.format("%s/%s", pathItemName, toLowerCase(property.getName()));
    }

    /**
     * Determines whether controlled vocabulary endpoints should be generated.
     *
     * @return {@code true} if controlled vocabulary endpoints should be generated, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingControlledVocabularyEndpoints() {
        return controlledVocabulary != null && controlledVocabulary.isGenerating();
    }
}

