package org.brapi.schematools.core.ontmodel.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OntModelGeneratorMetadataTest {

    @Test
    void load() {
        OntModelGeneratorMetadata metadata = OntModelGeneratorMetadata.load();

        checkMetadata(metadata);
    }

    @Test
    void loadJson() {
    }

    @Test
    void loadYaml() {
    }

    private void checkMetadata(OntModelGeneratorMetadata metadata) {
        assertNotNull(metadata);

        assertEquals("http://brapi.org", metadata.getNamespace());
    }

}