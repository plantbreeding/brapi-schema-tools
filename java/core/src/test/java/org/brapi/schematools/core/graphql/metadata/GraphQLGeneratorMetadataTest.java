package org.brapi.schematools.core.graphql.metadata;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphQLGeneratorMetadataTest {

    void load() {
        GraphQLGeneratorMetadata metadata = GraphQLGeneratorMetadata.load();

        checkMetadata(metadata);
    }

    @Test
    void loadJson() {
    }

    @Test
    void loadYaml() {
    }

    private void checkMetadata(GraphQLGeneratorMetadata metadata) {
        assertNotNull(metadata);

        assertEquals("BrAPI", metadata.getTitle());
        assertEquals("0.0.0", metadata.getVersion());
    }
}