package org.brapi.schematools.core.openapi.generator;


import io.swagger.v3.oas.models.OpenAPI;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenAPIGeneratorTest {

    @Test
    void generate() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());
    }

    @Test
    void generateByModule() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(true)).
                generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(5, specifications.getResult().size());

        Map<String, OpenAPI> byTitle = specifications.getResult().stream().collect(Collectors.toMap(specification -> specification.getInfo().getTitle(), specification -> specification));

        assertTrue(byTitle.containsKey("BrAPI-Core"));
        assertTrue(byTitle.containsKey("BrAPI-Germplasm"));
        assertTrue(byTitle.containsKey("BrAPI-Phenotyping"));
        assertTrue(byTitle.containsKey("BrAPI-Genotyping"));
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}