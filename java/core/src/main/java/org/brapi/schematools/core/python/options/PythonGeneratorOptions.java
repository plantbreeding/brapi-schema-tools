package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractRESTGeneratorOptions;
import org.brapi.schematools.core.python.PythonGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

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

    /** When {@code true} a Jupyter notebook is generated for each primary entity. */
    private Boolean generateNotebooks;

    /**
     * Output directory for generated notebooks, relative to {@code outputPath}.
     * Defaults to {@code ../../notebooks} (project-root notebooks/ when outputPath is src/brapi/).
     */
    private String notebooksDirectory;

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
            .assertNotNull(entitiesDirectory, "'entitiesDirectory' option is null");
    }

    /**
     * Determines if the Generator should generate Jupyter notebooks.
     *
     * @return {@code true} if notebooks should be generated, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGeneratingNotebooks() {
        return Boolean.TRUE.equals(generateNotebooks);
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
        if (overrideOptions.generateNotebooks != null) {
            generateNotebooks = overrideOptions.generateNotebooks;
        }
        if (overrideOptions.notebooksDirectory != null) {
            notebooksDirectory = overrideOptions.notebooksDirectory;
        }
        return this;
    }

}
