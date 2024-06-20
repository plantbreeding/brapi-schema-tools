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
    void defaultBuilder() {
        GraphQLGeneratorOptions options = GraphQLGeneratorOptions.defaultBuilder().build();

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
        assertTrue(options.getQueryType().isGenerating());
        assertEquals("Query", options.getQueryType().getName());
        assertNotNull(options.getQueryType().getSingleQuery());
        assertEquals("Returns a %s object by id", options.getQueryType().getSingleQuery().getDescriptionFormat());

        assertFalse(options.isGeneratingMutationType());
        assertNotNull(options.getMutationType());
        assertFalse(options.getMutationType().isGenerating());
        assertEquals("Mutation", options.getMutationType().getName());

        assertNotNull(options.getIds());
        assertTrue(options.getIds().isUsingIDType());
        assertEquals("%sDbId", options.getIds().getNameFormat());
    }

    private void checkOptions(IdsOptions options) {
        assertNotNull(options);
        assertTrue(options.isUsingIDType());
        assertEquals("%sDbId", options.getNameFormat());
    }

    private void checkOptions(QueryTypeOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertTrue(options.isPartitionedByCrop());
        assertEquals("Query", options.getName());
        checkOptions(options.getSingleQuery()) ;
        checkOptions(options.getListQuery()) ;
        checkOptions(options.getSearchQuery()) ;
    }

    private void checkOptions(SingleQueryOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("Returns a %s object by id", options.getDescriptionFormat());
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
        assertFalse(options.isGenerating());
        assertEquals("Mutation", options.getName());
        checkOptions(options.getCreateMutation()) ;
        checkOptions(options.getUpdateMutation()) ;
        checkOptions(options.getDeleteMutation()) ;
    }

    private void checkOptions(CreateMutationOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("create%s", options.getNameFormat());
        assertEquals("createTrial", options.getMutationNameFor("Trial"));
    }

    private void checkOptions(UpdateMutationOptions options) {
        assertNotNull(options);
        assertTrue(options.isGenerating());
        assertEquals("update%s", options.getNameFormat());
        assertEquals("updateTrial", options.getMutationNameFor("Trial"));
    }

    private void checkOptions(DeleteMutationOptions options) {
        assertNotNull(options);
        assertFalse(options.isGenerating());
        assertEquals("delete%s", options.getNameFormat());
        assertEquals("deleteTrial", options.getMutationNameFor("Trial"));
    }

    private void checkOptions(InputOptions options) {
        assertNotNull(options);
        assertEquals("input", options.getName());
        assertEquals("%sInput", options.getNameFormat());
        assertEquals("%sInput", options.getTypeNameFormat());
    }
}