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

        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
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

        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
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

        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
    }

    @Test
    void overwrite() {
        OpenAPIGeneratorOptions options = null;

        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertTrue(options.isGeneratingNewRequestFor("BreedingMethod"));
        assertTrue(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("TrialNewRequest2", options.getNewRequestNameFor("Trial"));

        assertEquals("attributeDbId", options.getProperties().getIdPropertyNameFor("GermplasmAttribute")) ;
    }

    private void checkDefaultOptions(OpenAPIGeneratorOptions options) {
        checkOptions(options);

        assertFalse(options.isGeneratingNewRequestFor("BreedingMethod"));
        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("TrialNewRequest", options.getNewRequestNameFor("Trial"));
    }

    private void checkOptions(OpenAPIGeneratorOptions options) {
        assertNotNull(options.validate());
        assertTrue(options.validate().isValid()) ;

        assertNotNull(options);

        assertNotNull(options.getProperties());
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

        assertTrue(options.isGeneratingNewRequestFor("Trial"));

        assertTrue(options.isGeneratingEndpointNameWithIdFor("Trial"));

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