package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
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

        assertTrue(options.isGeneratingNewRequestFor("AlleleMatrix"));

        assertEquals("TrialNewRequest2", options.getNewRequestNameFor("Trial"));

        assertEquals("AlleleMatrix", options.getPluralFor("AlleleMatrix"));

        assertEquals("pedigree", options.getPathItemNameFor("PedigreeNode"));
        assertEquals("pedigree", options.getPathItemNameFor(BrAPIObjectType.builder().name("PedigreeNode").build()));
        assertTrue(options.getSingleGet().isGenerating());
        assertTrue(options.getSingleGet().isGeneratingFor("AlleleMatrix"));

        assertTrue(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("Get a filtered list of PedigreeNode X", options.getListGet().getSummaryFor("PedigreeNode"));

        assertEquals("Create new PedigreeNode X", options.getPost().getSummaryFor("PedigreeNode"));

        assertTrue(options.getPut().isGeneratingFor("BreedingMethod"));

        assertEquals(LinkType.ID,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.ID,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Trial").build(),
                BrAPIObjectProperty.builder().name("contacts").build())
        );
    }

    @Test
    void compare() {
        try {
            OpenAPIGeneratorOptions options1 = OpenAPIGeneratorOptions.load() ;
            OpenAPIGeneratorOptions options2 = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("openapi-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void checkDefaultOptions(OpenAPIGeneratorOptions options) {
        checkOptions(options);

        assertFalse(options.isGeneratingNewRequestFor("BreedingMethod"));
        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("TrialNewRequest", options.getNewRequestNameFor("Trial"));

        assertEquals("AlleleMatrix", options.getPluralFor("AlleleMatrix"));

        assertEquals("/pedigrees", options.getPathItemNameFor("PedigreeNode"));
        assertEquals("/pedigrees", options.getPathItemNameFor(BrAPIObjectType.builder().name("PedigreeNode").build()));

        assertTrue(options.getSingleGet().isGenerating());
        assertFalse(options.getSingleGet().isGeneratingFor("AlleleMatrix"));

        assertEquals("Get a filtered list of PedigreeNode", options.getListGet().getSummaryFor("PedigreeNode"));

        assertEquals("Create new PedigreeNode", options.getPost().getSummaryFor("PedigreeNode"));

        assertFalse(options.getPut().isGeneratingFor("BreedingMethod"));

        assertEquals(LinkType.SUB_PATH,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.EMBEDDED,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Trial").build(),
                BrAPIObjectProperty.builder().name("contacts").build())
        );
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

        assertEquals("attributeDbId", options.getProperties().getIdPropertyNameFor("GermplasmAttribute")) ;

        assertEquals("/trials", options.getPathItemNameFor("Trial"));
        assertEquals("/trials", options.getPathItemNameFor(BrAPIObjectType.builder().name("Trial").build()));

        assertEquals("/studies", options.getPathItemNameFor("Study"));
        assertEquals("/studies", options.getPathItemNameFor(BrAPIObjectType.builder().name("Study").build()));

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("germplasm").build())
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("pedigreeNodes").build())
        );

        assertEquals(LinkType.SUB_PATH,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Variant").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.SUB_PATH,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.SUB_PATH,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("callSets").build())
        );

        assertEquals(LinkType.SUB_PATH,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("variants").build())
        );
    }
}