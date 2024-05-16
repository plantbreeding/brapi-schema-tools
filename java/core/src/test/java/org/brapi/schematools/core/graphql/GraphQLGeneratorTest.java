package org.brapi.schematools.core.graphql;

import graphql.schema.GraphQLSchema;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.graphql.options.GraphQLGeneratorOptions;
import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class GraphQLGeneratorTest {

  @Test
  void generate() {
    Response<GraphQLSchema> schema = null;
    try {
      schema = new GraphQLGenerator().generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), GraphQLGeneratorOptions.load());
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
    assertNotNull(schema) ;

    schema.getAllErrors().forEach(this::printError);

    assertFalse(schema.hasErrors()); ;
  }

  private void printError(Response.Error error) {
    System.out.println(error.toString());
  }
}