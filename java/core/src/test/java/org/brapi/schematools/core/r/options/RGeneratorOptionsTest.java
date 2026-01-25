package org.brapi.schematools.core.r.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.validiation.Validation;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class RGeneratorOptionsTest {
    @Test
    void load() {
        RGeneratorOptions options = RGeneratorOptions.load();

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;

        checkDefaultOptions(options);
    }

    @Test
    void load2() {
        RGeneratorOptions options = RGeneratorOptions.load().setOverwrite(true);

        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        checkOverrideOptions(options);
    }

    @Test
    void loadJson() {
        RGeneratorOptions options = null;
        try {
            options = RGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("RGenerator/test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        RGeneratorOptions options = null;
        try {
            options = RGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("RGenerator/test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        RGeneratorOptions options = null;

        try {
            options = RGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("RGenerator/override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertEquals("AlleleMatrix", options.getPluralFor("AlleleMatrix"));

        assertEquals("/pedigree", options.getPathItemNameFor("PedigreeNode"));
        assertEquals("/pedigree", options.getPathItemNameFor(BrAPIObjectType.builder().name("PedigreeNode").build()));
        assertTrue(options.getSingleGet().isGenerating());
        assertFalse(options.getSingleGet().isGeneratingFor("AlleleMatrix"));

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

    @Test
    void compare() {
        try {
            RGeneratorOptions options1 = RGeneratorOptions.load() ;
            RGeneratorOptions options2 = RGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("RGenerator/no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private void checkDefaultOptions(RGeneratorOptions options) {
        checkOptions(options);

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

    private void checkOptions(RGeneratorOptions options) {
        assertNotNull(options.validate());
        assertTrue(options.validate().isValid()) ;

        assertNotNull(options);

        assertNotNull(options.getProperties());
        assertNotNull(options.getSingleGet());
        assertNotNull(options.getListGet());
        assertNotNull(options.getPost());
        assertNotNull(options.getPut());
        assertNotNull(options.getDelete());

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

    private void checkOverrideOptions(RGeneratorOptions options) {
        checkOptions(options);

        assertTrue(options.isOverwritingExistingFiles());
    }
}