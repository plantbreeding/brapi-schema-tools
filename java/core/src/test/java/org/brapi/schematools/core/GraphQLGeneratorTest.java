package org.brapi.schematools.core;

import graphql.schema.GraphQLSchema;
import org.brapi.schematools.core.model.BrAPISchema;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class GraphQLGeneratorTest {

  @Test
  void generate() {
    Response<GraphQLSchema> schema = null;
    try {
      schema = new GraphQLGenerator().generate(Path.of(ClassLoader.getSystemResource("BrAPI-Schema").toURI()), GraphQLGenerator.Options.builder().build());
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