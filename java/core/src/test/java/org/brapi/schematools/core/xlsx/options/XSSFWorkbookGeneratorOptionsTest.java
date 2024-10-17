package org.brapi.schematools.core.xlsx.options;

import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.valdiation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class XSSFWorkbookGeneratorOptionsTest {
    @Test
    void load() {
        XSSFWorkbookGeneratorOptions options = XSSFWorkbookGeneratorOptions.load();

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;
    }
}