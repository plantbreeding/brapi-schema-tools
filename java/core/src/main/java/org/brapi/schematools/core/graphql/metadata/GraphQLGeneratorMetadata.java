package org.brapi.schematools.core.graphql.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Provides metadata for the GraphQL generation
 */
@Getter
@Setter
public class GraphQLGeneratorMetadata implements Metadata {
    private String title ;
    private String version ;

    /**
     * Load the default metadata
     * @return The default metadata
     */
    public static GraphQLGeneratorMetadata load() {
        try {
            return ConfigurationUtils.load("graphql-metadata.yaml", GraphQLGeneratorMetadata.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the metadata from a metadata file in YAML or Json. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param metadataFile The path to the metadata file in YAML or Json.
     * @return The metadata loaded from the YAML or Json file.
     * @throws IOException if the metadata file can not be found or is incorrectly formatted.
     */
    public static GraphQLGeneratorMetadata load(Path metadataFile) throws IOException {
        return load().override(ConfigurationUtils.load(metadataFile, GraphQLGeneratorMetadata.class)) ;
    }


    /**
     * Load the metadata from an metadata input stream in YAML or Json. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static GraphQLGeneratorMetadata load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, GraphQLGeneratorMetadata.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideMetadata the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public GraphQLGeneratorMetadata override(GraphQLGeneratorMetadata overrideMetadata) {
        if (overrideMetadata.title != null) {
            setTitle(overrideMetadata.title);
        }

        if (overrideMetadata.version != null) {
            setVersion(overrideMetadata.version);
        }

        return this ;
    }
}
