package org.brapi.schematools.core.r;

import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.r.options.RGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.brapi.schematools.core.test.TestUtils.assertMultilineEqual;
import static org.brapi.schematools.core.utils.StringUtils.isMultilineEqual;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Slf4j
class RGeneratorTest {

    @Test
    void generate() {
        Response<List<Path>> response = null;
        try {
            response = new RGenerator(RGeneratorOptions.load().setOverwrite(true), Paths.get("build/test-output/R/Generated"))
                .generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }

        assertNotNull(response);

        response.getAllErrors().forEach(this::printError);
        assertFalse(response.hasErrors());

        assertNotNull(response.getResult());

        assertREquals(response.getResult()) ;
    }

    private void assertREquals(List<Path> result) {
        try {
            for (Path path : result) {

                String rFile = path.getFileName().toString();

                String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("RGenerator/Generated/"+ rFile).toURI())).getResultOrThrow();
                String actual = StringUtils.readStringFromPath(path).getResultOrThrow();

                if (!isMultilineEqual(expected, actual)) {
                    Path build = Paths.get("build/test-output/R/Fails", rFile);
                    Files.createDirectories(build.getParent());
                    Files.writeString(build, actual);
                }

                assertMultilineEqual(expected, actual);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}