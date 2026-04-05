package org.brapi.schematools.core.sql;

import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.sql.metadata.SQLGeneratorMetadata;
import org.brapi.schematools.core.sql.options.SQLGeneratorOptions;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
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
        generate(SQLGeneratorOptions.load(), SQLGeneratorMetadata.load(), 0, "build/test-output/SQLGenerator/defaults") ;
    }

    @Test
    void generateWithOverwrite() {
        generate(SQLGeneratorOptions.load().setOverwrite(true), SQLGeneratorMetadata.load(), 36, "build/test-output/SQLGenerator/defaults") ;
    }

    void generate(SQLGeneratorOptions options, SQLGeneratorMetadata metadata, int expectedSize, String classpath) {
        Response<List<Path>> response = null;
        try {
            SQLGenerator generator = new SQLGenerator(options, Paths.get(classpath));

            response = generator.generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), metadata) ;

            assertNotNull(response);

            response.getAllErrors().forEach(this::printError);
            assertFalse(response.hasErrors());

            assertNotNull(response.getResult());
            assertEquals(expectedSize, response.getResult().size());

            response.getResult().forEach(path -> {
                assertTrue(Files.exists(path), "Generated file does not exist: " + path);
                assertTrue(Files.isRegularFile(path), "Generated path is not a file: " + path);

                try {
                    assertDDLEquals(Path.of(ClassLoader.getSystemResource("SQLGenerator/defaults").toURI()).resolve(path.getFileName().toString()), path) ;
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
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