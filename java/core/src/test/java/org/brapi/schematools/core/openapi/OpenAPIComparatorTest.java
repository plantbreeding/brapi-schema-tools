package org.brapi.schematools.core.openapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.brapi.schematools.core.utils.StringUtils.readStringFromPath;
import static org.junit.jupiter.api.Assertions.*;

class OpenAPIComparatorTest {

    private static ObjectMapper mapper;

    @BeforeAll
    static void setup() {
        mapper = new ObjectMapper();
    }

    @Test
    void comparePetstoreHTML() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/petstore-comparison0.html") ;
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

            outputPath = Path.of("build/test-output/petstore-comparison1.html") ;
            response = comparator.compare(
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
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void comparePetstoreMarkdown() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/petstore-comparison0.md") ;
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

            outputPath = Path.of("build/test-output/petstore-comparison1.md") ;
            response = comparator.compare(
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
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void comparePetstoreAsciidoc() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/petstore-comparison0.text") ;
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

            outputPath = Path.of("build/test-output/petstore-comparison1.text") ;
            response = comparator.compare(
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
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void comparePetstoreJSON() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/petstore-comparison0.json") ;
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

            outputPath = Path.of("build/test-output/petstore-comparison1.json") ;
            response = comparator.compare(
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
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    void compareBrAPIHTML() {
        OpenAPIComparator comparator = new OpenAPIComparator() ;

        try {
            Path outputPath = Path.of("build/test-output/brapi-comparison0.html") ;
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

            outputPath = Path.of("build/test-output/brapi-comparison1.html") ;
            response = comparator.compare(
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
            e.printStackTrace();
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