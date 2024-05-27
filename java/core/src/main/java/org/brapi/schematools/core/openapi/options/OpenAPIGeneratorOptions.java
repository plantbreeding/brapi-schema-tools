package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;
import org.brapi.schematools.core.graphql.options.IdsOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;


@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenAPIGeneratorOptions {

    SingleGetOptions singleGet ;
    ListGetOptions listGet ;
    PostOptions post ;
    PutOptions put ;
    DeleteOptions delete ;
    IdsOptions ids;

    public static OpenAPIGeneratorOptions load(Path optionsFile) throws IOException {
        return load(Files.newInputStream(optionsFile));
    }

    public static OpenAPIGeneratorOptions load() {

        try {
            InputStream inputStream = OpenAPIGeneratorOptions.class
                .getClassLoader()
                .getResourceAsStream("openapi-options.yaml");
            return load(inputStream);
        } catch (Exception e) { // The default options should be present on the classpath
            throw new RuntimeException(e);
        }
    }

    public boolean isGeneratingSingleGet() {
        return singleGet != null && singleGet.isGenerating() ;
    }

    public boolean isGeneratingListGet() {
        return listGet != null && listGet.isGenerating() ;
    }

    public boolean isGeneratingPost() {
        return post != null && post.isGenerating() ;
    }

    public boolean isGeneratingPut() {
        return put != null && put.isGenerating() ;
    }

    public boolean isGeneratingDelete() {
        return delete != null && delete.isGenerating() ;
    }

    public boolean isGeneratingEndpoint() {
        return isGeneratingListGet() || isGeneratingPost() ;
    }
    public boolean isGeneratingEndpointNameWithId(){
        return isGeneratingSingleGet() || isGeneratingPut() || isGeneratingDelete() ;
    }

    private static OpenAPIGeneratorOptions load(InputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        return mapper.readValue(inputStream, OpenAPIGeneratorOptions.class);
    }
}
