package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphQLGeneratorOptions {

  QueryTypeOptions queryType;
  MutationTypeOptions mutationType;
  IdOptions ids;

  public static GraphQLGeneratorOptions load(Path optionsFile) throws IOException {
    return load(Files.newInputStream(optionsFile));
  }

  public static GraphQLGeneratorOptions load() {

    try {
      InputStream inputStream = GraphQLGeneratorOptions.class
              .getClassLoader()
              .getResourceAsStream("graphql-options.yaml");
      return load(inputStream);
    } catch (Exception e) { // The default options should be present on the classpath
      throw new RuntimeException(e);
    }
  }

  private static GraphQLGeneratorOptions load(InputStream inputStream) throws IOException {

    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    return mapper.readValue(inputStream, GraphQLGeneratorOptions.class);
  }

  public boolean isGeneratingQueryType() {
    return queryType != null && queryType.isGenerating();
  }

  public boolean isGeneratingSingleQueries() {
    return isGeneratingQueryType() && queryType.getSingleQuery().isGenerating();
  }

  public boolean isGeneratingListQueries() {
    return isGeneratingQueryType() && queryType.getListQuery().isGenerating();
  }

  public boolean isGeneratingSearchQueries() {
    return isGeneratingQueryType() && queryType.getSearchQuery().isGenerating();
  }

  public boolean isGeneratingMutationType() {
    return mutationType != null && mutationType.isGenerating();
  }

  public boolean isUsingIDType() {
    return ids != null && ids.isUsingIDType();
  }
}