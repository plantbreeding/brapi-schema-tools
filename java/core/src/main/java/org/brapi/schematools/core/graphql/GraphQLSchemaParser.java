package org.brapi.schematools.core.graphql;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.introspection.IntrospectionQuery;
import graphql.introspection.IntrospectionResultToSchema;
import graphql.language.Document;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.SchemaPrinter;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.UnExecutableSchemaGenerator;

import java.util.Map;

/**
 * Parser for converting the result of a introspection query into a GraphQLSchema,
 * and vice versa.
 */
public class GraphQLSchemaParser {
    private final ObjectMapper objectMapper;

    /**
     * Creates a new Parser with a standard ObjectMapper
     */
    public GraphQLSchemaParser() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Creates a new Parser with a predefined ObjectMapper
     * @param mapper a predefined ObjectMapper
     */
    public GraphQLSchemaParser(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    /**
     * Parses the result of an introspection query into a GraphQLSchema. Note
     * the schema is not executable.
     * @param jsonSchema the result of an introspection query into a GraphQLSchema
     * @return A GraphQLSchema generated from the result of an introspection query
     * @throws JsonProcessingException if there is a problem reading the schema as a valid
     * JSON object.
     */
    public GraphQLSchema parseJsonSchema(String jsonSchema) throws JsonProcessingException {
        IntrospectionResultToSchema introspectionResultToSchema = new IntrospectionResultToSchema();

        Map<String, Object> map = objectMapper.readValue(jsonSchema, Map.class);

        Document document = introspectionResultToSchema.createSchemaDefinition((Map<String, Object>) map.get("data"));

        String sdl = new SchemaPrinter().print(document);

        TypeDefinitionRegistry registry = new SchemaParser().parse(sdl);

        return UnExecutableSchemaGenerator.makeUnExecutableSchema(registry);
    }

    /**
     * Writes the schema to a String.
     * @param schema A GraphQLSchema generated from the result of an introspection query
     * @return A string version of the JSON representing a GraphQLSchema generated from the result of an introspection query
     * @throws JsonProcessingException if there is a problem writing the schema as a valid
     * JSON object.
     */
    public String writeSchemaToString(GraphQLSchema schema) throws JsonProcessingException {

        GraphQL graphQL = GraphQL.newGraphQL(schema).build();

        ExecutionInput executionInput = ExecutionInput.newExecutionInput().query(IntrospectionQuery.INTROSPECTION_QUERY)
            .build();
        ExecutionResult execute = graphQL.execute(executionInput);

        return objectMapper.writeValueAsString(execute.toSpecification());
    }
}
