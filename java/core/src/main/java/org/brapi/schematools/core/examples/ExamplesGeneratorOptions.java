package org.brapi.schematools.core.examples;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
//@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class ExamplesGeneratorOptions implements Options {

    /**
     * Load the default options
     * @return The default options
     */
    public static ExamplesGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("examples-options.yaml", ExamplesGeneratorOptions.class) ;
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
    public static ExamplesGeneratorOptions load(Path optionsFile) throws IOException {
        return ConfigurationUtils.load(optionsFile, ExamplesGeneratorOptions.class) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static ExamplesGeneratorOptions load(InputStream inputStream) throws IOException {
        return ConfigurationUtils.load(inputStream, ExamplesGeneratorOptions .class) ;
    }
}
