package org.brapi.schematools.core.openapi.comparator.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIComparatorOptionsTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void load() {
        OpenAPIComparatorOptions options = OpenAPIComparatorOptions.load();

        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        OpenAPIComparatorOptions options = null;
        try {
            options = OpenAPIComparatorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-comparator-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        OpenAPIComparatorOptions options = null;
        try {
            options = OpenAPIComparatorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-comparator-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        OpenAPIComparatorOptions options = null;

        try {
            options = OpenAPIComparatorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-comparator-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options) ;
        assertFalse(options.getJson().isPrettyPrinting()); ;
    }

    @Test
    void compare() {
        try {
            OpenAPIComparatorOptions options1 = OpenAPIComparatorOptions.load() ;
            OpenAPIComparatorOptions options2 = OpenAPIComparatorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-comparator-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void checkDefaultOptions(OpenAPIComparatorOptions options) {
        checkOptions(options);
        assertTrue(options.getJson().isPrettyPrinting()); ;
    }

    private void checkOptions(OpenAPIComparatorOptions options) {
        assertNotNull(options);
        assertNotNull(options.validate());
    }
}