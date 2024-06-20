package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIGeneratorOptionsTest {
    @Test
    void load() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();

        checkOptions(options);
    }

    @Test
    void defaultBuilder() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.defaultBuilder().build();

        checkOptions(options);
    }


    @Test
    void loadJson() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);
    }

    @Test
    void loadYaml() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);
    }

    private void checkOptions(OpenAPIGeneratorOptions options) {
        assertNotNull(options);

        checkOptions(options.getIds());
        checkOptions(options.getSingleGet());
        checkOptions(options.getListGet());
        checkOptions(options.getPost());
        checkOptions(options.getPut());
        checkOptions(options.getDelete());

        assertTrue(options.isGeneratingSingleGet()) ;
        assertTrue(options.isGeneratingListGet()) ;
        assertTrue(options.isGeneratingSearch()) ;
        assertTrue(options.isGeneratingPost()) ;
        assertTrue(options.isGeneratingPut()) ;
        assertFalse(options.isGeneratingDelete()); ;
        assertTrue(options.isGeneratingEndpoint());
        assertTrue(options.isGeneratingEndpointWithId());
        assertTrue(options.isGeneratingSearchEndpoint());

        assertTrue(options.isGeneratingSingleGetEndpointFor("Trial"));
        assertFalse(options.isGeneratingSingleGetEndpointFor("AlleleMatrix"));
        assertTrue(options.isGeneratingListGetEndpointFor("Trial"));
        assertFalse(options.isGeneratingListGetEndpointFor("AlleleMatrix"));
        assertTrue(options.isGeneratingPostEndpointFor("Trial"));
        assertTrue(options.isGeneratingPutEndpointFor("Trial"));
        assertFalse(options.isGeneratingDeleteEndpointFor("Trial"));
        assertTrue(options.isGeneratingSearchEndpointFor("Trial"));
        assertTrue(options.isGeneratingEndpointFor("Trial"));
        assertTrue(options.isGeneratingEndpointNameWithIdFor("Trial"));
        assertTrue(options.isGeneratingNewRequestFor("Trial"));
        assertEquals("TrialNewRequest", options.getNewRequestNameFor("Trial"));
        assertTrue(options.isGeneratingListResponseFor("Trial"));
        assertEquals("TrialSingleResponse", options.getSingleResponseNameFor("Trial"));
        assertEquals("TrialListResponse", options.getListResponseNameFor("Trial"));
        assertTrue(options.isGeneratingSearchRequestFor("Trial"));
        assertEquals("TrialSearchRequest", options.getSearchRequestNameFor("Trial"));
        assertEquals("Trials", options.getPluralFor("Trial"));
        assertEquals("Studies", options.getPluralFor("Study"));
        assertEquals("trialDbId", options.getSingularForProperty("trialDbIds"));
    }

    private void checkOptions(IdsOptions options) {
        assertNotNull(options);
    }

    private void checkOptions(SingleGetOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertTrue(options.isGeneratingFor("Trial"));
        assertTrue(options.isGeneratingFor("BreedingMethod"));
        assertFalse(options.isGeneratingFor("AlleleMatrix"));
    }

    private void checkOptions(PostOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertTrue(options.isGeneratingFor("Trial"));
        assertTrue(options.isGeneratingFor("BreedingMethod"));
        assertFalse(options.isGeneratingFor("AlleleMatrix"));
    }

    private void checkOptions(PutOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertTrue(options.isGeneratingFor("Trial"));
        assertTrue(options.isGeneratingFor("BreedingMethod"));
        assertFalse(options.isGeneratingFor("AlleleMatrix"));
    }

    private void checkOptions(ListGetOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertTrue(options.isGeneratingFor("Trial"));
        assertTrue(options.isGeneratingFor("BreedingMethod"));
        assertFalse(options.isGeneratingFor("AlleleMatrix"));
    }

    private void checkOptions(DeleteOptions options) {
        assertNotNull(options);
        assertFalse(options.isGenerating());
        assertFalse(options.isGeneratingFor("Trial"));
        assertFalse(options.isGeneratingFor("BreedingMethod"));
        assertFalse(options.isGeneratingFor("AlleleMatrix"));
    }

}