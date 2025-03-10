package org.brapi.schematools.core.ontmodel.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.validiation.Validation;
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
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/ont-model-test-options.json").toURI()));
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
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/ont-model-test-options.yaml").toURI()));
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
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/ont-model-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertEquals("test2", options.getName());
    }

    @Test
    void compare() {
        try {
            OntModelGeneratorOptions options1 = OntModelGeneratorOptions.load() ;
            OntModelGeneratorOptions options2 = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/ont-model-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultOptions(OntModelGeneratorOptions options) {
        checkOptions(options);

        assertEquals("test", options.getName());
    }

    private void checkOptions(OntModelGeneratorOptions options) {

    }
}