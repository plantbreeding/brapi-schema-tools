package org.brapi.schematools.core.python;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.python.options.PythonGeneratorOptions;
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
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class PythonGeneratorTest {

    private static final Path OUTPUT_PATH = Paths.get("build/test-output/Python/Generated");
    private static final Path NOTEBOOK_OUTPUT_PATH = Paths.get("build/test-output/Python/Notebooks");

    @Test
    void generate() {
        Response<List<Path>> response = null;
        try {
            response = new PythonGenerator(
                PythonGeneratorOptions.load()
                    .setOverwrite(true)
                    .setGenerateNotebooks(false),
                OUTPUT_PATH)
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

    @Test
    void generateNotebooks() {
        Response<List<Path>> response = null;
        try {
            response = new PythonGenerator(
                PythonGeneratorOptions.load()
                    .setOverwrite(true)
                    .setGenerateNotebooks(true)
                    .setNotebooksDirectory("notebooks"),
                NOTEBOOK_OUTPUT_PATH)
                .generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e);
        }

        assertNotNull(response);
        response.getAllErrors().forEach(this::printError);
        assertFalse(response.hasErrors());
        assertNotNull(response.getResult());

        List<Path> notebooks = response.getResult().stream()
            .filter(p -> p.toString().endsWith(".ipynb"))
            .toList();

        assertFalse(notebooks.isEmpty(), "Expected at least one notebook to be generated");

        ObjectMapper mapper = new ObjectMapper();
        for (Path notebook : notebooks) {
            assertTrue(Files.exists(notebook), "Notebook file missing: " + notebook);

            JsonNode root;
            try {
                root = mapper.readTree(notebook.toFile());
            } catch (Exception e) {
                fail("Notebook is not valid JSON: " + notebook + " — " + e.getMessage());
                return;
            }

            assertEquals(4, root.path("nbformat").asInt(-1),
                "Expected nbformat 4 in " + notebook.getFileName());
            assertTrue(root.has("cells"),
                "Notebook missing 'cells' array: " + notebook.getFileName());

            JsonNode cells = root.get("cells");
            assertTrue(cells.isArray() && cells.size() > 0,
                "Notebook has empty cells array: " + notebook.getFileName());

            // Every cell must have a non-empty source
            for (JsonNode cell : cells) {
                assertTrue(cell.has("cell_type"),
                    "Cell missing 'cell_type' in " + notebook.getFileName());
                assertTrue(cell.has("source"),
                    "Cell missing 'source' in " + notebook.getFileName());
            }
        }
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
