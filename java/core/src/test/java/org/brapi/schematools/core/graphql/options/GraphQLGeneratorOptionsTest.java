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
}