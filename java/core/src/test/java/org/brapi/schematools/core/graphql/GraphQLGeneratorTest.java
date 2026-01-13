package org.brapi.schematools.core.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.schema.GraphQLSchema;
import io.swagger.v3.oas.models.OpenAPI;
import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.brapi.schematools.core.utils.OpenAPIUtils.OUTPUT_FORMAT_JSON;
import static org.brapi.schematools.core.utils.OpenAPIUtils.prettyPrint;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

        assertSchemaEquals("GraphQLGenerator/BrAPI-Schema.json", schema.getResult()) ;
    }

    private void assertSchemaEquals(String classPath, GraphQLSchema schema) {
        try {
            GraphQL graphQL = GraphQL.newGraphQL(schema).build();
            ExecutionResult executionResult = graphQL.execute(IntrospectionQuery.INTROSPECTION_QUERY);

            ObjectMapper mapper = new ObjectMapper();

            String expected = StringUtils.readStringFromPath(Path.of(ClassLoader.getSystemResource(classPath).toURI())).getResultOrThrow();
            String actual = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(executionResult.toSpecification().get("data")) ;

            // Parse both JSON strings into objects for order-insensitive comparison
            Object expectedObj = mapper.readTree(expected);
            Object actualObj = mapper.readTree(actual);

            if (!expectedObj.equals(actualObj)) {
                Path build = Paths.get("build/test-output", classPath);
                Files.createDirectories(build.getParent());
                Files.writeString(build, actual);
            }

            assertEquals(expectedObj, actualObj, "GraphQL Schema does not match (ignoring property order)");
        } catch (URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void printError(Response.Error error) {
        System.out.println(error.toString());
    }
}