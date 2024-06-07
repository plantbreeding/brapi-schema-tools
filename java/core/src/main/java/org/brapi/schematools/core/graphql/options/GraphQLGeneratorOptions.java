package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphQLGeneratorOptions {

    QueryTypeOptions queryType;
    MutationTypeOptions mutationType;
    IdsOptions ids;

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

    public static GraphQLGeneratorOptions.GraphQLGeneratorOptionsBuilder defaultBuilder() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            GraphQLGeneratorOptions deepCopy = objectMapper
                .readValue(objectMapper.writeValueAsString(load()), GraphQLGeneratorOptions.class);

            return deepCopy.toBuilder();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonIgnore
    public boolean isGeneratingQueryType() {
        return queryType != null && queryType.isGenerating();
    }

    @JsonIgnore
    public boolean isGeneratingSingleQueries() {
        return isGeneratingQueryType() && (queryType.getSingleQuery().isGenerating() || queryType.getSingleQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingListQueries() {
        return isGeneratingQueryType() && (queryType.getListQuery().isGenerating() || queryType.getListQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingSearchQueries() {
        return isGeneratingQueryType() && (queryType.getSearchQuery().isGenerating() || queryType.getSearchQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingMutationType() {
        return mutationType != null && (mutationType.isGenerating() || mutationType.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isUsingIDType() {
        return ids != null && ids.isUsingIDType();
    }

    public static class GraphQLGeneratorOptionsBuilder{}
}