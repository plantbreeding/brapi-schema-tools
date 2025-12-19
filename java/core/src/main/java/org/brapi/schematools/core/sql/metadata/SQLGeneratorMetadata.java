package org.brapi.schematools.core.sql.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Provides metadata for the RDF Graph generation
 */
@Getter
@Setter
public class SQLGeneratorMetadata implements Metadata {
    private String tablePrefix ;

    /**
     * Load the default metadata
     * @return The default metadata
     */
    public static SQLGeneratorMetadata load() {
        try {
            return ConfigurationUtils.load("sql-metadata.yaml", SQLGeneratorMetadata.class) ;
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
    public static SQLGeneratorMetadata load(Path metadataFile) throws IOException {
        return load().override(ConfigurationUtils.load(metadataFile, SQLGeneratorMetadata.class)) ;
    }

    /**
     * Load the metadata from a metadata input stream in YAML or JSON. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static SQLGeneratorMetadata load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, SQLGeneratorMetadata.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideMetadata the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public SQLGeneratorMetadata override(SQLGeneratorMetadata overrideMetadata) {
        if (overrideMetadata.tablePrefix != null) {
            setTablePrefix(overrideMetadata.tablePrefix);
        }

        return this ;
    }
}
