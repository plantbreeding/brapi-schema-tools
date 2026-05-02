package org.brapi.schematools.core.openapi.generator.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.brapischema.BrAPISchemaReader;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.options.OptionsTestBase;
import org.brapi.schematools.core.utils.BrAPIClassCacheBuilder;
import org.brapi.schematools.core.validiation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
class OpenAPIGeneratorOptionsTest extends OptionsTestBase {

    @BeforeEach
    void setUp() {
    }

    @Test
    void load() {
        OpenAPIGeneratorOptions options = OpenAPIGeneratorOptions.load();

        checkValidation(options) ;
        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
    }

    @SuppressWarnings("null")
    @Test
    void loadJson() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkValidation(options) ;
        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
    }

    @SuppressWarnings("null")
    @Test
    void loadYaml() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkDefaultOptions(options);

        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));
    }

    @Test
    void validateAgainstCache() {
        try {
            List<BrAPIClass> schemas = new BrAPISchemaReader()
                .readDirectories(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()))
                .onFailDoWithResponse(response -> fail(response.getMessagesCombined(",")))
                .getResult();

            Validation validation = OpenAPIGeneratorOptions.load().validateAgainstCache(BrAPIClassCacheBuilder.createCache(schemas));

            validation.getErrors().forEach(error -> log.error(error.getMessage()));

            assertTrue(validation.isValid());

        } catch (URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    void overwrite() {
        OpenAPIGeneratorOptions options = null;

        try {
            options = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        checkOptions(options);

        assertTrue(options.isGeneratingNewRequestFor("AlleleMatrix"));

        assertEquals("TrialNewRequest2", options.getNewRequestNameFor("Trial"));

        assertEquals("AlleleMatrix", options.getPluralFor("AlleleMatrix"));

        assertEquals("/pedigree2", options.getPathItemNameFor("PedigreeNode"));
        assertEquals("/pedigree2", options.getPathItemNameFor(BrAPIObjectType.builder().name("PedigreeNode").build()));
        assertTrue(options.getSingleGet().isGenerating());
        assertTrue(options.getSingleGet().isGeneratingFor("AlleleMatrix"));

        assertTrue(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("Get a filtered list of PedigreeNode X", options.getListGet().getSummaryFor("PedigreeNode"));

        assertEquals("Create new PedigreeNode X", options.getPost().getSummaryFor("PedigreeNode"));

        assertTrue(options.getPut().isGeneratingFor("BreedingMethod"));

        assertEquals(LinkType.ID,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.ID,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Trial").build(),
                BrAPIObjectProperty.builder().name("contacts").build()).getResultOrThrow()
        );
    }

    /**
     * Verifies that supplying {@code null} (~) as a map value in an override YAML
     * removes that entry from the base map, so subsequent lookups fall back to the
     * appropriate default.
     */
    @SuppressWarnings("null")
    @Test
    void overrideWithRemoval() {
        OpenAPIGeneratorOptions options = null;
        try {
            options = OpenAPIGeneratorOptions.load(
                Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-remove-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }

        // Validate the structure is still correct even after removals.
        checkValidation(options);

        // generateNewRequestFor.AlleleMatrix was false; after null-removal it falls back to
        // generateNewRequest: true → isGeneratingNewRequestFor should now return true.
        assertTrue(options.isGeneratingNewRequestFor("AlleleMatrix"),
            "generateNewRequestFor.AlleleMatrix removed: should fall back to generateNewRequest=true");

        // pluralFor.AlleleMatrix was "AlleleMatrix"; after null-removal the English inflector
        // produces "AlleleMatrices".
        assertEquals("AlleleMatrices", options.getPluralFor("AlleleMatrix"),
            "pluralFor.AlleleMatrix removed: should fall back to English inflector");

        // pathItemNameFor.PedigreeNode was "/pedigree"; after null-removal the computed default
        // is used → "/" + toLowerCase(getPluralFor("PedigreeNode")) = "/pedigreenodes".
        assertEquals("/pedigreenodes", options.getPathItemNameFor("PedigreeNode"),
            "pathItemNameFor.PedigreeNode removed: should fall back to computed default");

        // singleGet.generateFor.AlleleMatrix was false; after null-removal it falls back to
        // singleGet.generate: true.
        assertTrue(options.getSingleGet().isGeneratingFor("AlleleMatrix"),
            "singleGet.generateFor.AlleleMatrix removed: should fall back to generate=true");

        // search.paged.AlleleMatrix was false; after null-removal it falls back to
        // search.pagedDefault: true.
        assertTrue(options.getSearch().isPagedFor("AlleleMatrix"),
            "search.paged.AlleleMatrix removed: should fall back to pagedDefault=true");

        // listGet.propertyFromRequestFor.CallSet was removed (outer null).
        // Every property lookup for CallSet now falls back to propertiesFromRequest: true.
        assertTrue(options.getListGet().isUsingPropertyFromRequestFor("CallSet", "commonCropNames"),
            "listGet.propertyFromRequestFor.CallSet removed: commonCropNames should fall back to propertiesFromRequest=true");
        assertTrue(options.getListGet().isUsingPropertyFromRequestFor("CallSet", "germplasmNames"),
            "listGet.propertyFromRequestFor.CallSet removed: germplasmNames should fall back to propertiesFromRequest=true");

        // listGet.propertyFromRequestFor.Germplasm.familyCodes was false; after null-removal of
        // that inner key it falls back to propertiesFromRequest: true.
        assertTrue(options.getListGet().isUsingPropertyFromRequestFor("Germplasm", "familyCodes"),
            "listGet.propertyFromRequestFor.Germplasm.familyCodes removed: should fall back to propertiesFromRequest=true");
        // Other Germplasm entries that are still explicitly false should be unchanged.
        assertFalse(options.getListGet().isUsingPropertyFromRequestFor("Germplasm", "programNames"),
            "listGet.propertyFromRequestFor.Germplasm.programNames was not removed: should still be false");

        // put.multipleFor.Call was true; after null-removal it falls back to put.multiple: false.
        assertFalse(options.getPut().isMultipleFor("Call"),
            "put.multipleFor.Call removed: should fall back to multiple=false");

        // put.useAdditionalProperties.Cross was true; after null-removal it falls back to
        // put.multiple: false.
        assertFalse(options.getPut().isUsingAdditionalProperties("Cross"),
            "put.useAdditionalProperties.Cross removed: should fall back to multiple=false");

        // properties.linkTypeFor.CallSet was removed (outer null).
        // getLinkTypeFor falls back to the default relationship-type logic → EMBEDDED
        // (the test property has no relationship type set, which defaults to ONE_TO_ONE,
        //  and the dereferenced type is unknown here → EMBEDDED).
        assertEquals(LinkType.EMBEDDED,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow(),
            "properties.linkTypeFor.CallSet removed: should fall back to default EMBEDDED");

        // properties.linkTypeFor.BreedingMethod.germplasm was removed (inner null).
        // The BreedingMethod type entry still exists but germplasm key is gone → EMBEDDED.
        assertEquals(LinkType.EMBEDDED,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("germplasm").build()).getResultOrThrow(),
            "properties.linkTypeFor.BreedingMethod.germplasm removed: should fall back to default EMBEDDED");

        // Sibling entries that were NOT removed must retain their original values.
        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("pedigreeNodes").build()).getResultOrThrow(),
            "properties.linkTypeFor.BreedingMethod.pedigreeNodes was not removed: should still be NONE");
    }

    @Test
    void compare() {
        try {
            OpenAPIGeneratorOptions options1 = OpenAPIGeneratorOptions.load() ;
            OpenAPIGeneratorOptions options2 = OpenAPIGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("OpenAPIGenerator/openapi-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            log.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    private void checkDefaultOptions(OpenAPIGeneratorOptions options) {
        checkOptions(options);

        assertFalse(options.isGeneratingNewRequestFor("BreedingMethod"));
        assertFalse(options.isGeneratingEndpointNameWithIdFor("AlleleMatrix"));

        assertEquals("TrialNewRequest", options.getNewRequestNameFor("Trial"));

        assertEquals("AlleleMatrix", options.getPluralFor("AlleleMatrix"));

        assertEquals("/pedigree", options.getPathItemNameFor("PedigreeNode"));
        assertEquals("/pedigree", options.getPathItemNameFor(BrAPIObjectType.builder().name("PedigreeNode").build()));

        assertTrue(options.getSingleGet().isGenerating());
        assertFalse(options.getSingleGet().isGeneratingFor("AlleleMatrix"));

        assertEquals("Get a filtered list of PedigreeNode", options.getListGet().getSummaryFor("PedigreeNode"));

        assertEquals("Create new PedigreeNode", options.getPost().getSummaryFor("PedigreeNode"));

        assertFalse(options.getPut().isGeneratingFor("BreedingMethod"));

        assertEquals(LinkType.SUB_QUERY,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("CallSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.EMBEDDED,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Trial").build(),
                BrAPIObjectProperty.builder().name("contacts").build()).getResultOrThrow()
        );
    }

    private void checkOptions(OpenAPIGeneratorOptions options) {
        Validation validation = options.validate() ;
        assertNotNull(validation);
        if (!validation.isValid()) {
            log.error("Validation errors: {}", validation.getErrors());
        }
        assertTrue(validation.isValid()) ;

        assertNotNull(options);

        assertNotNull(options.getProperties());
        assertNotNull(options.getSingleGet());
        assertNotNull(options.getListGet());
        assertNotNull(options.getPost());
        assertNotNull(options.getPut());
        assertNotNull(options.getDelete());

        assertTrue(options.isSeparatingByModule()) ;

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
                BrAPIObjectProperty.builder().name("germplasm").build()).getResultOrThrow()
        );

        assertEquals(LinkType.NONE,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("pedigreeNodes").build()).getResultOrThrow()
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("Variant").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("calls").build()).getResultOrThrow()
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("callSets").build()).getResultOrThrow()
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getProperties().getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("variants").build()).getResultOrThrow()
        );
    }
}


