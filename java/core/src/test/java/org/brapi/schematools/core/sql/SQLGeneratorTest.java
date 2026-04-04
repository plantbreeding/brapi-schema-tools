package org.brapi.schematools.core.sql;

import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.brapi.schematools.core.test.TestUtils.assertMultilineEqual;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class SQLGeneratorTest {

    @Test
    void generateWithDefaults() {
        generate(SQLGeneratorOptions.load(), SQLGeneratorMetadata.load()) ;
    }

    void generate(SQLGeneratorOptions options, SQLGeneratorMetadata metadata) {
        Response<List<Path>> response = null;
        try {
            SQLGenerator generator = new SQLGenerator(options, Paths.get("build/test-output/SQLGenerator/defaults"));

            response = generator.generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), metadata) ;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }

        assertNotNull(response);

        response.getAllErrors().forEach(this::printError);
        assertFalse(response.hasErrors());

        assertNotNull(response.getResult());

        response.getResult().forEach(path -> {
            assertTrue(Files.exists(path), "Generated file does not exist: " + path);
            assertTrue(Files.isRegularFile(path), "Generated path is not a file: " + path);

            assertDDLEquals(Paths.get("build/test-output/SQLGenerator/defaults", path.getFileName().toString()), path) ;
        });
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }

    private void assertDDLEquals(Path expectedPath, Path actualPath) {
        try {
            String expected = StringUtils.readStringFromPath(expectedPath).getResultOrThrow() ;
            String actual = StringUtils.readStringFromPath(actualPath).getResultOrThrow() ;

            assertMultilineEqual(expected, actual);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

}