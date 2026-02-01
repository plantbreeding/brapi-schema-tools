package org.brapi.schematools.core.markdown;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.markdown.options.MarkdownGeneratorOptions;
import org.brapi.schematools.core.options.OptionsTestBase;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class MarkdownGeneratorOptionsTest extends OptionsTestBase {
    @Test
    void load() {
        MarkdownGeneratorOptions options = MarkdownGeneratorOptions.load();

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void load2() {
        MarkdownGeneratorOptions options = MarkdownGeneratorOptions.load().setOverwrite(true);

        checkValidation(options) ;
        assertTrue(options.isOverwritingExistingFiles());
        assertTrue(options.isAddingGeneratorComments()) ;
    }

    @Test
    void loadJson() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("Markdown/markdown-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("Markdown/markdown-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        MarkdownGeneratorOptions options = null;
        try {
            options = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("Markdown/markdown-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkOptions(options);

        assertFalse(options.isAddingGeneratorComments()) ;
    }

    @Test
    void compare() {
        try {
            MarkdownGeneratorOptions options1 = MarkdownGeneratorOptions.load() ;
            checkValidation(options1) ;
            MarkdownGeneratorOptions options2 = MarkdownGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("Markdown/markdown-no-override-options.yaml").toURI()));
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
    private void checkDefaultOptions(MarkdownGeneratorOptions options) {
        checkOptions(options);

        assertTrue(options.isAddingGeneratorComments());
        assertTrue(options.isGeneratingForProperties());
        assertTrue(options.isGeneratingForDuplicateProperties());
        assertFalse(options.isGeneratingForParameters());
        assertFalse(options.isGeneratingForRequests());
    }

    private void checkOptions(MarkdownGeneratorOptions options) {
        assertFalse(options.isOverwritingExistingFiles()); ;
    }
}