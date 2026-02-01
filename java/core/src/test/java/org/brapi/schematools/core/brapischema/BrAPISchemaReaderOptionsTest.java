package org.brapi.schematools.core.brapischema;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.graphql.options.*;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.OptionsTestBase;
import org.brapi.schematools.core.validiation.Validation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class BrAPISchemaReaderOptionsTest extends OptionsTestBase {

    @Test
    void load() {
        BrAPISchemaReaderOptions options = BrAPISchemaReaderOptions.load();

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        BrAPISchemaReaderOptions options = null;
        try {
            options = BrAPISchemaReaderOptions.load(Path.of(ClassLoader.getSystemResource("BrAPISchemaReader/test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        BrAPISchemaReaderOptions options = null;
        try {
            options = BrAPISchemaReaderOptions.load(Path.of(ClassLoader.getSystemResource("BrAPISchemaReader/test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        BrAPISchemaReaderOptions options = null;
        try {
            options = BrAPISchemaReaderOptions.load(Path.of(ClassLoader.getSystemResource("BrAPISchemaReader/override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkOverrideOptions(options);
    }

    @Test
    void compare() {
        try {
            BrAPISchemaReaderOptions options1 = BrAPISchemaReaderOptions.load() ;
            checkValidation(options1) ;
            BrAPISchemaReaderOptions options2 = BrAPISchemaReaderOptions.load(Path.of(ClassLoader.getSystemResource("BrAPISchemaReader/no-override-options.yaml").toURI()));
            checkValidation(options2) ;

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private void checkOverrideOptions(BrAPISchemaReaderOptions options) {
        checkOptions(options);

        assertEquals("V7", options.getSpecVersion());
        assertTrue(options.isIgnoringDuplicateProperties());
    }

    private void checkDefaultOptions(BrAPISchemaReaderOptions options) {
        checkOptions(options);

        assertEquals("V202012", options.getSpecVersion());
        assertFalse(options.isIgnoringDuplicateProperties());
    }

    private void checkOptions(BrAPISchemaReaderOptions options) {
        assertTrue(options.isWarningAboutDuplicateProperties());
    }
}