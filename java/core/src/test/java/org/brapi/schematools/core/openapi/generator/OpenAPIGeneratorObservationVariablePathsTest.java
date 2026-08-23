package org.brapi.schematools.core.openapi.generator;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.openapi.generator.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.BrAPITypeUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Regression coverage for ObservationVariable REST path emission.
 *
 * ObservationVariable is an allOf of the Variable interface plus local properties, with
 * brapi-metadata.primaryModel=true. Paths must be emitted under the documented pathItemNameFor
 * mapping (/variables), not the naive plural /observationvariables.
 *
 * Historical failure mode (CI): ObservationVariable was degraded after allOf flattening and
 * dropped from the primary-model set, which removed all six REST operations and the
 * NewRequest/SearchRequest schemas from single-file OPEN_API_JSON output.
 */
class OpenAPIGeneratorObservationVariablePathsTest {

    private static final Set<String> EXPECTED_PATHS = Set.of(
        "/variables",
        "/variables/{observationVariableDbId}",
        "/search/variables",
        "/search/variables/{searchResultsDbId}"
    );

    @Test
    void observationVariablePathItemNameIsVariablesNotObservationVariables() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();
        assertEquals("/variables", options.getPathItemNameFor("ObservationVariable"),
            "default openapi-options.yaml must map ObservationVariable to /variables");
        assertEquals("/search/variables", options.getSearchPathItemNameFor("ObservationVariable"));
        assertTrue(options.isGeneratingEndpointFor("ObservationVariable"));
        assertTrue(options.isGeneratingEndpointNameWithIdFor("ObservationVariable"));
        assertTrue(options.getSearch().isGeneratingFor("ObservationVariable"));
    }

    @Test
    void observationVariableIsPrimaryObjectTypeWithFullProperties() throws Exception {
        List<BrAPIClass> classes = new BrAPISchemaReader()
            .readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()))
            .onFailDoWithResponse(r -> fail(r.getMessagesCombined(",")))
            .getResult();

        long observationVariableCount = classes.stream()
            .filter(c -> "ObservationVariable".equals(c.getName()))
            .count();
        assertEquals(1, observationVariableCount,
            "schema reader must yield exactly one ObservationVariable class");

        BrAPIClass observationVariable = classes.stream()
            .filter(c -> "ObservationVariable".equals(c.getName()))
            .findFirst()
            .orElseGet(() -> fail("ObservationVariable class missing from schema reader output"));

        assertTrue(observationVariable instanceof BrAPIObjectType,
            "ObservationVariable must be flattened to BrAPIObjectType, was "
                + observationVariable.getClass().getSimpleName());
        assertTrue(BrAPITypeUtils.isPrimaryModel(observationVariable),
            "ObservationVariable must retain primaryModel metadata after allOf flattening");
        assertNotNull(observationVariable.getModule());
        assertEquals("BrAPI-Phenotyping", observationVariable.getModule());

        BrAPIObjectType objectType = (BrAPIObjectType) observationVariable;
        Set<String> propertyNames = objectType.getProperties().stream()
            .map(p -> p.getName())
            .collect(Collectors.toSet());

        assertTrue(propertyNames.contains("observationVariableDbId"));
        assertTrue(propertyNames.contains("observationVariableName"));
        // Properties inherited from the Variable interface. Missing these is the known degraded form
        // that previously coincided with dropped /variables REST paths.
        assertTrue(propertyNames.contains("trait"),
            "ObservationVariable should include Variable interface properties such as trait; got " + propertyNames);
        assertTrue(propertyNames.contains("method"));
        assertTrue(propertyNames.contains("scale"));
        assertTrue(propertyNames.size() > 5,
            "ObservationVariable should not be degraded to only its local properties; size=" + propertyNames.size());

        boolean hasRequest = classes.stream().anyMatch(c -> "ObservationVariableRequest".equals(c.getName()));
        assertTrue(hasRequest, "ObservationVariableRequest must be discovered for list/search parameter generation");
    }

    @Test
    void singleFileOpenApiIncludesAllObservationVariableRestPaths() throws Exception {
        // Mirrors BrAPI generator/openapi-single-file-options.yaml: separateByModule=false
        Response<List<OpenAPI>> specifications = new OpenAPIGenerator(
            OpenAPIGeneratorOptions.load().setSeparateByModule(false))
            .generate(
                Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()),
                Path.of(ClassLoader.getSystemResource("OpenAPI-Components").toURI()));

        assertFalse(specifications.hasErrors(), () -> specifications.getMessagesCombined(","));
        assertEquals(1, specifications.getResult().size());

        OpenAPI openAPI = specifications.getResult().getFirst();
        Map<String, PathItem> paths = openAPI.getPaths();
        assertNotNull(paths);

        assertFalse(paths.containsKey("/observationvariables"),
            "paths must use pathItemNameFor /variables, not naive plural /observationvariables");
        assertFalse(paths.containsKey("/search/observationvariables"),
            "search paths must use pathItemNameFor /variables, not naive plural");

        for (String expectedPath : EXPECTED_PATHS) {
            assertTrue(paths.containsKey(expectedPath),
                "Missing ObservationVariable path: " + expectedPath + "; variable paths: "
                    + paths.keySet().stream().filter(p -> p.toLowerCase().contains("variable")).sorted().toList());
        }

        PathItem collection = paths.get("/variables");
        assertNotNull(collection.getGet(), "GET /variables");
        assertNotNull(collection.getPost(), "POST /variables");

        PathItem byId = paths.get("/variables/{observationVariableDbId}");
        assertNotNull(byId.getGet(), "GET /variables/{observationVariableDbId}");
        assertNotNull(byId.getPut(), "PUT /variables/{observationVariableDbId}");

        PathItem search = paths.get("/search/variables");
        assertNotNull(search.getPost(), "POST /search/variables");

        PathItem searchById = paths.get("/search/variables/{searchResultsDbId}");
        assertNotNull(searchById.getGet(), "GET /search/variables/{searchResultsDbId}");

        assertNotNull(openAPI.getComponents().getSchemas().get("ObservationVariableNewRequest"),
            "ObservationVariableNewRequest schema should be generated for primary model");
        assertNotNull(openAPI.getComponents().getSchemas().get("ObservationVariableSearchRequest"),
            "ObservationVariableSearchRequest schema should be generated for primary model");
    }
}
