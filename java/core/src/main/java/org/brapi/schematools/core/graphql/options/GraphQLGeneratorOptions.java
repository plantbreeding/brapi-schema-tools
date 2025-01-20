package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.openapi.options.OpenAPIGeneratorOptions;
import org.brapi.schematools.core.options.AbstractGeneratorOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.valdiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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
public class GraphQLGeneratorOptions extends AbstractGeneratorOptions {

    private InputOptions input ;
    private QueryTypeOptions queryType;
    private MutationTypeOptions mutationType;
    private IdsOptions ids;
    @JsonProperty("mergeOneOfType")
    private Boolean mergingOneOfType ;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> mergingOneOfTypeFor = new HashMap<>();

    /**
     * Load the default options
     * @return The default options
     */
    public static GraphQLGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("graphql-options.yaml", GraphQLGeneratorOptions.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static GraphQLGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, GraphQLGeneratorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
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
            .assertNotNull(ids,  "Id Options are null")
            .merge(input)
            .merge(queryType)
            .merge(mutationType)
            .merge(ids) ;
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

        if (overrideOptions.ids != null) {
            ids.override(overrideOptions.ids) ;
        }

        if (mergingOneOfType != null) {
            setMergingOneOfType(overrideOptions.mergingOneOfType) ;
        }

        mergingOneOfTypeFor.putAll(overrideOptions.mergingOneOfTypeFor);

        return this ;
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
     * {@link SingleQueryOptions#isGenerating} is set to <code>true</code> or
     * @return <code>true</code> if the Generator should generate any single query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSingleQueries() {
        return queryType.getSingleQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the single query for a specific Primary Model.
     * Returns <code>true</code> if {@link SingleQueryOptions#isGeneratingFor(String)} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate single query for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSingleQueryFor(String name) {
        return queryType.getSingleQuery().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate any List Query. Returns <code>true</code> if
     * {@link ListQueryOptions#isGenerating} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any List Query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingListQueries() {
        return queryType.getListQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the List Query for a specific Primary Model.
     * Returns <code>true</code> if {@link ListQueryOptions#isGeneratingFor(String)} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate List Query for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingListQueryFor(String name) {
        return queryType.getListQuery().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate any Search Query. Returns <code>true</code> if
     * {@link SearchQueryOptions#isGenerating} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate any Search Query, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearchQueries() {
        return queryType.getSearchQuery().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Search Query for a specific Primary Model.
     * Returns <code>true</code> if {@link SearchQueryOptions#isGeneratingFor(String)} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Search Query for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearchQueryFor(String name) {
        return queryType.getSearchQuery().isGeneratingFor(name) ;
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
     * Determines if the Generator should generate the New mutations. Returns <code>true</code> if
     * {@link CreateMutationOptions#isGenerating()} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate New mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingCreateMutation() {
        return mutationType.getCreateMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the New mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link CreateMutationOptions#isGeneratingFor(String)} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Create mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingCreateMutationFor(String name) {
        return mutationType.getCreateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Update mutations. Returns <code>true</code> if
     * {@link UpdateMutationOptions#isGenerating()} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate Update mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingUpdateMutation() {
        return mutationType.getUpdateMutation() != null && mutationType.getUpdateMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Update mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link UpdateMutationOptions#isGeneratingFor(String)} is set to <code>true</code> or the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Update mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingUpdateMutationFor(String name) {
        return mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Delete mutations. Returns <code>true</code> if
     * {@link DeleteMutationOptions#isGenerating} is set to <code>true</code> or
     * @return <code>true</code> if the Generator should generate Delete mutations, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDeleteMutation() {
        return mutationType.getDeleteMutation() != null && mutationType.getDeleteMutation().isGenerating() ;
    }

    /**
     * Determines if the Generator should generate the Delete mutation for a specific Primary Model.
     * Returns <code>true</code> if {@link DeleteMutationOptions#isGeneratingFor(String)} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate Delete mutation for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDeleteMutationFor(String name) {
        return mutationType.getUpdateMutation().isGeneratingFor(name) ;
    }

    /**
     * Determines if the built-in GraphQLID type should be used for IDs instead of GraphQLString
     * @return <code>true</code> if the built-in GraphQLID type should be used for IDs instead of GraphQLString, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isUsingIDType() {
        return ids.isUsingIDType();
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
    public String getSingleQueryNameFor(String name) {
        return getNameFor(this.queryType.getSingleQuery(), name) ;
    }

    /**
     * Gets the name of the List Query of specific primary model
     * @param name the name of the primary model
     * @return the name of the List Query of specific primary model
     */
    public String getListQueryNameFor(String name) {
        return getNameFor(this.queryType.getListQuery(), name) ;
    }

    /**
     * Gets the name of the Search Query of specific primary model
     * @param name the name of the primary model
     * @return the name of the Search Query of specific primary model
     */
    public String getSearchQueryNameFor(String name) {
        return getNameFor(this.queryType.getSearchQuery(), name) ;
    }

    /**
     * Gets the name of the Create Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Create Mutation of specific primary model
     */
    public String getCreateMutationNameFor(String name) {
        return getNameFor(this.mutationType.getCreateMutation(), name) ;
    }

    /**
     * Gets the name of the Update Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Update Mutation of specific primary model
     */
    public String getUpdateMutationNameFor(String name) {
        return getNameFor(this.mutationType.getUpdateMutation(), name) ;
    }

    /**
     * Gets the name of the Delete Mutation of specific primary model
     * @param name the name of the primary model
     * @return the name of the Delete Mutation of specific primary model
     */
    public String getDeleteMutationNameFor(String name) {
        return getNameFor(this.mutationType.getDeleteMutation(), name) ;
    }

    private String getNameFor(AbstractGraphQLOptions options, String name) {
        String newName = options.isPluralisingName() ? getPluralFor(name) : name;

        return options.getNameFor(newName) ;
    }

    /**
     * Gets if the possible types of a 'OneOf' type are merged into a single type.
     *
     * @param type the BrAPIClass
     * @return <code>true</code> if the possible types of a 'OneOf' type are merged into a single type.
     */
    public boolean isMergingOneOfType(BrAPIClass type) {
        return mergingOneOfTypeFor.getOrDefault(type.getName(), mergingOneOfType) ;
    }

    /**
     * Sets if the possible types of a 'OneOf' type are merged into a single type.
     *
     * @param name the name of the type
     * @param isMergingOneOfType <code>true</code> if the possible types of a 'OneOf' type are merged into a single type,
     *                 <code>false</code> otherwise
     * @return the options for chaining
     */
    @JsonIgnore
    public final GraphQLGeneratorOptions setMergingOneOfType(String name, boolean isMergingOneOfType) {
        mergingOneOfTypeFor.put(name, isMergingOneOfType) ;

        return this ;
    }
}