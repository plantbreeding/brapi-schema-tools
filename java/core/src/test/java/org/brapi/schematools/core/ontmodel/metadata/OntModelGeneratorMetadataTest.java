package org.brapi.schematools.core.ontmodel.metadata;

import org.brapi.schematools.core.xlsx.options.XSSFWorkbookGeneratorOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class OntModelGeneratorMetadataTest {

    @Test
    void load() {
        OntModelGeneratorMetadata metadata = OntModelGeneratorMetadata.load();

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadJson() {
        OntModelGeneratorMetadata metadata = null;
        try {
            metadata = OntModelGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("ont-model-test-metadata.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadYaml() {
        OntModelGeneratorMetadata metadata = null;
        try {
            metadata = OntModelGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("ont-model-test-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void overwrite() {
        OntModelGeneratorMetadata metadata = null;
        try {
            metadata = OntModelGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("ont-model-override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("es", metadata.getLanguage());
    }

    private void checkDefaultMetadata(OntModelGeneratorMetadata metadata) {
        checkMetadata(metadata);

        assertEquals("en", metadata.getLanguage());
    }

    private void checkMetadata(OntModelGeneratorMetadata metadata) {
        assertNotNull(metadata);

        assertEquals("http://brapi.org", metadata.getNamespace());
    }

}