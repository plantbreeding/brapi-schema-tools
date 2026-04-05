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
import org.brapi.schematools.core.validiation.Validation;
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
        validateTrue(SQLGeneratorOptions.load().setIfNotExists(false).setDropTable(false).validate());
        validateTrue(SQLGeneratorOptions.load().setIfNotExists(false).setDropTable(true).validate());
        validateTrue(SQLGeneratorOptions.load().setIfNotExists(true).setDropTable(false).validate());
        validateFalse(SQLGeneratorOptions.load().setIfNotExists(true).setDropTable(true).validate());

        validateTrue(SQLGeneratorOptions.load().setAddForeignKeyConstraints(false).setGenerateForeignKeyConstraintScript(false).setAddConstraintIfExists(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddForeignKeyConstraints(false).setGenerateForeignKeyConstraintScript(true).setAddConstraintIfExists(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddForeignKeyConstraints(true).setGenerateForeignKeyConstraintScript(false).setAddConstraintIfExists(false).validate());
        validateFalse(SQLGeneratorOptions.load().setAddForeignKeyConstraints(true).setGenerateForeignKeyConstraintScript(true).setAddConstraintIfExists(false).validate());
    }

    @Test
    void checkKeyConstraintOptions() {
        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(false).setAddForeignKeyConstraints(false).setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(false).validate());
        validateFalse(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(false).setAddForeignKeyConstraints(true).setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(true).setAddForeignKeyConstraints(false).setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(true).setAddForeignKeyConstraints(true).setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(false).validate());

        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(false).setGenerateForeignKeyConstraintScript(false).setAddConstraintIfExists(false).setAddForeignKeyConstraints(false).validate());
        validateFalse(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(false).setGenerateForeignKeyConstraintScript(true).setAddConstraintIfExists(false).setAddForeignKeyConstraints(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(true).setGenerateForeignKeyConstraintScript(false).setAddConstraintIfExists(false).setAddForeignKeyConstraints(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddPrimaryKeyConstraints(true).setGenerateForeignKeyConstraintScript(true).setAddConstraintIfExists(false).setAddForeignKeyConstraints(false).validate());


        validateTrue(SQLGeneratorOptions.load().setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(false).setAddForeignKeyConstraints(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddConstraintIfExists(false).setGenerateForeignKeyConstraintScript(true).setAddForeignKeyConstraints(false).validate());
        validateFalse(SQLGeneratorOptions.load().setAddConstraintIfExists(true).setGenerateForeignKeyConstraintScript(false).setAddForeignKeyConstraints(false).validate());
        validateTrue(SQLGeneratorOptions.load().setAddConstraintIfExists(true).setGenerateForeignKeyConstraintScript(true).setAddForeignKeyConstraints(false).validate());
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

    private void validateTrue(Validation validate) {
        if (validate.isValid()) {
            assertTrue(true);
        } else {
            validate.getErrors().forEach(error -> log.error(error.toString()));
            fail("Validation was expected to be valid but was not");
        }
    }

    private void validateFalse(Validation validate) {
        if (validate.isValid()) {
            fail("Validation was expected to be invalid but was valid");
        } else {
            validate.getErrors().forEach(error -> log.error(error.toString()));
            assertTrue(true);
        }
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