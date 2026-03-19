package org.brapi.schematools.core.python.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Provides metadata for the Python generation.
 */
@Getter
@Setter
public class PythonGeneratorMetadata implements Metadata {
    private String filePrefix;

    /** Output subdirectory (relative to outputPath) for the client and common Python files. */
    private String commonDirectory;

    /** Output subdirectory (relative to outputPath) for the generated entity Python files. */
    private String entitiesDirectory;

    /** Output subdirectory (relative to outputPath) for the generated Jupyter notebooks. */
    private String notebooksDirectory;

    /**
     * Load the default metadata.
     *
     * @return The default metadata
     */
    public static PythonGeneratorMetadata load() {
        try {
            return ConfigurationUtils.load("python-metadata.yaml", PythonGeneratorMetadata.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the metadata from a metadata file in YAML or JSON. The metadata file may have missing
     * (defined) values; in these cases the default values are loaded. See {@link #load()}
     *
     * @param metadataFile The path to the metadata file in YAML or JSON.
     * @return The metadata loaded from the YAML or JSON file.
     * @throws IOException if the metadata file cannot be found or is incorrectly formatted.
     */
    public static PythonGeneratorMetadata load(Path metadataFile) throws IOException {
        return load().override(ConfigurationUtils.load(metadataFile, PythonGeneratorMetadata.class));
    }

    /**
     * Load the metadata from a metadata input stream in YAML or JSON. The metadata file may have
     * missing (defined) values; in these cases the default values are loaded. See {@link #load()}
     *
     * @param inputStream The input stream in YAML or JSON.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static PythonGeneratorMetadata load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, PythonGeneratorMetadata.class));
    }

    /**
     * Overrides the values in this metadata object from the provided metadata object if they are non-null.
     *
     * @param overrideMetadata the metadata which will be used to override this metadata object
     * @return this metadata for method chaining
     */
    public PythonGeneratorMetadata override(PythonGeneratorMetadata overrideMetadata) {
        if (overrideMetadata.filePrefix != null) {
            this.filePrefix = overrideMetadata.filePrefix;
        }
        if (overrideMetadata.commonDirectory != null) {
            this.commonDirectory = overrideMetadata.commonDirectory;
        }
        if (overrideMetadata.entitiesDirectory != null) {
            this.entitiesDirectory = overrideMetadata.entitiesDirectory;
        }
        if (overrideMetadata.notebooksDirectory != null) {
            this.notebooksDirectory = overrideMetadata.notebooksDirectory;
        }
        return this;
    }
}
