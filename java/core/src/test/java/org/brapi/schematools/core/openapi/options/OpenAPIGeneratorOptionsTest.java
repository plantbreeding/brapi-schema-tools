package org.brapi.schematools.core.openapi.options;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OpenAPIGeneratorOptionsTest {

    @BeforeEach
    void setUp() {
    }

    @Test
    void load() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();

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

    @Test
    void isGeneratingSingleGet() {
    }

    @Test
    void isGeneratingListGet() {
    }

    @Test
    void isGeneratingSearch() {
    }

    @Test
    void isGeneratingPost() {
    }

    @Test
    void isGeneratingPut() {
    }

    @Test
    void isGeneratingDelete() {
    }

    @Test
    void isGeneratingEndpoint() {
    }

    @Test
    void isGeneratingEndpointWithId() {
    }

    @Test
    void isGeneratingSearchEndpoint() {
    }

    @Test
    void isGeneratingSingleGetEndpointFor() {
    }

    @Test
    void isGeneratingListGetEndpointFor() {
    }

    @Test
    void isGeneratingPostEndpointFor() {
    }

    @Test
    void isGeneratingPutEndpointFor() {
    }

    @Test
    void isGeneratingDeleteEndpointFor() {
    }

    @Test
    void isGeneratingSearchEndpointFor() {
    }

    @Test
    void isGeneratingEndpointFor() {
    }

    @Test
    void isGeneratingEndpointNameWithIdFor() {
    }

    @Test
    void isGeneratingNewRequestFor() {
    }

    @Test
    void getNewRequestNameFor() {
    }

    @Test
    void isGeneratingListResponseFor() {
    }

    @Test
    void getSingleResponseNameFor() {
    }

    @Test
    void getListResponseNameFor() {
    }

    @Test
    void isGeneratingSearchRequestFor() {
    }

    @Test
    void getSearchRequestNameFor() {
    }

    @Test
    void getPluralFor() {
    }

    @Test
    void getSingularForProperty() {
    }

    @Test
    void isSeparatingByModule() {
    }

    @Test
    void getSingleGet() {
    }

    @Test
    void getListGet() {
    }

    @Test
    void getPost() {
    }

    @Test
    void getPut() {
    }

    @Test
    void getDelete() {
    }

    @Test
    void getSearch() {
    }

    @Test
    void getIds() {
    }

    @Test
    void isCreatingNewRequest() {
    }

    @Test
    void getCreatingNewRequestFor() {
    }

    @Test
    void getNewRequestNameFormat() {
    }

    @Test
    void getSingleResponseNameFormat() {
    }

    @Test
    void getListResponseNameFormat() {
    }

    @Test
    void getSearchRequestNameFormat() {
    }

    @Test
    void builder() {
    }

    @Test
    void toBuilder() {
    }

    private void checkOptions(OpenAPIGeneratorOptions options) {
        assertNotNull(options);

        assertNotNull(options.getIds());
        assertNotNull(options.getSingleGet());
        assertNotNull(options.getListGet());
        assertNotNull(options.getPost());
        assertNotNull(options.getPut());
        assertNotNull(options.getDelete());

        assertFalse(options.isSeparatingByModule()) ;

        assertTrue(options.isGeneratingEndpoint()) ;
        assertTrue(options.isGeneratingEndpointFor("Trial"));
        assertFalse(options.isGeneratingEndpointFor("AlleleMatrix"));

        assertTrue(options.isGeneratingEndpointWithId()) ;
        assertTrue(options.isGeneratingEndpointNameWithIdFor("Trial"));
        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertTrue(options.isGeneratingNewRequestFor("Trial"));
        assertFalse(options.isGeneratingNewRequestFor("BreedingMethod"));

        assertTrue(options.isGeneratingEndpointNameWithIdFor("Trial"));
        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("TrialNewRequest", options.getNewRequestNameFor("Trial"));
        assertEquals("TrialSingleResponse", options.getSingleResponseNameFor("Trial"));
        assertEquals("TrialListResponse", options.getListResponseNameFor("Trial"));
        assertEquals("TrialSearchRequest", options.getSearchRequestNameFor("Trial"));

        assertEquals("Trials", options.getPluralFor("Trial"));
        assertEquals("Studies", options.getPluralFor("Study"));

        assertEquals("trial", options.getSingularForProperty("trials"));
        assertEquals("study", options.getSingularForProperty("studies"));
        assertEquals("trialDbId", options.getSingularForProperty("trialDbIds"));
        assertEquals("studyDbId", options.getSingularForProperty("studyDbIds"));
    }
}