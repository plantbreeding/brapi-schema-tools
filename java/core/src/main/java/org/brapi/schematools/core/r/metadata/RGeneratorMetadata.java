package org.brapi.schematools.core.r.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Provides metadata for the SQL generation
 */
@Getter
@Setter
public class RGeneratorMetadata implements Metadata {
    private String filePrefix ;

    /**
     * Load the default metadata
     * @return The default metadata
     */
    public static RGeneratorMetadata load() {
        try {
            return ConfigurationUtils.load("r-metadata.yaml", RGeneratorMetadata.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the metadata from a metadata file in YAML or JSON. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param metadataFile The path to the metadata file in YAML or JSON.
     * @return The metadata loaded from the YAML or JSON file.
     * @throws IOException if the metadata file cannot be found or is incorrectly formatted.
     */
    public static RGeneratorMetadata load(Path metadataFile) throws IOException {
        return load().override(ConfigurationUtils.load(metadataFile, RGeneratorMetadata.class)) ;
    }

    /**
     * Load the metadata from a metadata input stream in YAML or JSON. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static RGeneratorMetadata load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, RGeneratorMetadata.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideMetadata the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public RGeneratorMetadata override(RGeneratorMetadata overrideMetadata) {
        if (overrideMetadata.filePrefix != null) {
            setFilePrefix(overrideMetadata.filePrefix);
        }

        return this ;
    }
}
