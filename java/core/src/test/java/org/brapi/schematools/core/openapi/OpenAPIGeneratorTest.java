package org.brapi.schematools.core.openapi;


import io.swagger.v3.oas.models.OpenAPI;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OpenAPIGeneratorTest {

    @Test
    void generate() {
        Response<OpenAPI> specification = null;
        try {
            specification = new OpenAPIGenerator().generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), OpenAPIGeneratorOptions.load());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        assertNotNull(specification);

        specification.getAllErrors().forEach(this::printError);

        assertFalse(specification.hasErrors());
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}