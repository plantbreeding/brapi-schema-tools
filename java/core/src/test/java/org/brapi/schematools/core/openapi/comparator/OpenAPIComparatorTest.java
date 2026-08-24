package org.brapi.schematools.core.openapi.comparator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.openapitools.openapidiff.core.model.ChangedOpenApi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.brapi.schematools.core.utils.StringUtils.readStringFromPath;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class OpenAPIComparatorTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void compareTreatsLocalAllOfComponentPropertiesAsInherited() {
        String flattenedSpecification = """
            {"openapi":"3.1.0","info":{"title":"Test","version":"1"},"paths":{"/items":{"get":{"responses":{"200":{"description":"OK","content":{"application/json":{"schema":{"type":"object","properties":{"metadata":{"$ref":"#/components/schemas/metadata"}}}}}}}}}},"components":{"schemas":{"metadata":{"type":"object","properties":{"datafiles":{"type":"array"},"pagination":{"type":"object"},"status":{"type":"array"}}}}}}
            """;
        String composedSpecification = """
            {"openapi":"3.1.0","info":{"title":"Test","version":"1"},"paths":{"/items":{"get":{"responses":{"200":{"description":"OK","content":{"application/json":{"schema":{"type":"object","properties":{"metadata":{"$ref":"#/components/schemas/metadata"}}}}}}}}}},"components":{"schemas":{"metadata":{"allOf":[{"$ref":"#/components/schemas/metadataBase"},{"type":"object","properties":{"pagination":{"type":"object"}}}]},"metadataBase":{"type":"object","properties":{"datafiles":{"type":"array"},"status":{"type":"array"}}}}}}
            """;

        Response<ChangedOpenApi> response = new OpenAPIComparator().compare(flattenedSpecification, composedSpecification);

        if (response.hasErrors()) {
            handleFailedResponse(response);
        }
        assertTrue(response.getResult().getChangedOperations().isEmpty());
        assertTrue(response.getResult().getChangedSchemas().isEmpty());
    }

    @Test
    void compareTreatsPureLocalRequestAliasAsEquivalentToInlineSchema() {
        String inlineSpecification = """
            {"openapi":"3.1.0","info":{"title":"Test","version":"1"},"paths":{"/methods":{"put":{"requestBody":{"content":{"application/json":{"schema":{"$ref":"#/components/schemas/MethodNewRequest"}}}},"responses":{"200":{"description":"OK"}}}}},"components":{"schemas":{"MethodNewRequest":{"type":"object","properties":{"description":{"type":"string"},"methodName":{"type":"string"}},"required":["methodName"]}}}}
            """;
        String aliasSpecification = """
            {"openapi":"3.1.0","info":{"title":"Test","version":"1"},"paths":{"/methods":{"put":{"requestBody":{"content":{"application/json":{"schema":{"$ref":"#/components/schemas/MethodNewRequest"}}}},"responses":{"200":{"description":"OK"}}}}},"components":{"schemas":{"MethodNewRequest":{"$ref":"#/components/schemas/MethodBaseClass"},"MethodBaseClass":{"type":"object","properties":{"description":{"type":"string"},"methodName":{"type":"string"}},"required":["methodName"]}}}}
            """;

        Response<ChangedOpenApi> response = new OpenAPIComparator().compare(inlineSpecification, aliasSpecification);

        if (response.hasErrors()) {
            handleFailedResponse(response);
        }
        assertTrue(response.getResult().getChangedOperations().isEmpty());
        assertTrue(response.getResult().getChangedSchemas().isEmpty());
    }

    @Test
    void openApiComparePetstoreHTML0() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison0.html") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison0.html").toURI()),
                outputPath);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreHTML1() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.html") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.html").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreHTMLFromString() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.html") ;
            Response<Path> response = comparator.compare(
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI())).getResultOrThrow(),
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI())).getResultOrThrow(),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.html").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreMarkdown0() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison0.md") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.MARKDOWN);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison0.md").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreMarkdown1() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.md") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.md").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreMarkdownFromString() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.md") ;
            Response<Path> response = comparator.compare(
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI())).getResultOrThrow(),
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI())).getResultOrThrow(),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.md").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreAsciidoc0() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison0.text") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.ASCIIDOC);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison0.text").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreAsciidoc1() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.text") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.text").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreAsciidocFromString() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.text") ;
            Response<Path> response = comparator.compare(
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI())).getResultOrThrow(),
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI())).getResultOrThrow(),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.text").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreJSON0() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison0.json") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.JSON);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertJSONEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison0.json").toURI()),
                outputPath);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreJSON1() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.json") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI()),
                outputPath,
                ComparisonOutputFormat.JSON);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertJSONEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.json").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiComparePetstoreJSONFromString() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/petstore-comparison1.json") ;
            Response<Path> response = comparator.compare(
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_1.yaml").toURI())).getResultOrThrow(),
                StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore_v2_2.yaml").toURI())).getResultOrThrow(),
                outputPath,
                ComparisonOutputFormat.JSON);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertJSONEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/petstore-comparison1.json").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }


    @Test
    void openApiCompareBrAPIHTML0() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/brapi-comparison0.html") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi_openapi_2_1.json").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi_openapi_2_1.json").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi-comparison0.html").toURI()),
                outputPath);
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void openApiCompareBrAPIHTML1() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/OpenAPIComparator/brapi-comparison1.html") ;
            Response<Path> response = comparator.compare(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi_openapi_2_1.json").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi_openapi_2_1_0.json").toURI()),
                outputPath,
                ComparisonOutputFormat.HTML);

            if (response.hasErrors()) {
                handleFailedResponse(response) ;
            }

            assertEquals(outputPath, response.getResult());
            assertFileEquals(
                Path.of(ClassLoader.getSystemResource("OpenAPIComparator/brapi-comparison1.html").toURI()),
                outputPath);

        } catch (Exception e) {
            log.debug(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private void assertFileEquals(Path actualPath, Path expectedPath) {
        readStringFromPath(actualPath)
            .onSuccessDoWithResult(actual -> readStringFromPath(expectedPath)
                .onSuccessDoWithResult(expected -> assertLinesMatch(actual.lines(), expected.lines()))
                .onFailDoWithResponse(this::handleFailedResponse))
            .onFailDoWithResponse(this::handleFailedResponse);
    }

    private void assertJSONEquals(Path actualPath, Path expectedPath) {
        readStringFromPath(actualPath)
            .mapResultToResponse(this::readJSON)
            .onSuccessDoWithResult(actual -> readStringFromPath(expectedPath)
                .mapResultToResponse(this::readJSON)
                .onSuccessDoWithResult(expected -> assertEquals(expected, actual))
                .onFailDoWithResponse(this::handleFailedResponse))
            .onFailDoWithResponse(this::handleFailedResponse);
    }

    private Response<JsonNode> readJSON(String string) {
        try {
            return Response.success(mapper.readTree(string)) ;
        } catch (JsonProcessingException e) {
            return Response.fail(Response.ErrorType.VALIDATION, e.getMessage()) ;
        }
    }


    private void handleFailedResponse(Response<?> response) {
        fail(response.getMessagesCombined(", ")) ;
    }
}