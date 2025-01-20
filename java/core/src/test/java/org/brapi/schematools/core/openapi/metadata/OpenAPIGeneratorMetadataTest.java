package org.brapi.schematools.core.openapi.metadata;

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
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("openapi-test-metadata.json").toURI()));
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
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("openapi-test-metadata.yaml").toURI()));
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
            metadata = OpenAPIGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("openapi-override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("1.2.3", metadata.getVersion());
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