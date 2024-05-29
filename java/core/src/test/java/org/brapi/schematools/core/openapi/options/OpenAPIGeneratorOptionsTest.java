package org.brapi.schematools.core.openapi.options;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIGeneratorOptionsTest {
    @Test
    void load() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();

        checkOptions(options);
    }

    @Test
    void defaultBuilder() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.defaultBuilder().build();

        checkOptions(options);
    }


    @Test
    void loadJson() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);
    }

    @Test
    void loadYaml() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);
    }

    private void checkOptions(OpenAPIGeneratorOptions options) {
        assertNotNull(options);

        assertTrue(options.isGeneratingEndpoint());

        assertNotNull(options.getSingleGet());
        assertTrue(options.getSingleGet().isGenerating());

        assertNotNull(options.getListGet());
        assertTrue(options.getListGet().isGenerating());

        assertNotNull(options.getPost());
        assertTrue(options.getPost().isGenerating());

        assertNotNull(options.getPut());
        assertTrue(options.getPut().isGenerating());

        assertNotNull(options.getDelete());
        assertFalse(options.getDelete().isGenerating());
    }

}