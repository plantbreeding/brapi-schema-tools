package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
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

class GraphQLGeneratorOptionsTest {

    @Test
    void load() {
        GraphQLGeneratorOptions options = GraphQLGeneratorOptions.load();

        checkDefaultOptions(options);
    }

    @Test
    void loadJson() {
        GraphQLGeneratorOptions options = null;
        try {
            options = GraphQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("graphql-test-options.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void loadYaml() {
        GraphQLGeneratorOptions options = null;
        try {
            options = GraphQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("graphql-test-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultOptions(options);
    }

    @Test
    void overwrite() {
        GraphQLGeneratorOptions options = null;
        try {
            options = GraphQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("graphql-override-options.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkOptions(options);
        assertFalse(options.isUsingIDType());
        assertFalse(options.getQueryType().isPartitionedByCrop());
        assertEquals("Query2", options.getQueryType().getName());

        assertTrue(options.getQueryType().getSingleQuery().isGenerating());
        assertFalse(options.getQueryType().getListQuery().isGenerating());
        assertTrue(options.getQueryType().getSearchQuery().isGenerating());

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

    //@Test
    void compare() {
        try {
            GraphQLGeneratorOptions options1 = GraphQLGeneratorOptions.load() ;
            GraphQLGeneratorOptions options2 = GraphQLGeneratorOptions.load(Path.of(ClassLoader.getSystemResource("graphql-no-override-options.yaml").toURI()));

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

            ObjectWriter writer = objectMapper.writerWithDefaultPrettyPrinter();

            assertEquals(writer.writeValueAsString(options1), writer.writeValueAsString(options2));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
    private void checkDefaultOptions(GraphQLGeneratorOptions options) {
        checkOptions(options);

        assertTrue(options.isUsingIDType());
        assertTrue(options.getQueryType().isPartitionedByCrop());
        assertEquals("Query", options.getQueryType().getName());

        assertTrue(options.getQueryType().getSingleQuery().isGenerating());
        assertTrue(options.getQueryType().getListQuery().isGenerating());
        assertTrue(options.getQueryType().getSearchQuery().isGenerating());

        assertEquals(LinkType.SUB_QUERY,
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

    private void checkOptions(GraphQLGeneratorOptions options) {
        assertNotNull(options);

        Validation validation = options.validate();

        assertNotNull(validation);

        if (!validation.isValid()) {
            fail(validation.getAllErrorsMessage()) ;
        }

        assertTrue(options.isGeneratingQueryType());

        checkOptions(options.getProperties());
        checkOptions(options.getQueryType());

        checkOptions(options.getMutationType());
        checkOptions(options.getInput());

        assertNotNull(options.getQueryType());

        assertNotNull(options.getQueryType().getSingleQuery());

        assertTrue(options.isGeneratingMutationType());
        assertNotNull(options.getMutationType());
        assertEquals("Mutation", options.getMutationType().getName());
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

    private void checkOptions(QueryTypeOptions options) {
        assertNotNull(options);
        checkOptions(options.getSingleQuery()) ;
        checkOptions(options.getListQuery()) ;
        checkOptions(options.getSearchQuery()) ;
    }

    private void checkOptions(SingleQueryOptions options) {
        assertNotNull(options);
        assertEquals("Returns a Trial object by id", options.getDescriptionFor("Trial"));
    }

    private void checkOptions(ListQueryOptions options) {
        assertNotNull(options);
    }

    private void checkOptions(SearchQueryOptions options) {
        assertNotNull(options);
    }

    private void checkOptions(MutationTypeOptions options) {
        assertNotNull(options);
        assertEquals("Mutation", options.getName());
        checkOptions(options.getCreateMutation()) ;
        checkOptions(options.getUpdateMutation()) ;
        checkOptions(options.getDeleteMutation()) ;
    }

    private void checkOptions(CreateMutationOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("createTrial", options.getNameFor("Trial"));
    }

    private void checkOptions(UpdateMutationOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("updateTrial", options.getNameFor("Trial"));
    }

    private void checkOptions(DeleteMutationOptions options) {
        assertNotNull(options);
        assertFalse(options.isGenerating());
        assertEquals("deleteTrial", options.getNameFor("Trial"));
    }

    private void checkOptions(InputOptions options) {
        assertNotNull(options);
    }
}