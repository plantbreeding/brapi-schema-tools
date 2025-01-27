package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.OpenAPIGenerator;
import org.brapi.schematools.core.options.AbstractGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.valdiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;


/**
 * Options for the {@link OpenAPIGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class OpenAPIGeneratorOptions extends AbstractGeneratorOptions {

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

    @Getter(AccessLevel.PRIVATE)
    private Boolean separateByModule;
    @Getter(AccessLevel.PRIVATE)
    private Boolean generateNewRequest;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> generateNewRequestFor = new HashMap<>();
    @Getter(AccessLevel.PRIVATE)
    private String newRequestNameFormat;
    @Getter(AccessLevel.PRIVATE)
    private String singleResponseNameFormat;
    @Getter(AccessLevel.PRIVATE)
    private String listResponseNameFormat;
    @Getter(AccessLevel.PRIVATE)
    private String searchRequestNameFormat;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> pathItemNameFor = new HashMap<>();
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> tagFor = new HashMap<>();
    /**
     * Load the default options
     * @return The default options
     */
    public static OpenAPIGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("openapi-options.yaml", OpenAPIGeneratorOptions.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static OpenAPIGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, OpenAPIGeneratorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static OpenAPIGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, OpenAPIGeneratorOptions.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this object for method chaining
     */
    public OpenAPIGeneratorOptions override(OpenAPIGeneratorOptions overrideOptions) {
        super.override(overrideOptions) ;

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

        if (overrideOptions.separateByModule != null) {
            separateByModule = overrideOptions.separateByModule ;
        }

        if (overrideOptions.generateNewRequest != null) {
            generateNewRequest = overrideOptions.generateNewRequest ;
        }

        if (overrideOptions.generateNewRequestFor != null) {
            generateNewRequestFor.putAll(overrideOptions.generateNewRequestFor) ;
        }

        if (overrideOptions.newRequestNameFormat != null) {
            newRequestNameFormat = overrideOptions.newRequestNameFormat ;
        }

        if (overrideOptions.singleResponseNameFormat != null) {
            singleResponseNameFormat = overrideOptions.singleResponseNameFormat ;
        }

        if (overrideOptions.listResponseNameFormat != null) {
            listResponseNameFormat = overrideOptions.listResponseNameFormat ;
        }

        if (overrideOptions.searchRequestNameFormat != null) {
            searchRequestNameFormat = overrideOptions.searchRequestNameFormat ;
        }

        if (overrideOptions.pathItemNameFor != null) {
            pathItemNameFor.putAll(overrideOptions.pathItemNameFor) ;
        }

        if (overrideOptions.tagFor != null) {
            tagFor.putAll(overrideOptions.tagFor) ;
        }

        return this ;
    }

    public Validation validate() {
        return super.validate()
            .assertNotNull(singleGet, "Single Get Endpoint Options are null")
            .assertNotNull(listGet,  "List Get Endpoint Options are null")
            .assertNotNull(post, "Post Endpoint Options are null")
            .assertNotNull(put, "Put Endpoint Options are null")
            .assertNotNull(delete,  "Delete Endpoint Options are null")
            .assertNotNull(search,  "Search Endpoint Options are null")
            .assertNotNull(properties,  "Id Options are null")
            .assertNotNull(separateByModule, "'separateByModule' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(generateNewRequest, "'generateNewRequest' option on %s is null", this.getClass().getSimpleName())
            .merge(singleGet)
            .merge(listGet)
            .merge(post)
            .merge(put)
            .merge(delete)
            .merge(search)
            .merge(properties)
            .assertNotNull(generateNewRequestFor, "'generateNewRequestFor' option is null")
            .assertNotNull(newRequestNameFormat,  "'newRequestNameFormat' option is null")
            .assertNotNull(singleResponseNameFormat, "'singleResponseNameFormat' option is null")
            .assertNotNull(listResponseNameFormat, "'listResponseNameFormat' option is null")
            .assertNotNull(searchRequestNameFormat,  "'searchRequestNameFormat' option is null")
            .assertNotNull(pathItemNameFor,  "'pathItemNameFor' option is null")
            .assertNotNull(tagFor,  "'tagFor' option is null") ;
    }

    /**
     * Determines if the Generator should generate a separate specification per module.
     * @return <code>true</code> if the Generator should generate a separate specification per module, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isSeparatingByModule() {
        return separateByModule ;
    }

    /**
     * Determines if the Generator should generate any Endpoints without an ID parameter. Returns <code>true</code> if
     * {@link ListGetOptions#isGenerating()} or {@link PostOptions#isGenerating()} or {@link PutOptions#isGenerating()}  is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate any Endpoints without an ID parameter, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpoint() {
        return listGet.isGenerating() || post.isGenerating() || put.isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link ListGetOptions#isGeneratingFor(String)} or {@link PostOptions#isGeneratingFor(String)} is set to <code>true</code>
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointFor(@NonNull String name) {
        return listGet.isGeneratingFor(name) || post.isGeneratingFor(name) || put.isGeneratingEndpointFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link ListGetOptions#isGeneratingFor(String)} or {@link PostOptions#isGeneratingFor(String)} is set to <code>true</code>
     * @param type the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointFor(@NonNull BrAPIType type) {
        return isGeneratingEndpointFor(type.getName()) ;
    }

    /**
     * Determines if the Generator should generate any Endpoints with an ID parameter. Returns <code>true</code> if
     * {@link SingleGetOptions#isGenerating()} or {@link PutOptions#isGenerating()} or
     * {@link DeleteOptions#isGenerating()} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate any Endpoints without an ID parameter, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointWithId() {
        return singleGet.isGenerating() || put.isGenerating() || delete.isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link SingleGetOptions#isGeneratingFor(String)} or {@link PutOptions#isGeneratingEndpointNameWithIdFor(String)} or
     * {@link DeleteOptions#isGeneratingFor(String)}is set to <code>true</code>
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointNameWithIdFor(@NonNull String name) {
        return singleGet.isGeneratingFor(name) || put.isGeneratingEndpointNameWithIdFor(name) || delete.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link SingleGetOptions#isGeneratingFor(String)} or {@link PutOptions#isGeneratingEndpointNameWithIdFor(String)} or
     * {@link DeleteOptions#isGeneratingFor(String)}is set to <code>true</code>
     * @param type the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull BrAPIType type) {
        return isGeneratingEndpointNameWithIdFor(type.getName()) ;
    }

    /**
     * Determines if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model.
     * For example if set to <code>true</code> for the model 'Study' the generator will create the NewStudyRequest schema and the 'Study' schema,
     * whereas if set <code>false</code> generator will create only create the 'Study' schema
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingNewRequestFor(@NonNull String name) {
        return generateNewRequestFor.getOrDefault(name, generateNewRequest) ;
    }

    /**
     * Determines if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model.
     * For example if set to <code>true</code> for the model 'Study' the generator will create the NewStudyRequest schema and the 'Study' schema,
     * whereas if set <code>false</code> generator will create only create the 'Study' schema
     * @param type the Primary Model
     * @return <code>true</code> if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingNewRequestFor(@NonNull BrAPIType type) {
        return isGeneratingNewRequestFor(type.getName()) ;
    }

    /**
     * Gets the name for the NewRequest schema for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the NewRequest schema name for a specific Primary Model
     */
    @JsonIgnore
    public String getNewRequestNameFor(@NonNull String name) {
        return String.format(newRequestNameFormat, name) ;
    }

    /**
     * Gets the name for the NewRequest schema for a specific Primary Model
     * @param type the Primary Model
     * @return the NewRequest schema name for a specific Primary Model
     */
    @JsonIgnore
    public final String getNewRequestNameFor(@NonNull BrAPIType type) {
        return getNewRequestNameFor(type.getName()) ;
    }

    /**
     * Gets the name for the Single Response schema for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Single Response schema name for a specific Primary Model
     */
    @JsonIgnore
    public String getSingleResponseNameFor(@NonNull String name) {
        return String.format(singleResponseNameFormat, name) ;
    }

    /**
     * Gets the name for the Single Response schema for a specific Primary Model
     * @param type the Primary Model
     * @return the Single Response schema name for a specific Primary Model
     */
    @JsonIgnore
    public final String getSingleResponseNameFor(@NonNull BrAPIType type) {
        return getSingleResponseNameFor(type.getName()) ;
    }

    /**
     * Gets the name for the List Response for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the List Response name for a specific Primary Model
     */
    @JsonIgnore
    public final String getListResponseNameFor(@NonNull String name) {
        return String.format(listResponseNameFormat, name) ;
    }

    /**
     * Gets the name for the List Response for a specific Primary Model
     * @param type the Primary Model
     * @return the List Response name for a specific Primary Model
     */
    @JsonIgnore
    public final String getListResponseNameFor(@NonNull BrAPIType type) {
        return getListResponseNameFor(type.getName()) ;
    }

    /**
     * Gets the name for the Search Request schema for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Search Request schema name for a specific Primary Model
     */
    @JsonIgnore
    public final String getSearchRequestNameFor(@NonNull String name) {
        return String.format(searchRequestNameFormat, name) ;
    }

    /**
     * Gets the name for the Search Request schema for a specific Primary Model
     * @param type the Primary Model
     * @return the Search Request schema name for a specific Primary Model
     */
    @JsonIgnore
    public final String getSearchRequestNameFor(@NonNull BrAPIType type) {
        return getSearchRequestNameFor(type.getName()) ;
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
    public String getPathItemNameFor(String name) {
        return pathItemNameFor.getOrDefault(name, String.format("/%s", toParameterCase(getPluralFor(name))));
    }

    /**
     * Gets the path item name for a specific Primary Model
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public String getPathItemNameFor(BrAPIType type) {
        return pathItemNameFor.getOrDefault(type.getName(), String.format("/%s", toParameterCase(getPluralFor(type))));
    }

    /**
     * Gets the path item with id name for a specific Primary Model
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public String getPathItemWithIdNameFor(BrAPIType type) {
        return String.format("%s/{%s}", getPathItemNameFor(type), properties.getIdPropertyNameFor(type));
    }

    /**
     * Gets the tag name for a specific Primary Model. If not set {@link #getPluralFor(String)} will be used.
     * Use {@link #setTagFor(String, String)} to override this value.
     * @param name the name of the Primary Model
     * @return the tag name for a specific Primary Model
     */
    @JsonIgnore
    public final String getTagFor(@NonNull String name) {
        return tagFor.getOrDefault(name, getPluralFor(name)) ;
    }

    /**
     * Gets the tag name for a specific Primary Model. If not set {@link #getPluralFor(String)} will be used.
     * Use {@link #setTagFor(String, String)} to override this value.
     * @param type the Primary Model
     * @return the tag name for a specific Primary Model
     */
    @JsonIgnore
    public final String getTagFor(@NonNull BrAPIType type) {
        return getTagFor(type.getName()) ;
    }

    /**
     * Sets the tag name for a specific primary model.
     * @param name the name of the primary model
     * @param tagName the tag name for a specific primary model.
     * @return the options for chaining
     */
    @JsonIgnore
    public AbstractGeneratorOptions setTagFor(String name, String tagName) {
        tagFor.put(name, tagName) ;

        return this ;
    }
}
