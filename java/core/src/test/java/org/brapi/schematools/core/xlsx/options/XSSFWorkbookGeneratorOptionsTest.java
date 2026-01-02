package org.brapi.schematools.core.xlsx.options;

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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class XSSFWorkbookGeneratorOptionsTest {
    @Test
    void load() {
        XSSFWorkbookGeneratorOptions options = XSSFWorkbookGeneratorOptions.load();

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;

        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        XSSFWorkbookGeneratorOptions options = null;
        try {
            options = XSSFWorkbookGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("XSSFWorkbookGenerator/xlsx-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        XSSFWorkbookGeneratorOptions options = null;
        try {
            options = XSSFWorkbookGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("XSSFWorkbookGenerator/xlsx-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        XSSFWorkbookGeneratorOptions options = null;
        try {
            options = XSSFWorkbookGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("XSSFWorkbookGenerator/xlsx-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertFalse(options.getDataClassProperties().isEmpty()) ;
        assertEquals(2, options.getDataClassProperties().size()); ;
        assertEquals("name", options.getDataClassProperties().get(0).getName()) ;
        assertEquals("module", options.getDataClassProperties().get(1).getName()) ;

        assertFalse(options.getDataClassFieldProperties().isEmpty()) ;
        assertEquals(1, options.getDataClassFieldProperties().size()); ;
        assertEquals("name", options.getDataClassFieldProperties().getFirst().getName()) ;

        assertEquals(List.of("Class Name2"), options.getDataClassFieldHeaders()) ;
    }

    @Test
    void compare() {
        try {
            XSSFWorkbookGeneratorOptions options1 = XSSFWorkbookGeneratorOptions.load() ;
            XSSFWorkbookGeneratorOptions options2 = XSSFWorkbookGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("XSSFWorkbookGenerator/xlsx-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultOptions(XSSFWorkbookGeneratorOptions options) {

        checkOptions(options);

        assertFalse(options.getDataClassProperties().isEmpty()) ;
        assertEquals(5, options.getDataClassProperties().size()); ;
        assertEquals("name", options.getDataClassProperties().get(0).getName()) ;
        assertEquals("module", options.getDataClassProperties().get(1).getName()) ;

        assertFalse(options.getDataClassFieldProperties().isEmpty()) ;
        assertEquals(2, options.getDataClassFieldProperties().size()); ;
        assertEquals("name", options.getDataClassFieldProperties().get(0).getName()) ;
        assertEquals("description", options.getDataClassFieldProperties().get(1).getName()) ;
        assertEquals(List.of("Class Name", "BrAPI Module"), options.getDataClassFieldHeaders()) ;
    }


    private void checkOptions(XSSFWorkbookGeneratorOptions options) {


    }
}