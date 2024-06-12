package org.brapi.schematools.core.graphql;

import graphql.schema.GraphQLSchema;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GraphQLGeneratorTest {

    @Test
    void generate() {
        Response<GraphQLSchema> schema = null;
        try {
            schema = new GraphQLGenerator().generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        assertNotNull(schema);

        schema.getAllErrors().forEach(this::printError);

        assertFalse(schema.hasErrors());
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}