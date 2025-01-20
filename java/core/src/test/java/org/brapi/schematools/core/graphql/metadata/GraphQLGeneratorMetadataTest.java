package org.brapi.schematools.core.graphql.metadata;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

class GraphQLGeneratorMetadataTest {

    void load() {
        GraphQLGeneratorMetadata metadata = GraphQLGeneratorMetadata.load();

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadJson() {
        GraphQLGeneratorMetadata metadata = null;
        try {
            metadata = GraphQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("graphql-test-metadata.json").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void loadYaml() {
        GraphQLGeneratorMetadata metadata = null;
        try {
            metadata = GraphQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("graphql-test-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkDefaultMetadata(metadata);
    }

    @Test
    void overwrite() {
        GraphQLGeneratorMetadata metadata = null;
        try {
            metadata = GraphQLGeneratorMetadata.load(Path.of(ClassLoader.getSystemResource("graphql-override-metadata.yaml").toURI()));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

        checkMetadata(metadata);

        assertEquals("3.2.1", metadata.getVersion());
    }

    private void checkDefaultMetadata(GraphQLGeneratorMetadata metadata) {
        checkMetadata(metadata);

        assertEquals("0.0.0", metadata.getVersion());
    }

    private void checkMetadata(GraphQLGeneratorMetadata metadata) {
        assertNotNull(metadata);
        assertEquals("BrAPI", metadata.getTitle());
    }
}