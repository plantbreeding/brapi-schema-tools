package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.openapi.generator.metadata.OpenAPIGeneratorMetadata;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.brapi.schematools.core.test.TestUtils.assertJSONEquals;
import static org.brapi.schematools.core.utils.StringUtils.isJSONEqual;
import static org.brapi.schematools.core.utils.OpenAPIUtils.prettyPrint;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
class OpenAPIGeneratorTest {

    @Test
    void generate() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());

        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Complete.json", specifications.getResult().getFirst()) ;
    }

    @Test
    void generateWithMetadata() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                    OpenAPIGeneratorMetadata.load().setTitle("Test").setVersion("1.2.3"));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());

        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Complete-md.json", specifications.getResult().getFirst()) ;
    }

    @Test
    void generateByModule() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(true)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(4, specifications.getResult().size());

        Map<String, OpenAPI> byTitle = specifications.getResult().stream().collect(Collectors.toMap(specification -> specification.getInfo().getTitle(), specification -> specification));

        assertTrue(byTitle.containsKey("BrAPI-Core"));
        assertTrue(byTitle.containsKey("BrAPI-Germplasm"));
        assertTrue(byTitle.containsKey("BrAPI-Phenotyping"));
        assertTrue(byTitle.containsKey("BrAPI-Genotyping"));

        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Core.json", byTitle.get("BrAPI-Core")) ;
        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Germplasm.json", byTitle.get("BrAPI-Germplasm")) ;
        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Phenotyping.json", byTitle.get("BrAPI-Phenotyping")) ;
        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Genotyping.json", byTitle.get("BrAPI-Genotyping")) ;
    }

    @Test
    void generateBreedingMethod() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                    List.of("BreedingMethod"));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());

        assertSpecificationEquals("OpenAPIGenerator/BreedingMethod.json", specifications.getResult().getFirst()) ;
    }

    @Test
    void generateStudy() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                    List.of("Study"));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());

        assertSpecificationEquals("OpenAPIGenerator/Study.json", specifications.getResult().getFirst()) ;
    }

    @Test
    void generateGermplasm() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(false)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                    List.of("Germplasm"));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(1, specifications.getResult().size());

        assertSpecificationEquals("OpenAPIGenerator/Germplasm.json", specifications.getResult().getFirst()) ;
    }

    @Test
    void generateGermplasmAndStudySeparateByModule() {
        Response<List<OpenAPI>> specifications;
        try {
            specifications = new OpenAPIGenerator(OpenAPIGeneratorOptions.load().setSeparateByModule(true)).
                generate(
                    Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                    Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()),
                    List.of("Study", "Germplasm"));
        } catch (URISyntaxException e) {
            log.debug(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        assertNotNull(specifications);

        specifications.getAllErrors().forEach(this::printError);
        assertFalse(specifications.hasErrors());

        assertEquals(2, specifications.getResult().size());

        Map<String, OpenAPI> byTitle = specifications.getResult().stream().collect(Collectors.toMap(specification -> specification.getInfo().getTitle(), specification -> specification));

        assertTrue(byTitle.containsKey("BrAPI-Core"));
        assertTrue(byTitle.containsKey("BrAPI-Germplasm"));
        assertFalse(byTitle.containsKey("BrAPI-Phenotyping"));
        assertFalse(byTitle.containsKey("BrAPI-Genotyping"));

        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Core-Study.json", byTitle.get("BrAPI-Core")) ;
        assertSpecificationEquals("OpenAPIGenerator/BrAPI-Germplasm-Germplasm.json", byTitle.get("BrAPI-Germplasm")) ;
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }

    private void assertSpecificationEquals(String classPath, OpenAPI specification) {
        try {
            String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource(classPath).toURI())).getResultOrThrow() ;
            String actual = prettyPrint(specification);

            if (!isJSONEqual(expected, actual)) {
                Path build = Paths.get("build", classPath);
                Files.createDirectories(build.getParent()) ;
                Files.writeString(build, actual);
            }

            assertJSONEquals(expected, actual);
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}