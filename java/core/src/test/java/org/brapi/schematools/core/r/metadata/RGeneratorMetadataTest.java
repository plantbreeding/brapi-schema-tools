package org.brapi.schematools.core.r.metadata;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RGeneratorMetadataTest {

    @Test
    void load() {
        RGeneratorMetadata metadata = RGeneratorMetadata.load();

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadJson() {
        RGeneratorMetadata metadata = null;
        try {
            metadata = RGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("RGenerator/test-metadata.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadYaml() {
        RGeneratorMetadata metadata = null;
        try {
            metadata = RGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("RGenerator/test-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void overwrite() {
        RGeneratorMetadata metadata = null;
        try {
            metadata = RGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("RGenerator/override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("generated_test_", metadata.getFilePrefix());
    }
    
    @Test
    void compare() {
        try {
            RGeneratorMetadata options1 = RGeneratorMetadata.load() ;
            RGeneratorMetadata options2 = RGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("RGenerator/no-override-metadata.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultMetadata(RGeneratorMetadata metadata) {
        checkMetadata(metadata);

        assertEquals("generated_", metadata.getFilePrefix());
    }

    private void checkMetadata(RGeneratorMetadata metadata) {
        assertNotNull(metadata);
    }

}