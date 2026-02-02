package org.brapi.schematools.core.ontmodel.options;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractMainGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Options for the {@link OntModelGenerator}.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class OntModelGeneratorOptions extends AbstractMainGeneratorOptions {

    private String name ;

    /**
     * Load the default options
     * @return The default options
     */
    public static OntModelGeneratorOptions load() {
        try {
            OntModelGeneratorOptions options = ConfigurationUtils.load("ont-model-options.yaml", OntModelGeneratorOptions.class);

            loadBrAPISchemaReaderOptions(options) ;

            return options ;
        } catch (Exception e) { // The default options should be present on the classpath
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
    public static OntModelGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, OntModelGeneratorOptions.class));
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
    public static OntModelGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, OntModelGeneratorOptions.class));
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public OntModelGeneratorOptions override(OntModelGeneratorOptions overrideOptions) {

        super.override(overrideOptions);

        if (overrideOptions.name != null) {
            name = overrideOptions.name;
        }

        return this ;
    }
}