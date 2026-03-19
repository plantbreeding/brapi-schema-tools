package org.brapi.schematools.core.r6.options;

import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractRESTGeneratorOptions;
import org.brapi.schematools.core.r6.RGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Options for the {@link RGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class RGeneratorOptions extends AbstractRESTGeneratorOptions {

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
        return super.validate();
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

        return this;
    }

}

