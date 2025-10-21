package org.brapi.schematools.core.markdown;

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

import static org.junit.jupiter.api.Assertions.*;

class MarkdownGeneratorOptionsTest {
    @Test
    void load() {
        MarkdownGeneratorOptions options = MarkdownGeneratorOptions.load();

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;

        checkDefaultOptions(options);
    }

    @Test
    void load2() {
        MarkdownGeneratorOptions options = MarkdownGeneratorOptions.load().setOverwrite(true);

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(options.isOverwritingExistingFiles());
        assertTrue(options.isAddingGeneratorComments()) ;
    }

    @Test
    void loadJson() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/markdown-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/markdown-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/markdown-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertFalse(options.isAddingGeneratorComments()) ;
    }

    @Test
    void compare() {
        try {
            MarkdownGeneratorOptions options1 = MarkdownGeneratorOptions.load() ;
            MarkdownGeneratorOptions options2 = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("options/markdown-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultOptions(MarkdownGeneratorOptions options) {
        checkOptions(options);

        assertTrue(options.isAddingGeneratorComments());
    }

    private void checkOptions(MarkdownGeneratorOptions options) {
        assertFalse(options.isOverwritingExistingFiles()); ;
    }
}