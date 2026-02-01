package org.brapi.schematools.core.sql.opitons;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.OptionsTestBase;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class SQLGeneratorOptionsTest extends OptionsTestBase {
    @Test
    void load() {
        SQLGeneratorOptions options = SQLGeneratorOptions.load();

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void load2() {
        SQLGeneratorOptions options = SQLGeneratorOptions.load().setOverwrite(true);

        checkValidation(options);
        checkOverrideOptions(options);
    }

    @Test
    void loadJson() {
        SQLGeneratorOptions options = null;
        try {
            options = SQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);

            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        SQLGeneratorOptions options = null;
        try {
            options = SQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        SQLGeneratorOptions options = null;
        try {
            options = SQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;

        checkOptions(options);
    }

    @Test
    void missingBrAPISchemaReaderOptions() {
        SQLGeneratorOptions options;
        try {
            options = ConfigurationUtils.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/missing-brapi-schema-reader-options.yaml").toURI()), SQLGeneratorOptions.class) ;

            checkValidation(options) ;
            checkDefaultOptions(options);

        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    @Test
    void compare() {
        try {
            SQLGeneratorOptions options1 = SQLGeneratorOptions.load() ;
            checkValidation(options1) ;
            SQLGeneratorOptions options2 = SQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("SQLGenerator/sql-no-override-options.yaml").toURI()));
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

    @Test
    void checkMutuallyExclusiveOptions() {
        assertTrue(SQLGeneratorOptions.load().setIfNotExists(true).validate().isValid());
        assertTrue(SQLGeneratorOptions.load().setDropTable(true).validate().isValid());
        assertFalse(SQLGeneratorOptions.load().setIfNotExists(true).setDropTable(true).validate().isValid());
    }

    private void checkDefaultOptions(SQLGeneratorOptions options) {
        checkOptions(options);

        assertFalse(options.isOverwritingExistingFiles());

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.EMBEDDED,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Trial").build(),
                BrAPIObjectProperty.builder().name("contacts").build()).getResultOrThrow()
        );

        assertEquals(2, options.getIndentSize());
    }

    private void checkOverrideOptions(SQLGeneratorOptions options) {
        checkOptions(options);

        assertTrue(options.isOverwritingExistingFiles());
    }

    private void checkOptions(SQLGeneratorOptions options) {
        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("germplasm").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("pedigreeNodes").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Variant").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("callSets").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("variants").build()).getResultOrThrow()
        );
    }
}