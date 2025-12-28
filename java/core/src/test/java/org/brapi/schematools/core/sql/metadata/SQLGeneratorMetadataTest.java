package org.brapi.schematools.core.sql.metadata;

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

class SQLGeneratorMetadataTest {

    @Test
    void load() {
        SQLGeneratorMetadata metadata = SQLGeneratorMetadata.load();

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadJson() {
        SQLGeneratorMetadata metadata = null;
        try {
            metadata = SQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-test-metadata.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadYaml() {
        SQLGeneratorMetadata metadata = null;
        try {
            metadata = SQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-test-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void overwrite() {
        SQLGeneratorMetadata metadata = null;
        try {
            metadata = SQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("brapi.test.", metadata.getTablePrefix());
    }
    
    @Test
    void compare() {
        try {
            SQLGeneratorMetadata options1 = SQLGeneratorMetadata.load() ;
            SQLGeneratorMetadata options2 = SQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-no-override-metadata.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultMetadata(SQLGeneratorMetadata metadata) {
        checkMetadata(metadata);

        assertEquals("brapi_", metadata.getTablePrefix());
    }

    private void checkMetadata(SQLGeneratorMetadata metadata) {
        assertNotNull(metadata);
    }

}