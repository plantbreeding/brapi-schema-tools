package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.oas.models.Components;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIComponentsReaderTest {
    @Test
    void readComponents() {

        try {
            Components components =
                new OpenAPIComponentsReader().readComponents(Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI())).
                    onFailDoWithResponse(response -> fail(response.getMessagesCombined(", "))).getResult();

            assertNotNull(components);
            assertNotNull(components.getParameters());
            assertNotNull(components.getResponses());
            assertNotNull(components.getSchemas());

            assertEquals(9, components.getParameters().size());
            assertEquals(5, components.getResponses().size());
            assertEquals(9, components.getSchemas().size());
            assertEquals(1, components.getSecuritySchemes().size());

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}