package org.brapi.schematools.core.graphql.options;

import lombok.Getter;
import lombok.Setter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter
public class GraphQLGeneratorOptions {
  QueryTypeOptions queryType ;
  MutationTypeOptions mutationType ;
  IdOptions ids ;

  public static GraphQLGeneratorOptions load(Path optionsFile) throws IOException {
    return load(Files.newInputStream(optionsFile));
  }

  public static GraphQLGeneratorOptions load() {
    InputStream inputStream = GraphQLGeneratorOptions.class
            .getClassLoader()
            .getResourceAsStream("graphql-options.yaml");
    return load(inputStream);
  }

  private static GraphQLGeneratorOptions load(InputStream inputStream) {
    Yaml yaml = new Yaml();

    return yaml.loadAs(inputStream, GraphQLGeneratorOptions.class);
  }

  public boolean isGeneratingQueryType() {
    return queryType != null && queryType.isGenerate() ;
  }

  public boolean isGeneratingMutationType() {
    return mutationType != null && mutationType.isGenerate() ;
  }
}