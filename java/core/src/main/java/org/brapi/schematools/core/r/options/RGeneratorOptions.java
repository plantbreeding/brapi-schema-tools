package org.brapi.schematools.core.r.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.options.AbstractMainGeneratorOptions;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.PropertiesOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;
import org.brapi.schematools.core.r.RGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toLowerCase;
import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;

/**
 * Options for the {@link RGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class RGeneratorOptions extends AbstractMainGeneratorOptions {

    private Boolean overwrite;
    private Boolean addGeneratorComments;

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
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pathItemNameFor = new HashMap<>();
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Map<String, String>> pathItemNameForProperty = new HashMap<>();

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static RGeneratorOptions load() {
        try {
            RGeneratorOptions options = ConfigurationUtils.load("r-options.yaml", RGeneratorOptions.class);

            loadBrAPISchemaReaderOptions(options) ;

            return options ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or JSON. 
     * The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static RGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, RGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or JSON.
     * The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static RGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, RGeneratorOptions.class));
    }

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(singleGet, "Single Get Endpoint Options are null")
            .assertNotNull(listGet,  "List Get Endpoint Options are null")
            .assertNotNull(post, "Post Endpoint Options are null")
            .assertNotNull(put, "Put Endpoint Options are null")
            .assertNotNull(delete,  "Delete Endpoint Options are null")
            .assertNotNull(search,  "Search Endpoint Options are null")
            .assertNotNull(properties,  "Properties Options are null")
            .assertNotNull(controlledVocabulary,  "Controlled Vocabulary Options are null")
            .merge(singleGet)
            .merge(listGet)
            .merge(post)
            .merge(put)
            .merge(delete)
            .merge(search)
            .merge(properties)
            .merge(controlledVocabulary)
            .assertNotNull(pathItemNameFor,  "'pathItemNameFor' option is null") ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public RGeneratorOptions override(RGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
        }

        if (overrideOptions.singleGet != null) {
            singleGet.override(overrideOptions.getSingleGet()) ;
        }

        if (overrideOptions.listGet != null) {
            listGet.override(overrideOptions.getListGet()) ;
        }

        if (overrideOptions.post != null) {
            post.override(overrideOptions.getPost()) ;
        }

        if (overrideOptions.put != null) {
            put.override(overrideOptions.getPut()) ;
        }

        if (overrideOptions.search != null) {
            search.override(overrideOptions.getSearch()) ;
        }

        if (overrideOptions.delete != null) {
            delete.override(overrideOptions.getDelete()) ;
        }

        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.getProperties()) ;
        }

        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary.override(overrideOptions.getControlledVocabulary()) ;
        }

        if (overrideOptions.pathItemNameFor != null) {
            pathItemNameFor.putAll(overrideOptions.pathItemNameFor) ;
        }

        if (overrideOptions.pathItemNameForProperty != null) {
            overrideOptions.pathItemNameForProperty.forEach((key, value) -> {
                if (pathItemNameForProperty.containsKey(key)) {
                    pathItemNameForProperty.get(key).putAll(value) ;
                } else {
                    pathItemNameForProperty.put(key, new HashMap<>(value)) ;
                }
            });
        }

        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary = overrideOptions.controlledVocabulary ;
        }

        return this;
    }

    /**
     * Determines if the Generator should Overwrite exiting files.
     *
     * @return {@code true} if the Generator should Overwrite exiting files, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isOverwritingExistingFiles() {
        return overwrite != null && overwrite;
    }

    /**
     * Determines if the Generator should create a comment at the bottom of the SQL for each file
     *
     * @return {@code true} if the Generator should create a comment at the bottom of the SQL for each file,
     * {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isAddingGeneratorComments() {
        return addGeneratorComments != null && addGeneratorComments;
    }

    /**
     * Determines R class is generated for a specific primary model
     * @param name the name of the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String name) {
        return super.isGeneratingFor(name) && (singleGet.isGeneratingFor(name) ||
            listGet.isGeneratingFor(name) ||
            post.isGeneratingFor(name) ||
            put.isGeneratingFor(name) ||
            delete.isGeneratingFor(name) ||
            search.isGeneratingFor(name)) ;
    }

    /**
     * Determines R class is generated for a specific primary model
     * @param type the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(BrAPIType type) {
       return singleGet.isGeneratingFor(type) ||
            listGet.isGeneratingFor(type) ||
            post.isGeneratingFor(type) ||
            put.isGeneratingFor(type) ||
            delete.isGeneratingFor(type) ||
            search.isGeneratingFor(type) ;
    }

    /**
     * Gets the singular name for pluralised property name
     * @param propertyName the pluralised property name
     * @return the Pluralise name for a specific Primary Model
     */
    @JsonIgnore
    public final String getSingularForProperty(@NonNull String propertyName) {
        return toSingular(propertyName) ;
    }

    /**
     * Gets the path item name for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Pluralised name for a specific Primary Model
     */
    public final String getPathItemNameFor(String name) {
        return pathItemNameFor.getOrDefault(name, String.format("/%s", toLowerCase(getPluralFor(name))));
    }

    /**
     * Gets the path item name for a specific Primary Model
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getPathItemNameFor(BrAPIType type) {
        return getPathItemNameFor(type.getName()) ;
    }

    /**
     * Gets the search path item name for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getSearchPathItemNameFor(String name) {
        return String.format("/search%s", getPathItemNameFor(name));
    }

    /**
     * Gets the search path item name for a specific Primary Model
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getSearchPathItemNameFor(BrAPIType type) {
        return String.format("/search%s", getPathItemNameFor(type));
    }

    /**
     * Gets the path item name for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull String typeName, @NonNull String propertyName) {
        Map<String, String> map = pathItemNameForProperty.get(typeName) ;

        String defaultPathItemNameForProperty = String.format("%s/%s", getPathItemNameFor(typeName), toPlural(propertyName)) ;

        if (map != null) {
            return map.getOrDefault(propertyName, defaultPathItemNameForProperty) ;
        }

        return defaultPathItemNameForProperty ;
    }

    /**
     * Gets the path item name for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property) {
        return getPathItemNameForProperty(type.getName(), property.getName()) ;
    }

    /**
     * Gets the path item name for a specific BrAPI Property
     * @param typeWithProperty the primary model with the property
     * @return the path item name for the Property
     */
    @JsonIgnore
    public final String getPathItemNameForProperty(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return getPathItemNameForProperty(typeWithProperty.getType(), typeWithProperty.getProperty()) ;
    }

    /**
     * Sets the path item name for a specific BrAPI Property
     * @param typeName the name of the primary model
     * @param propertyName the name of the property
     * @param pathItemName the path item name
     * @return the options for chaining
     */
    @JsonIgnore
    public final RGeneratorOptions setPathItemNameForProperty(String typeName, String propertyName, String pathItemName) {
        Map<String, String> map = pathItemNameForProperty.get(typeName) ;

        if (map != null) {
            map.put(propertyName, pathItemName) ;
            return this ;
        } else {
            map = new HashMap<>() ;
            map.put(propertyName, pathItemName) ;
            pathItemNameForProperty.put(typeName, map) ;

            return this ;
        }
    }

    /**
     * Sets the path item name for a specific BrAPI Property
     * @param type the primary model
     * @param property the property
     * @param pathItemName the path item name
     * @return the options for chaining
     */
    @JsonIgnore
    public final RGeneratorOptions setPathItemNameForProperty(@NonNull BrAPIType type, @NonNull BrAPIObjectProperty property, String pathItemName) {
        return setPathItemNameForProperty(type.getName(), property.getName(), pathItemName) ;
    }

    /**
     * Determines if a specific property should be exposed as a separate Endpoint
     * @param type the Object type
     * @param property the Object type property
     * @return {@code true} generator will create a separate Endpoint for the property, {@code false} otherwise
     */
    public final boolean isGeneratingSubPathFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return properties.getLinkTypeFor(type, property).mapResult(LinkType.SUB_QUERY::equals).orElseResult(false) ;
    }

    /**
     * Gets the name of the Sub-path endpoint for a property
     * @param pathItemName the path prefix
     * @param property the Object type property
     * @return the name of the Sub-path endpoint for a property
     */
    public final String getSubPathItemNameFor(String pathItemName, BrAPIObjectProperty property) {
        return String.format("%s/%s", pathItemName, toLowerCase(property.getName())) ;
    }

    /**
     * Determines Controlled vocabulary endpoints should be generated. Any entity which as a
     * property that is indicated in the metadata that it returns a controlled vocabulary
     * will have an endpoint generated in the format /{entity-plural}/{property-name-plural}
     * for example /attributes/categories
     *
     * @return {@code true} if controlled vocabulary endpoints should be generated
     */
    public final boolean isGeneratingControlledVocabularyEndpoints() {
        return controlledVocabulary != null && controlledVocabulary.isGenerating() ;
    }
}