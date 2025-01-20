package org.brapi.schematools.core.openapi.metadata;

import lombok.Getter;
import lombok.Setter;
import org.brapi.schematools.core.graphql.metadata.GraphQLGeneratorMetadata;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Provides metadata for the OpenAPI generation
 */
@Getter
@Setter
public class OpenAPIGeneratorMetadata implements Metadata {
    private String title ;
    private String version ;

    private SingleGetMetadata singleGet = new SingleGetMetadata() ;
    private ListGetMetadata listGet = new ListGetMetadata() ;
    private PostMetadata post = new PostMetadata() ;
    private PutMetadata put = new PutMetadata() ;
    private DeleteMetadata delete = new DeleteMetadata() ;
    private SearchMetadata search = new SearchMetadata() ;

    /**
     * Load the metadata from a metadata file in YAML or Json. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param metadataFile The path to the metadata file in YAML or Json.
     * @return The metadata loaded from the YAML or Json file.
     * @throws IOException if the metadata file can not be found or is incorrectly formatted.
     */
    public static OpenAPIGeneratorMetadata load(Path metadataFile) throws IOException {
        return ConfigurationUtils.load(metadataFile, OpenAPIGeneratorMetadata.class) ;
    }

    /**
     * Load the default metadata
     * @return The default metadata
     */
    public static OpenAPIGeneratorMetadata load() {
        try {
            return ConfigurationUtils.load("openapi-metadata.yaml", OpenAPIGeneratorMetadata.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the metadata from an metadata input stream in YAML or Json. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static OpenAPIGeneratorMetadata load(InputStream inputStream) throws IOException {
        return ConfigurationUtils.load(inputStream, OpenAPIGeneratorMetadata.class) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideMetadata the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public OpenAPIGeneratorMetadata override(OpenAPIGeneratorMetadata overrideMetadata) {
        if (overrideMetadata.title != null) {
            setTitle(overrideMetadata.title);
        }

        if (overrideMetadata.version != null) {
            setVersion(overrideMetadata.version);
        }

        if (overrideMetadata.singleGet != null) {
            singleGet.override(overrideMetadata.getSingleGet()) ;
        }

        if (overrideMetadata.listGet != null) {
            listGet.override(overrideMetadata.getListGet()) ;
        }

        if (overrideMetadata.post != null) {
            post.override(overrideMetadata.getPost()) ;
        }

        if (overrideMetadata.put != null) {
            put.override(overrideMetadata.getPut()) ;
        }

        if (overrideMetadata.search != null) {
            search.override(overrideMetadata.getSearch()) ;
        }

        return this ;
    }
}
