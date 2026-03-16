package org.brapi.schematools.core.r6.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.generator.BrAPIObjectTypeWithProperty;
import org.brapi.schematools.core.options.AbstractRESTGeneratorOptions;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.ListGetOptions;
import org.brapi.schematools.core.r6.RGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

import static org.brapi.schematools.core.utils.StringUtils.toSingular;

/**
 * Options for the {@link RGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class RGeneratorOptions extends AbstractRESTGeneratorOptions {

    @Setter(AccessLevel.PRIVATE)
    private ListGetOptions listGet;
    @Setter(AccessLevel.PRIVATE)
    private ControlledVocabularyOptions controlledVocabulary;

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static RGeneratorOptions load() {
        try {
            RGeneratorOptions options = ConfigurationUtils.load("r-options.yaml", RGeneratorOptions.class);
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
    public static RGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, RGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or JSON.
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
            .assertNotNull(listGet, "List Get Endpoint Options are null")
            .assertNotNull(controlledVocabulary, "Controlled Vocabulary Options are null")
            .merge(listGet)
            .merge(controlledVocabulary);
    }

    /** {@inheritDoc} */
    @Override
    public RGeneratorOptions setOverwrite(Boolean overwrite) {
        super.setOverwrite(overwrite);
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public RGeneratorOptions setAddGeneratorComments(Boolean addGeneratorComments) {
        super.setAddGeneratorComments(addGeneratorComments);
        return this;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public RGeneratorOptions override(RGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.listGet != null) {
            listGet.override(overrideOptions.listGet);
        }
        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary.override(overrideOptions.getControlledVocabulary());
        }

        return this;
    }

    /**
     * Determines R class is generated for a specific primary model
     * @param name the name of the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull String name) {
        return super.isGeneratingFor(name) && (getSingleGet().isGeneratingFor(name) ||
            listGet.isGeneratingFor(name) ||
            getPost().isGeneratingFor(name) ||
            getPut().isGeneratingFor(name) ||
            getDelete().isGeneratingFor(name) ||
            getSearch().isGeneratingFor(name));
    }

    /**
     * Determines R class is generated for a specific primary model
     * @param type the primary model
     * @return {@code true} if the Endpoint/Query/Mutation is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(BrAPIType type) {
        return getSingleGet().isGeneratingFor(type) ||
            listGet.isGeneratingFor(type) ||
            getPost().isGeneratingFor(type) ||
            getPut().isGeneratingFor(type) ||
            getDelete().isGeneratingFor(type) ||
            getSearch().isGeneratingFor(type);
    }

    /**
     * Gets the singular name for a pluralised property name
     * @param propertyName the pluralised property name
     * @return the singular name for the property
     */
    @JsonIgnore
    public final String getSingularForProperty(@NonNull String propertyName) {
        return toSingular(propertyName);
    }

    /**
     * Determines if a specific property should be exposed as a separate Endpoint
     * @param type the Object type
     * @param property the Object type property
     * @return {@code true} generator will create a separate Endpoint for the property, {@code false} otherwise
     */
    @Override
    public final boolean isGeneratingSubPathFor(BrAPIObjectType type, BrAPIObjectProperty property) {
        return getProperties().getLinkTypeFor(type, property).mapResult(LinkType.SUB_QUERY::equals).orElseResult(false);
    }

    /**
     * Determines Controlled vocabulary endpoints should be generated. Any entity which as a
     * property that is indicated in the metadata that it returns a controlled vocabulary
     * will have an endpoint generated in the format /{entity-plural}/{property-name-plural}
     * for example /attributes/categories
     *
     * @return {@code true} if controlled vocabulary endpoints should be generated
     */
    @Override
    public final boolean isGeneratingControlledVocabularyEndpoints() {
        return controlledVocabulary != null && controlledVocabulary.isGenerating();
    }
}