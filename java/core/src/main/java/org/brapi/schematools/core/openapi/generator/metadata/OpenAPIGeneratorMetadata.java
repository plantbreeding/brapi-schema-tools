package org.brapi.schematools.core.openapi.generator.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.metadata.Metadata;
import org.brapi.schematools.core.utils.ConfigurationUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides metadata for the OpenAPI generation
 */
@Getter
@Setter
@Accessors(chain = true)
public class OpenAPIGeneratorMetadata implements Metadata {
    private String title ;
    private String version ;
    @Setter(AccessLevel.PRIVATE)
    private Map<String, String> titleFor = new HashMap<>();

    private SingleGetMetadata singleGet = new SingleGetMetadata() ;
    private ListGetMetadata listGet = new ListGetMetadata() ;
    private PostMetadata post = new PostMetadata() ;
    private PutMetadata put = new PutMetadata() ;
    private DeleteMetadata delete = new DeleteMetadata() ;
    private SearchMetadata search = new SearchMetadata() ;

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
     * Load the metadata from a metadata file in YAML or JSON. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param metadataFile The path to the metadata file in YAML or JSON.
     * @return The metadata loaded from the YAML or JSON file.
     * @throws IOException if the metadata file cannot be found or is incorrectly formatted.
     */
    public static OpenAPIGeneratorMetadata load(Path metadataFile) throws IOException {
        return load().override(ConfigurationUtils.load(metadataFile, OpenAPIGeneratorMetadata.class)) ;
    }


    /**
     * Load the metadata from an metadata input stream in YAML or JSON. The metadata file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or JSON.
     * @return The metadata loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static OpenAPIGeneratorMetadata load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, OpenAPIGeneratorMetadata.class)) ;
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

        titleFor.putAll(overrideMetadata.titleFor);

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

    /**
     * Gets the specification title for a specification generated for a module or class
     * @param name the name of the module or class
     * @return the title for a specification generated for a module or class, or the default title
     */
    public String getTitleFor(String name) {
        return titleFor.getOrDefault(name, name);
    }

    /**
     * Sets the specification title for a specification generated for a module or class
     * @param name the name of the module or class
     * @param title the title for a specification generated for a module or class
     * @return the options for chaining
     */
    @JsonIgnore
    public OpenAPIGeneratorMetadata setTitleFor(String name, String title) {
        titleFor.put(name, title) ;

        return this ;
    }
}
