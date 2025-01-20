package org.brapi.schematools.core.ontmodel.options;

import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.valdiation.Validation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OntModelGeneratorOptionsTest {
    @Test
    void load() {
        OntModelGeneratorOptions options = OntModelGeneratorOptions.load();

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;

        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("ont-model-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("ont-model-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("ont-model-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertEquals("test2", options.getName());
    }

    private void checkDefaultOptions(OntModelGeneratorOptions options) {
        checkOptions(options);

        assertEquals("test", options.getName());
    }

    private void checkOptions(OntModelGeneratorOptions options) {

    }
}