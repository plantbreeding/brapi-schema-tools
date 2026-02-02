package org.brapi.schematools.core.brapischema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.networknt.schema.SpecVersion;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.stream.Collectors;
/**
 * Options for the {@link BrAPISchemaReaderOptions}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class BrAPISchemaReaderOptions implements Options {

    private String specVersion;
    private Boolean ignoreDuplicateProperties ;
    private Boolean warnAboutDuplicateProperties;

    /**
     * Load the default options
     * @return The default options
     */
    public static BrAPISchemaReaderOptions load() {
        try {
            return ConfigurationUtils.load("brapi-reader-options.yaml", BrAPISchemaReaderOptions.class) ;
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
    public static BrAPISchemaReaderOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, BrAPISchemaReaderOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static BrAPISchemaReaderOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, BrAPISchemaReaderOptions.class)) ;
    }

    @Override
    public Validation validate() {
        return Validation.valid()
            .assertNotNull(specVersion, "'specVersion' option on %s is null", this.getClass().getSimpleName())
            .assertOneOf(specVersion, Arrays.stream(SpecVersion.VersionFlag.values()).map(SpecVersion.VersionFlag::name).collect(Collectors.toList()),
                String.format("'specVersion' option not valid on %s, '%s' is not a supported version, supported versions are: %s",
                this.getClass().getSimpleName(),
                    specVersion,
                String.join(", ", Arrays.stream(SpecVersion.VersionFlag.values()).map(SpecVersion.VersionFlag::name).collect(Collectors.toSet())))) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public BrAPISchemaReaderOptions override(BrAPISchemaReaderOptions overrideOptions) {
        if (overrideOptions.specVersion != null) {
            specVersion = overrideOptions.specVersion ;
        }

        if (overrideOptions.ignoreDuplicateProperties != null) {
            ignoreDuplicateProperties = overrideOptions.ignoreDuplicateProperties ;
        }


        if (overrideOptions.warnAboutDuplicateProperties != null) {
            warnAboutDuplicateProperties = overrideOptions.warnAboutDuplicateProperties ;
        }


        return this ;
    }

    /**
     * Determines if the duplicate Properties are ignored, with only the first being used.
     *
     * @return {@code true} if the Reader should ignore duplicate Properties, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isIgnoringDuplicateProperties() {
        return ignoreDuplicateProperties != null && ignoreDuplicateProperties ;
    }

    /**
     * Determines if a warning is logged when duplicate Properties are ignored.
     *
     * @return {@code true} if the Reader should ignore duplicate Properties, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isWarningAboutDuplicateProperties() {
        return warnAboutDuplicateProperties != null && warnAboutDuplicateProperties ;
    }
}
