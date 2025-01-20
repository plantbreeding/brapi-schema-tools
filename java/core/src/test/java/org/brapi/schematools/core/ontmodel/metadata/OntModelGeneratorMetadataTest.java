package org.brapi.schematools.core.ontmodel.metadata;

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
    
    @Test
    void compare() {
        try {
            OntModelGeneratorMetadata options1 = OntModelGeneratorMetadata.load() ;
            OntModelGeneratorMetadata options2 = OntModelGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("ont-model-no-override-metadata.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
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