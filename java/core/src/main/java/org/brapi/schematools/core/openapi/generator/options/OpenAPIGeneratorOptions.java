package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.model.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.openapi.generator.OpenAPIGenerator;
import org.brapi.schematools.core.options.AbstractGeneratorOptions;
import org.brapi.schematools.core.options.AbstractRESTGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toSentenceCase;


/**
 * Options for the {@link OpenAPIGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class OpenAPIGeneratorOptions extends AbstractRESTGeneratorOptions {
    @Getter(AccessLevel.PUBLIC)
    private String supplementalSpecification;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> supplementalSpecificationFor = new HashMap<>();
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
    private Map<String, String> tagFor = new HashMap<>();

    /**
     * Load the default options
     * @return The default options
     */
    public static OpenAPIGeneratorOptions load() {
        try {
            OpenAPIGeneratorOptions options = ConfigurationUtils.load("openapi-options.yaml", OpenAPIGeneratorOptions.class);

            loadBrAPISchemaReaderOptions(options) ;

            return options ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static OpenAPIGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, OpenAPIGeneratorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or JSON.
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

        if (overrideOptions.separateByModule != null) {
            separateByModule = overrideOptions.separateByModule ;
        }

        if (overrideOptions.generateNewRequest != null) {
            generateNewRequest = overrideOptions.generateNewRequest ;
        }

        if (overrideOptions.supplementalSpecification != null) {
            supplementalSpecification = overrideOptions.supplementalSpecification ;
        }

        if (overrideOptions.supplementalSpecificationFor != null) {
            supplementalSpecificationFor.putAll(overrideOptions.supplementalSpecificationFor) ;
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

        if (overrideOptions.tagFor != null) {
            tagFor.putAll(overrideOptions.tagFor) ;
        }

        return this ;
    }

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(separateByModule, "'separateByModule' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(generateNewRequest, "'generateNewRequest' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(supplementalSpecification, "'supplementalSpecification' option is null")
            .assertNotNull(supplementalSpecificationFor, "'supplementalSpecificationFor' option is null")
            .assertNotNull(generateNewRequestFor, "'generateNewRequestFor' option is null")
            .assertNotNull(newRequestNameFormat,  "'newRequestNameFormat' option is null")
            .assertNotNull(singleResponseNameFormat, "'singleResponseNameFormat' option is null")
            .assertNotNull(listResponseNameFormat, "'listResponseNameFormat' option is null")
            .assertNotNull(searchRequestNameFormat,  "'searchRequestNameFormat' option is null")
            .assertNotNull(tagFor,  "'tagFor' option is null") ;
    }

    /**
     * Determines if the Generator should generate a separate specification per module.
     * @return {@code true} if the Generator should generate a separate specification per module, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isSeparatingByModule() {
        return separateByModule ;
    }

    /**
     * Determines if the Generator should generate any Endpoints without an ID parameter. Returns {@code true} if
     * {@link ListGetOptions#isGenerating()} or {@link org.brapi.schematools.core.options.PostOptions#isGenerating()} or {@link org.brapi.schematools.core.options.PutOptions#isGenerating()}  is set to {@code true}
     * @return {@code true} if the Generator should generate any Endpoints without an ID parameter, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpoint() {
        return getListGet().isGenerating() || getPost().isGenerating() || getPut().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model. Returns {@code true} if
     * {@link org.brapi.schematools.core.options.ListGetOptions#isGeneratingFor(String)} or {@link org.brapi.schematools.core.options.PostOptions#isGeneratingFor(String)}
     *  or {@link org.brapi.schematools.core.options.PutOptions#isGeneratingFor(String)} is set to {@code true}
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointFor(@NonNull String name) {
        return getListGet().isGeneratingFor(name) || getPost().isGeneratingFor(name) || getPut().isGeneratingEndpointFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model. Returns {@code true} if
     * {@link #isGeneratingEndpointFor(String)} returns {@code true}
     * @param type the Primary Model
     * @return {@code true} if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointFor(@NonNull BrAPIType type) {
        return isGeneratingEndpointFor(type.getName()) ;
    }

    /**
     * Determines if the Generator should generate any Endpoints with an ID parameter. Returns {@code true} if
     * {@link org.brapi.schematools.core.options.SingleGetOptions#isGenerating()} or {@link org.brapi.schematools.core.options.PutOptions#isGenerating()} or
     * {@link org.brapi.schematools.core.options.DeleteOptions#isGenerating()} is set to {@code true}
     * @return {@code true} if the Generator should generate any Endpoints without an ID parameter, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointWithId() {
        return getSingleGet().isGenerating() || getPut().isGenerating() || getDelete().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model. Returns {@code true} if
     * {@link org.brapi.schematools.core.options.SingleGetOptions#isGeneratingFor(String)} or {@link org.brapi.schematools.core.options.PutOptions#isGeneratingEndpointNameWithIdFor(String)} or
     * {@link org.brapi.schematools.core.options.DeleteOptions#isGeneratingFor(String)} is set to {@code true}
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull String name) {
        return getSingleGet().isGeneratingFor(name) || getPut().isGeneratingEndpointNameWithIdFor(name) || getDelete().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model. Returns {@code true} if
     * {@link #isGeneratingEndpointNameWithIdFor(String)} returns {@code true}
     * @param type the Primary Model
     * @return {@code true} if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingEndpointNameWithIdFor(@NonNull BrAPIType type) {
        return isGeneratingEndpointNameWithIdFor(type.getName()) ;
    }


    @JsonIgnore
    public final String getSupplementalSpecificationFor(@NonNull String name) {
        return supplementalSpecificationFor.getOrDefault(name, supplementalSpecification) ;
    }

    /**
     * Determines if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model.
     * For example if set to {@code true} for the model 'Study' the generator will create the NewStudyRequest schema and the 'Study' schema,
     * whereas if set {@code false} generator will create only create the 'Study' schema
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingNewRequestFor(@NonNull String name) {
        return generateNewRequestFor.getOrDefault(name, generateNewRequest) ;
    }

    /**
     * Determines if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model.
     * For example if set to {@code true} for the model 'Study' the generator will create the NewStudyRequest schema and the 'Study' schema,
     * whereas if set {@code false} generator will create only create the 'Study' schema
     * @param type the Primary Model
     * @return {@code true} if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model, {@code false} otherwise
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
    public final String getNewRequestNameFor(@NonNull String name) {
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
    public final String getSingleResponseNameFor(@NonNull String name) {
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
     * Gets the name for the List Response for a specific Primary Model and property
     * @param typeWithProperty the Primary Model and the Property
     * @return the List Response name for a specific Primary Model
     */
    @JsonIgnore
    public final String getListResponseNameFor(@NonNull BrAPIObjectTypeWithProperty typeWithProperty) {
        return String.format(listResponseNameFormat, String.format("%s%s", typeWithProperty.getType().getName(), toSentenceCase(typeWithProperty.getProperty().getName()))) ;
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
     * Gets the path item with id name for a specific Primary Model
     * @param type the Primary Model
     * @return the path item name for a specific Primary Model
     */
    public final String getPathItemWithIdNameFor(BrAPIType type) {
        return String.format("%s/{%s}", getPathItemNameFor(type), getProperties().getIdPropertyNameFor(type));
    }

    /**
     * Gets the tag name for a specific Primary Model.
     * @param name the name of the Primary Model
     * @return the tag name for a specific Primary Model
     */
    @JsonIgnore
    public final String getTagFor(@NonNull String name) {
        return tagFor.getOrDefault(name, getPluralFor(name)) ;
    }

    /**
     * Gets the tag name for a specific Primary Model.
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
    public final AbstractGeneratorOptions setTagFor(String name, String tagName) {
        tagFor.put(name, tagName) ;

        return this ;
    }
}
