package org.brapi.schematools.core.openapi.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIGeneratorMetadataTest {

    void load() {
        OpenAPIGeneratorMetadata metadata = OpenAPIGeneratorMetadata.load();

        checkMetadata(metadata);
    }

    @Test
    void loadJson() {
        OpenAPIGeneratorMetadata metadata = null;
        try {
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("options/openapi-test-metadata.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadYaml() {
        OpenAPIGeneratorMetadata metadata = null;
        try {
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("options/openapi-test-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void overwrite() {
        OpenAPIGeneratorMetadata metadata = null;
        try {
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("options/openapi-override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("1.2.3", metadata.getVersion());
    }

    @Test
    void compare() {
        try {
            OpenAPIGeneratorMetadata options1 = OpenAPIGeneratorMetadata.load() ;
            OpenAPIGeneratorMetadata options2 = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("options/openapi-no-override-metadata.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultMetadata(OpenAPIGeneratorMetadata metadata) {
        checkMetadata(metadata);

        assertEquals("0.0.0", metadata.getVersion());
    }
    private void checkMetadata(OpenAPIGeneratorMetadata metadata) {
        assertNotNull(metadata);
        assertEquals("BrAPI", metadata.getTitle());
    }
}