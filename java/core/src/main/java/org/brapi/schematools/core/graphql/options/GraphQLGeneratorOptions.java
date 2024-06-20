package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.openapi.options.SearchOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Options for the {@link GraphQLGenerator}.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GraphQLGeneratorOptions {

    InputOptions input ;
    QueryTypeOptions queryType;
    MutationTypeOptions mutationType;
    IdsOptions ids;

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static GraphQLGeneratorOptions load(Path optionsFile) throws IOException {
        return load(Files.newInputStream(optionsFile));
    }

    /**
     * Load the default options
     * @return The default options
     */
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

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static GraphQLGeneratorOptions load(InputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        return mapper.readValue(inputStream, GraphQLGeneratorOptions.class);
    }

    /**
     * Creates a build class with the default options already loaded. This also for
     * ease of overriding programmatically only a few options from their defaults.
     * @return a build class with the default options already loaded.
     */
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

    /**
     * Determines if the Generator should generate the Query Type.
     * @return <code>true</code> if the Generator should generate the Query Type, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingQueryType() {
        return isGeneratingSingleQueries() || isGeneratingListQueries() || isGeneratingSearchQueries() ;
    }

    /**
     * Determines if the Generator should generate any single query. Returns <code>true</code> if
     * {@link QueryTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link SingleQueryOptions#generating} is set to <code>true</code> or
     * {@link SingleQueryOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any single query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSingleQueries() {
        return queryType != null && queryType.isGenerating()  && queryType.getSingleQuery() != null &&
            (queryType.getSingleQuery().isGenerating() || queryType.getSingleQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any list query. Returns <code>true</code> if
     * {@link QueryTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link ListQueryOptions#generating} is set to <code>true</code> or
     * {@link ListQueryOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any list query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingListQueries() {
        return queryType != null && queryType.isGenerating()  && queryType.getSingleQuery() != null &&
            (queryType.getListQuery().isGenerating() || queryType.getListQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any search query. Returns <code>true</code> if
     * {@link QueryTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link SearchQueryOptions#generating} is set to <code>true</code> or
     * {@link SearchQueryOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any search query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearchQueries() {
        return queryType != null && queryType.isGenerating() && queryType.getSingleQuery() != null &&
            (queryType.getSearchQuery().isGenerating() || queryType.getSearchQuery().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate the Mutation Type.
     * @return <code>true</code> if the Generator should generate the Mutation Type, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingMutationType() {
        return isGeneratingCreateMutation() || isGeneratingUpdateMutation() || isGeneratingDeleteMutation();
    }

    /**
     * Determines if the Generator should generate the Create mutations. Returns <code>true</code> if
     * {@link MutationTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link CreateMutationOptions#generating} is set to <code>true</code> or
     * {@link CreateMutationOptions#generatingFor}  is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate New mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingCreateMutation() {
        return mutationType != null && mutationType.isGenerating() && mutationType.getCreateMutation() != null &&
            (mutationType.getCreateMutation().isGenerating() || mutationType.getCreateMutation().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate the Create mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link MutationTypeOptions#generating} is set to <code>true</code> AND
     * one of {@link CreateMutationOptions#isGenerating()} is set to <code>true</code> or
     * {@link CreateMutationOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Create mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingCreateMutationFor(String name) {
        return mutationType != null && mutationType.isGenerating() && mutationType.getCreateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Update mutations. Returns <code>true</code> if
     * {@link MutationTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link UpdateMutationOptions#generating} is set to <code>true</code> or
     * {@link UpdateMutationOptions#generatingFor}  is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate Update mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingUpdateMutation() {
        return mutationType != null && mutationType.isGenerating() && mutationType.getUpdateMutation() != null &&
            (mutationType.getUpdateMutation().isGenerating() || mutationType.getUpdateMutation().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate the Update mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link MutationTypeOptions#generating} is set to <code>true</code> AND
     * one of {@link UpdateMutationOptions#isGenerating()} is set to <code>true</code> or
     * {@link UpdateMutationOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Update mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingUpdateMutationFor(String name) {
        return mutationType != null && mutationType.isGenerating() && mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Delete mutations. Returns <code>true</code> if
     * {@link MutationTypeOptions#generating} is set to <code>true</code> AND one of
     * {@link DeleteMutationOptions#generating} is set to <code>true</code> or
     * {@link DeleteMutationOptions#generatingFor}  is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate Delete mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDeleteMutation() {
        return mutationType != null && mutationType.isGenerating() && mutationType.getUpdateMutation() != null &&
            (mutationType.getUpdateMutation().isGenerating() || mutationType.getUpdateMutation().getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate the Delete mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link MutationTypeOptions#generating} is set to <code>true</code> AND
     * one of {@link DeleteMutationOptions#isGenerating()} is set to <code>true</code> or
     * {@link DeleteMutationOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Delete mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDeleteMutationFor(String name) {
        return mutationType != null && mutationType.isGenerating() && mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the built-in GraphQLID type should be used for IDs instead of GraphQLString
     * @return <code>true</code> if the built-in GraphQLID type should be used for IDs instead of GraphQLString, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isUsingIDType() {
        return ids != null && ids.isUsingIDType();
    }

    /**
     *
     */
    public static class GraphQLGeneratorOptionsBuilder{}
}