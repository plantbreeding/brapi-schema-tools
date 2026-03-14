package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.options.AbstractRESTGeneratorOptions;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.PropertiesOptions;
import org.brapi.schematools.core.python.PythonGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.brapi.schematools.core.utils.StringUtils.toSingular;

/**
 * Options for the {@link PythonGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class PythonGeneratorOptions extends AbstractRESTGeneratorOptions {

    private String entitiesDirectory;

    @Setter(AccessLevel.PRIVATE)
    private SingleGetOptions singleGet;
    @Setter(AccessLevel.PRIVATE)
    private ListGetOptions listGet;
    @Setter(AccessLevel.PRIVATE)
    private TableOptions table;
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

    /**
     * Load the default options.
     *
     * @return The default options
     */
    public static PythonGeneratorOptions load() {
        try {
            PythonGeneratorOptions options = ConfigurationUtils.load("python-options.yaml", PythonGeneratorOptions.class);
            loadBrAPISchemaReaderOptions(options);
            return options;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or JSON.
     *
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static PythonGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, PythonGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or JSON.
     *
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static PythonGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, PythonGeneratorOptions.class));
    }

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(entitiesDirectory, "'entitiesDirectory' option is null")
            .assertNotNull(singleGet, "Single Get Endpoint Options are null")
            .assertNotNull(listGet, "List Get Endpoint Options are null")
            .assertNotNull(post, "Post Endpoint Options are null")
            .assertNotNull(put, "Put Endpoint Options are null")
            .assertNotNull(delete, "Delete Endpoint Options are null")
            .assertNotNull(search, "Search Endpoint Options are null")
            .assertNotNull(properties, "Properties Options are null")
            .assertNotNull(controlledVocabulary, "Controlled Vocabulary Options are null")
            .merge(singleGet)
            .merge(listGet)
            .merge(post)
            .merge(put)
            .merge(delete)
            .merge(search)
            .merge(properties)
            .merge(controlledVocabulary);
    }

    /** {@inheritDoc} */
    @Override
    public PythonGeneratorOptions setOverwrite(Boolean overwrite) {
        super.setOverwrite(overwrite);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public PythonGeneratorOptions setAddGeneratorComments(Boolean addGeneratorComments) {
        super.setAddGeneratorComments(addGeneratorComments);
        return this;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public PythonGeneratorOptions override(PythonGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.entitiesDirectory != null) {
            entitiesDirectory = overrideOptions.entitiesDirectory;
        }
        if (overrideOptions.singleGet != null) {
            singleGet.override(overrideOptions.getSingleGet());
        }
        if (overrideOptions.listGet != null) {
            listGet.override(overrideOptions.getListGet());
        }
        if (overrideOptions.post != null) {
            post.override(overrideOptions.getPost());
        }
        if (overrideOptions.put != null) {
            put.override(overrideOptions.getPut());
        }
        if (overrideOptions.search != null) {
            search.override(overrideOptions.getSearch());
        }
        if (overrideOptions.delete != null) {
            delete.override(overrideOptions.getDelete());
        }
        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.getProperties());
        }
        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary.override(overrideOptions.getControlledVocabulary());
        }

        return this;
    }

    /**
     * Determines if a Python class is generated for a specific primary model.
     *
     * @param name the name of the primary model
     * @return {@code true} if a class is generated for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String name) {
        return super.isGeneratingFor(name) && (singleGet.isGeneratingFor(name) ||
            listGet.isGeneratingFor(name) ||
            post.isGeneratingFor(name) ||
            put.isGeneratingFor(name) ||
            delete.isGeneratingFor(name) ||
            search.isGeneratingFor(name));
    }

    /**
     * Determines if a Python class is generated for a specific primary model.
     *
     * @param type the primary model
     * @return {@code true} if a class is generated for the primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(BrAPIType type) {
        return singleGet.isGeneratingFor(type) ||
            listGet.isGeneratingFor(type) ||
            post.isGeneratingFor(type) ||
            put.isGeneratingFor(type) ||
            delete.isGeneratingFor(type) ||
            search.isGeneratingFor(type);
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

    @Override
    public final boolean isGeneratingSubPathFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return properties.getLinkTypeFor(type, property)
            .mapResult(LinkType.SUB_QUERY::equals).orElseResult(false);
    }

    @Override
    public final boolean isGeneratingControlledVocabularyEndpoints() {
        return controlledVocabulary != null && controlledVocabulary.isGenerating();
    }
}
