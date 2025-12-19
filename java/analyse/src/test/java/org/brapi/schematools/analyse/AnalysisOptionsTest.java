package org.brapi.schematools.analyse;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.graphql.options.IdsOptions;
import org.brapi.schematools.core.options.LinkType;
import org.brapi.schematools.core.graphql.options.PropertiesOptions;
import org.brapi.schematools.core.model.BrAPIObjectProperty;
import org.brapi.schematools.core.model.BrAPIObjectType;
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

class AnalysisOptionsTest {

    @Test
    void load() {
        AnalysisOptions options = AnalysisOptions.load();

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        AnalysisOptions options = null;
        try {
            options = AnalysisOptions.load(Path.of(ClassLoader.getSystemResource("analyse-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        AnalysisOptions options = null;
        try {
            options = AnalysisOptions.load(Path.of(ClassLoader.getSystemResource("analyse-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);

        assertTrue(options.isPartitionedByCrop());

        assertFalse(options.isAnalysingGetForEntity("AlleleMatrix"));
        assertFalse(options.isAnalysingListForEntity("AlleleMatrix"));
        assertFalse(options.isAnalysingSearchForEntity("AlleleMatrix"));

        assertFalse(options.isAnalysingGetForEntity("Sample"));
        assertFalse(options.isAnalysingListForEntity("Sample"));
        assertFalse(options.isAnalysingSearchForEntity("Sample"));

        assertEquals(options.getProperties().getIdPropertyNameFor("CultivarAttribute"), "attributeDbId");
        assertEquals(options.getProperties().getIdPropertyNameFor("LocaleAttribute"), "attributeDbId");
        assertEquals(options.getProperties().getIdPropertyNameFor("VarietyAttributeValue"), "attributeValueDbId");

        assertEquals(options.getProperties().getName().getPropertyNameFor("CultivarAttribute"), "attributeName");
        assertEquals(options.getProperties().getName().getPropertyNameFor("LocaleAttribute"), "attributeName");
        assertEquals(options.getProperties().getName().getPropertyNameFor("VarietyAttributeValue"), "attributeValueName");

        assertEquals(options.getProperties().getPui().getPropertyNameFor("CultivarAttribute"), "attributePUI");
        assertEquals(options.getProperties().getPui().getPropertyNameFor("LocaleAttribute"), "attributePUI");
        assertEquals(options.getProperties().getPui().getPropertyNameFor("VarietyAttributeValue"), "attributeValuePUI");

        assertTrue(options.isAnalysingGetForEntity("VendorPlateSubmission"));
        assertTrue(options.isAnalysingListForEntity("VendorPlateSubmission"));
        assertTrue(options.isAnalysingListForEntity("VendorPlateSubmission"));

        assertEquals(0, options.getGetEntity().getRequiredParametersFor("Attribute").size()) ;
        assertEquals(1, options.getListEntity().getRequiredParametersFor("Attribute").size()) ;
    }

    //@Test
    void compare() {
        try {
            GraphQLGeneratorOptions options1 = GraphQLGeneratorOptions.load() ;
            GraphQLGeneratorOptions options2 = GraphQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("analyse-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultOptions(AnalysisOptions options) {
        checkOptions(options);

        assertFalse(options.isPartitionedByCrop());

        assertTrue(options.isAnalysingGetForEntity("AlleleMatrix"));
        assertTrue(options.isAnalysingListForEntity("AlleleMatrix"));
        assertTrue(options.isAnalysingSearchForEntity("AlleleMatrix"));

        assertFalse(options.isAnalysingGetForEntity("VendorPlateSubmission"));
        assertFalse(options.isAnalysingListForEntity("VendorPlateSubmission"));
        assertFalse(options.isAnalysingListForEntity("VendorPlateSubmission"));

        assertEquals(0, options.getGetEntity().getRequiredParametersFor("Attribute").size()) ;
        assertEquals(0, options.getListEntity().getRequiredParametersFor("Attribute").size()) ;
    }

    private void checkOptions(AnalysisOptions options) {
        assertNotNull(options);

        Validation validation = options.validate();

        assertNotNull(validation);

        if (!validation.isValid()) {
            fail(validation.getAllErrorsMessage()) ;
        }

        assertFalse(options.isAnalysingDepreciated());

        assertTrue(options.isAnalysingGetForEntity("Germplasm"));
        assertTrue(options.isAnalysingGetForEntity("Trial"));
        assertFalse(options.isAnalysingGetForEntity("VendorOrder"));

        assertTrue(options.isAnalysingListForEntity("Germplasm"));
        assertTrue(options.isAnalysingListForEntity("Trial"));
        assertFalse(options.isAnalysingListForEntity("VendorOrder"));

        assertTrue(options.isAnalysingListForEntity("Germplasm"));
        assertTrue(options.isAnalysingListForEntity("Trial"));
        assertFalse(options.isAnalysingListForEntity("VendorOrder"));

        assertEquals(options.getProperties().getIdPropertyNameFor("GermplasmAttribute"), "attributeDbId");

        assertEquals(options.getProperties().getName().getPropertyNameFor("GermplasmAttribute"), "attributeName");

        assertEquals(options.getProperties().getPui().getPropertyNameFor("GermplasmAttribute"), "attributePUI");
    }

    private void checkOptions(PropertiesOptions options) {
        assertNotNull(options);
        checkOptions(options.getIds());

        assertEquals(LinkType.NONE,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("germplasm").build())
        );

        assertEquals(LinkType.NONE,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("BreedingMethod").build(),
                BrAPIObjectProperty.builder().name("pedigreeNodes").build())
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("Variant").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("calls").build())
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("callSets").build())
        );

        assertEquals(LinkType.SUB_QUERY,
            options.getLinkTypeFor(
                BrAPIObjectType.builder().name("VariantSet").build(),
                BrAPIObjectProperty.builder().name("variants").build())
        );
    }

    private void checkOptions(IdsOptions options) {
        assertNotNull(options);

        assertEquals("attributeDbId", options.getIDFieldFor("GermplasmAttribute")) ;
    }
}