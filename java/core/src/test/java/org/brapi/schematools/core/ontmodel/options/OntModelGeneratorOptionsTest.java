package org.brapi.schematools.core.ontmodel.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.options.OptionsTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class OntModelGeneratorOptionsTest extends OptionsTestBase {
    @Test
    void load() {
        OntModelGeneratorOptions options = OntModelGeneratorOptions.load();

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OntModelGenerator/ont-model-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OntModelGenerator/ont-model-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        OntModelGeneratorOptions options = null;
        try {
            options = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OntModelGenerator/ont-model-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkOptions(options);

        assertEquals("test2", options.getName());
    }

    @Test
    void compare() {
        try {
            OntModelGeneratorOptions options1 = OntModelGeneratorOptions.load() ;
            checkValidation(options1) ;
            OntModelGeneratorOptions options2 = OntModelGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OntModelGenerator/ont-model-no-override-options.yaml").toURI()));
            checkValidation(options2) ;

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