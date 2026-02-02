package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractMainGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Options for the {@link GraphQLGenerator}.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class GraphQLGeneratorOptions extends AbstractMainGeneratorOptions {

    private InputOptions input ;
    private QueryTypeOptions queryType;
    private MutationTypeOptions mutationType;
    private PropertiesOptions properties;
    private Boolean mergeOneOfType;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> mergingOneOfTypeFor = new HashMap<>();

    /**
     * Load the default options
     * @return The default options
     */
    public static GraphQLGeneratorOptions load() {
        try {
            GraphQLGeneratorOptions options = ConfigurationUtils.load("graphql-options.yaml", GraphQLGeneratorOptions.class);

            loadBrAPISchemaReaderOptions(options) ;

            return options ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static GraphQLGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, GraphQLGeneratorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static GraphQLGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, GraphQLGeneratorOptions .class)) ;
    }

    public Validation validate() {

        return super.validate()
            .assertNotNull(input, "Input Options are null")
            .assertNotNull(queryType,  "Query Options are null")
            .assertNotNull(mutationType, "Mutation Options are null")
            .assertNotNull(properties,  "Properties Options are null")
            .assertNotNull(mergeOneOfType, "'mergeOneOfType' option on %s is null", this.getClass().getSimpleName())
            .merge(input)
            .merge(queryType)
            .merge(mutationType)
            .merge(properties) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public GraphQLGeneratorOptions override(GraphQLGeneratorOptions overrideOptions) {
        super.override(overrideOptions) ;

        if (overrideOptions.input != null) {
            input.override(overrideOptions.input) ;
        }

        if (overrideOptions.queryType != null) {
            queryType.override(overrideOptions.queryType) ;
        }

        if (overrideOptions.mutationType != null) {
            mutationType.override(overrideOptions.mutationType) ;
        }

        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.properties) ;
        }

        if (overrideOptions.mergeOneOfType != null) {
            setMergeOneOfType(overrideOptions.mergeOneOfType) ;
        }

        mergingOneOfTypeFor.putAll(overrideOptions.mergingOneOfTypeFor);

        return this ;
    }

    /**
     * Determines if the Generator should generate the Query Type.
     * @return {@code true} if the Generator should generate the Query Type, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingQueryType() {
        return isGeneratingSingleQueries() || isGeneratingListQueries() || isGeneratingSearchQueries() ;
    }

    /**
     * Determines if the Generator should generate any single query. Returns {@code true} if
     * {@link SingleQueryOptions#isGenerating} is set to {@code true} or
     * @return {@code true} if the Generator should generate any single query, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingSingleQueries() {
        return queryType.getSingleQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the single query for a specific Primary Model.
     * Returns {@code true} if {@link SingleQueryOptions#isGeneratingFor(String)} is set to {@code true} for the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate single query for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingSingleQueryFor(String name) {
        return queryType.getSingleQuery().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate any List Query. Returns {@code true} if
     * {@link ListQueryOptions#isGenerating} is set to {@code true} for any type
     * @return {@code true} if the Generator should generate any List Query, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingListQueries() {
        return queryType.getListQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the List Query for a specific Primary Model.
     * Returns {@code true} if {@link ListQueryOptions#isGeneratingFor(String)} is set to {@code true} for the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate List Query for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingListQueryFor(String name) {
        return queryType.getListQuery().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate any Search Query. Returns {@code true} if
     * {@link SearchQueryOptions#isGenerating} is set to {@code true}
     * @return {@code true} if the Generator should generate any Search Query, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingSearchQueries() {
        return queryType.getSearchQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Search Query for a specific Primary Model.
     * Returns {@code true} if {@link SearchQueryOptions#isGeneratingFor(String)} is set to {@code true} for the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate Search Query for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingSearchQueryFor(String name) {
        return queryType.getSearchQuery().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Mutation Type.
     * @return {@code true} if the Generator should generate the Mutation Type, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingMutationType() {
        return isGeneratingCreateMutation() || isGeneratingUpdateMutation() || isGeneratingDeleteMutation();
    }

    /**
     * Determines if the Generator should generate the New mutations. Returns {@code true} if
     * {@link CreateMutationOptions#isGenerating()} is set to {@code true}
     * @return {@code true} if the Generator should generate New mutations, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingCreateMutation() {
        return mutationType.getCreateMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the New mutation for a specific Primary Model.
     * Returns {@code true} if {@link CreateMutationOptions#isGeneratingFor(String)} is set to {@code true} for the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate Create mutation for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingCreateMutationFor(String name) {
        return mutationType.getCreateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Update mutations. Returns {@code true} if
     * {@link UpdateMutationOptions#isGenerating()} is set to {@code true}
     * @return {@code true} if the Generator should generate Update mutations, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingUpdateMutation() {
        return mutationType.getUpdateMutation() != null && mutationType.getUpdateMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Update mutation for a specific Primary Model.
     * Returns {@code true} if {@link UpdateMutationOptions#isGeneratingFor(String)} is set to {@code true} or the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate Update mutation for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingUpdateMutationFor(String name) {
        return mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Delete mutations. Returns {@code true} if
     * {@link DeleteMutationOptions#isGenerating} is set to {@code true} or
     * @return {@code true} if the Generator should generate Delete mutations, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingDeleteMutation() {
        return mutationType.getDeleteMutation() != null && mutationType.getDeleteMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Delete mutation for a specific Primary Model.
     * Returns {@code true} if {@link DeleteMutationOptions#isGeneratingFor(String)} is set to {@code true} for the specified type
     * @param name the name of the Primary Model
     * @return {@code true} if the Generator should generate Delete mutation for a specific Primary Model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingDeleteMutationFor(String name) {
        return mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the built-in GraphQLID type should be used for IDs instead of GraphQLString
     * @return {@code true} if the built-in GraphQLID type should be used for IDs instead of GraphQLString, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isUsingIDType() {
        return properties.getIds().isUsingIDType();
    }

    /**
     * Gets the name of the List or Search Query input parameter for of specific primary model
     * @param name the name of the primary model
     * @return the name of the List or Search Query input parameter for of specific primary model
     */
    @JsonIgnore
    public final String getQueryInputParameterNameFor(@NonNull String name) {
        return input.getNameFor(getListQueryNameFor(name)) ;
    }

    /**
     * Gets the name of the List or Search Query input parameter for of specific primary model
     * @param type the primary model
     * @return the name of the List or Search Query input parameter for of specific primary model
     */
    @JsonIgnore
    public final String getQueryInputParameterNameFor(@NonNull BrAPIType type) {
        return getQueryInputParameterNameFor(type.getName()) ;
    }

    /**
     * Gets the name of the List or Search Query input type for of specific primary model
     * @param name the name of the primary model
     * @return the name of the List or Search input type for of specific primary model
     */
    @JsonIgnore
    public final String getQueryInputTypeNameFor(@NonNull String name) {
        return input.getTypeNameForQuery(getListQueryNameFor(name)) ;
    }

    /**
     * Gets the name of the List or Search Query input type for of specific primary model
     * @param type the primary model
     * @return the name of the List or Search input type for of specific primary model
     */
    @JsonIgnore
    public final String getQueryInputTypeNameFor(@NonNull BrAPIType type) {
        return getQueryInputTypeNameFor(type.getName()) ;
    }

    /**
     * Gets the name of the Single Query of specific primary model
     * @param name the name of the primary model
     * @return the name of the Single Query of specific primary model
     */
    public final String getSingleQueryNameFor(String name) {
        return getNameFor(this.queryType.getSingleQuery(), name) ;
    }

    /**
     * Gets the name of the List Query of specific primary model
     * @param name the name of the primary model
     * @return the name of the List Query of specific primary model
     */
    public final String getListQueryNameFor(String name) {
        return getNameFor(this.queryType.getListQuery(), name) ;
    }

    /**
     * Gets the name of the Search Query of specific primary model
     * @param name the name of the primary model
     * @return the name of the Search Query of specific primary model
     */
    public final String getSearchQueryNameFor(String name) {
        return getNameFor(this.queryType.getSearchQuery(), name) ;
    }

    /**
     * Gets the name of the Create Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Create Mutation of specific primary model
     */
    public final String getCreateMutationNameFor(String name) {
        return getNameFor(this.mutationType.getCreateMutation(), name) ;
    }

    /**
     * Gets the name of the Update Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Update Mutation of specific primary model
     */
    public final String getUpdateMutationNameFor(String name) {
        return getNameFor(this.mutationType.getUpdateMutation(), name) ;
    }

    /**
     * Gets the name of the Delete Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Delete Mutation of specific primary model
     */
    public final String getDeleteMutationNameFor(String name) {
        return getNameFor(this.mutationType.getDeleteMutation(), name) ;
    }

    private final String getNameFor(AbstractGraphQLOptions options, String name) {
        String newName = options.isPluralisingName() ? getPluralFor(name) : name;

        return options.getNameFor(newName) ;
    }

    /**
     * Gets if the possible types of a 'OneOf' type are merged into a single type.
     *
     * @param type the BrAPIClass
     * @return {@code true} if the possible types of a 'OneOf' type are merged into a single type.
     */
    public final boolean isMergingOneOfType(BrAPIClass type) {
        return mergingOneOfTypeFor.getOrDefault(type.getName(), mergeOneOfType) ;
    }

    /**
     * Sets if the possible types of a 'OneOf' type are merged into a single type.
     *
     * @param name the name of the type
     * @param isMergingOneOfType {@code true} if the possible types of a 'OneOf' type are merged into a single type,
     *                 {@code false} otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final GraphQLGeneratorOptions setMergeOneOfType(String name, boolean isMergingOneOfType) {
        mergingOneOfTypeFor.put(name, isMergingOneOfType) ;

        return this ;
    }
}