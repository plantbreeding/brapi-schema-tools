package org.brapi.schematools.core.python;

import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.python.options.PythonGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.brapi.schematools.core.test.TestUtils.assertMultilineEqual;
import static org.brapi.schematools.core.utils.StringUtils.isMultilineEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class PythonGeneratorTest {

    private static final Path OUTPUT_PATH = Paths.get("build/test-output/Python/Generated");

    @Test
    void generate() {
        Response<List<Path>> response = null;
        try {
            response = new PythonGenerator(PythonGeneratorOptions.load().setOverwrite(true), OUTPUT_PATH)
                .generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }

        assertNotNull(response);

        response.getAllErrors().forEach(this::printError);
        assertFalse(response.hasErrors());

        assertNotNull(response.getResult());

        assertPythonEquals(response.getResult()) ;
    }

    private void assertPythonEquals(List<Path> result) {
        boolean failed = false ;
        try {
            for (Path path : result) {
                String relativePath = path.toAbsolutePath().toString().substring(OUTPUT_PATH.toAbsolutePath().toString().length() + 1);

                String actual = StringUtils.readStringFromPath(path).getResultOrThrow();

                try {
                    String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("Python/Generated/" + relativePath).toURI())).getResultOrThrow();
                    if (!isMultilineEqual(expected, actual)) {
                        Path build = Paths.get("build/test-output/Python/Fails", relativePath);
                        Files.createDirectories(build.getParent());
                        Files.writeString(build, actual);
                    }
                } catch (Exception e) {
                    failed = true ;
                    log.error(e.getMessage(), e);
                    Path build = Paths.get("build/test-output/Python/Fails", relativePath);
                    Files.createDirectories(build.getParent());
                    Files.writeString(build, actual);
                }
            }

            for (Path path : result) {
                String relativePath = path.toAbsolutePath().toString().substring(OUTPUT_PATH.toAbsolutePath().toString().length() + 1);

                String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("Python/Generated/"+ relativePath).toURI())).getResultOrThrow();
                String actual = StringUtils.readStringFromPath(path).getResultOrThrow();

                assertMultilineEqual(expected, actual);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }

        if (failed) {
            Assertions.fail("Generated Python did not match expected output. See build/test-output/Python/Fails for actual output.");
        }
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}