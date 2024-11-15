package org.brapi.schematools.core.openapi.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OpenAPIGeneratorMetadataTest {

    void load() {
        OpenAPIGeneratorMetadata metadata = OpenAPIGeneratorMetadata.load();

        checkMetadata(metadata);
    }

    @Test
    void loadJson() {
    }

    @Test
    void loadYaml() {
    }

    private void checkMetadata(OpenAPIGeneratorMetadata metadata) {
        assertNotNull(metadata);

        assertEquals("BrAPI", metadata.getTitle());
        assertEquals("0.0.0", metadata.getVersion());
    }
}