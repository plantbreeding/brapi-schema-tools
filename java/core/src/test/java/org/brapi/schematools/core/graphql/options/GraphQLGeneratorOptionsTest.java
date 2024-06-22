package org.brapi.schematools.core.graphql.options;

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

        checkOptions(options);
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

        checkOptions(options);
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

        checkOptions(options);
    }

    private void checkOptions(GraphQLGeneratorOptions options) {
        assertNotNull(options);

        assertTrue(options.isGeneratingQueryType());

        checkOptions(options.getIds());
        checkOptions(options.getQueryType());
        checkOptions(options.getMutationType());
        checkOptions(options.getInput());

        assertNotNull(options.getQueryType());
        assertEquals("Query", options.getQueryType().getName());
        assertNotNull(options.getQueryType().getSingleQuery());

        assertTrue(options.isGeneratingMutationType());
        assertNotNull(options.getMutationType());
        assertEquals("Mutation", options.getMutationType().getName());

        assertNotNull(options.getIds());
        assertTrue(options.getIds().isUsingIDType());
    }

    private void checkOptions(IdsOptions options) {
        assertNotNull(options);
        assertTrue(options.isUsingIDType());
    }

    private void checkOptions(QueryTypeOptions options) {
        assertNotNull(options);
        assertTrue(options.isPartitionedByCrop());
        assertEquals("Query", options.getName());
        checkOptions(options.getSingleQuery()) ;
        checkOptions(options.getListQuery()) ;
        checkOptions(options.getSearchQuery()) ;
    }

    private void checkOptions(SingleQueryOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("Returns a Trial object by id", options.getDescriptionFor("Trial"));
    }

    private void checkOptions(ListQueryOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
    }

    private void checkOptions(SearchQueryOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
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