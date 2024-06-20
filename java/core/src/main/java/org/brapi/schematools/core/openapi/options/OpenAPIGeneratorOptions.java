package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;
import org.brapi.schematools.core.openapi.OpenAPIGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;


/**
 * Options for the {@link OpenAPIGenerator}.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder(toBuilder = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OpenAPIGeneratorOptions {

    @JsonProperty("separateByModule")
    boolean separatingByModule;
    SingleGetOptions singleGet;
    ListGetOptions listGet;
    PostOptions post;
    PutOptions put;
    DeleteOptions delete;
    SearchOptions search;
    IdsOptions ids;
    @JsonProperty("createNewRequest")
    boolean creatingNewRequest;
    @JsonProperty("createNewRequestFor")
    @Builder.Default
    Map<String, Boolean> creatingNewRequestFor = new HashMap<>();
    String newRequestNameFormat;
    String singleResponseNameFormat;
    String listResponseNameFormat;
    String searchRequestNameFormat;

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static OpenAPIGeneratorOptions load(Path optionsFile) throws IOException {
        return load(Files.newInputStream(optionsFile));
    }

    /**
     * Load the default options
     * @return The default options
     */
    public static OpenAPIGeneratorOptions load() {

        try {
            InputStream inputStream = OpenAPIGeneratorOptions.class
                .getClassLoader()
                .getResourceAsStream("openapi-options.yaml");
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
    public static OpenAPIGeneratorOptions load(InputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        return mapper.readValue(inputStream, OpenAPIGeneratorOptions.class);
    }

    /**
     * Creates a build class with the default options already loaded. This also for
     * ease of overriding programmatically only a few options from their defaults.
     * @return a build class with the default options already loaded.
     */
    public static OpenAPIGeneratorOptions.OpenAPIGeneratorOptionsBuilder defaultBuilder() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            OpenAPIGeneratorOptions deepCopy = objectMapper
                .readValue(objectMapper.writeValueAsString(load()), OpenAPIGeneratorOptions.class);

            return deepCopy.toBuilder();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines if the Generator should generate any Single Get Endpoints. Returns <code>true</code> if
     * {@link SingleGetOptions#generating} is set to <code>true</code> or
     * {@link SingleGetOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Single Get Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSingleGet() {
        return singleGet != null && (singleGet.isGenerating() || singleGet.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any List Get Endpoints. Returns <code>true</code> if
     * {@link ListGetOptions#generating} is set to <code>true</code> or
     * {@link ListGetOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any List Get Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingListGet() {
        return listGet != null && (listGet.isGenerating() || listGet.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any Search Post or Get Endpoints. Returns <code>true</code> if
     * {@link SearchOptions#generating} is set to <code>true</code> or
     * {@link SearchOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Search Post or Get Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearch() {
        return search != null && (search.isGenerating() || search.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any Post Endpoints. Returns <code>true</code> if
     * {@link PostOptions#generating} is set to <code>true</code> or
     * {@link PostOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Post Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingPost() {
        return post != null && (post.isGenerating() || post.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any Put Endpoints. Returns <code>true</code> if
     * {@link PutOptions#generating} is set to <code>true</code> or
     * {@link PutOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Put Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingPut() {
        return put != null && (put.isGenerating() || put.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any Delete Endpoints. Returns <code>true</code> if
     * {@link DeleteOptions#generating} is set to <code>true</code> or
     * {@link DeleteOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Delete Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDelete() {
        return delete != null && (delete.isGenerating() || delete.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate any Endpoints without an ID parameter. Returns <code>true</code> if
     * {@link #isGeneratingListGet()} or {@link #isGeneratingPost()} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate any Endpoints without an ID parameter, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpoint() {
        return isGeneratingListGet() || isGeneratingPost();
    }

    /**
     * Determines if the Generator should generate any Endpoints with an ID parameter. Returns <code>true</code> if
     * {@link #isGeneratingSingleGet()} or {@link #isGeneratingPut()} or {@link #isGeneratingDelete()} is set to <code>true</code>
     * @return <code>true</code> if the Generator should generate any Endpoints with an ID parameter, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointWithId() {
        return isGeneratingSingleGet() || isGeneratingPut() || isGeneratingDelete();
    }

    /**
     * Determines if the Generator should generate any Search Post or Get Endpoints. Returns <code>true</code> if
     * {@link SearchOptions#generating} is set to <code>true</code> or
     * {@link SearchOptions#generatingFor} is set to <code>true</code> for any type
     * @return <code>true</code> if the Generator should generate any Search Post or Get Endpoints, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearchEndpoint() {
        return search != null && (search.isGenerating() || search.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    /**
     * Determines if the Generator should generate the Single Get Endpoint for a specific Primary Model. Returns <code>true</code> if
     * {@link SingleGetOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Single Get Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSingleGetEndpointFor(String name) {
        return singleGet != null && singleGet.generatingFor.getOrDefault(name, singleGet.isGenerating()) ;
    }

    /**
     * Determines if the Generator should generate the List Get Endpoint for a specific Primary Model. Returns <code>true</code> if
     * {@link ListGetOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the List Get Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingListGetEndpointFor(String name) {
        return listGet != null && listGet.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Post Endpoint for a specific Primary Model. Returns <code>true</code> if
     * {@link PostOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Post Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingPostEndpointFor(String name) {
        return post != null && post.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Put Endpoint for a specific Primary Model. Returns <code>true</code> if
     * {@link PutOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Put Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingPutEndpointFor(String name) {
        return put != null && put.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Delete Endpoint for a specific Primary Model. Returns <code>true</code> if
     * {@link PostOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Delete Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingDeleteEndpointFor(String name) {
        return delete != null && delete.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Search Post and Get Endpoint for a specific Primary Model.
     * Returns <code>true</code> if {@link #isGeneratingSearch()} is set to <code>true</code> or
     * {@link SearchOptions#generatingFor} is set to <code>true</code> for the specified type
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Search Post and Get  Endpoint for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingSearchEndpointFor(String name) {
        return search != null && search.isGeneratingFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link #isGeneratingListGetEndpointFor(String)} or {@link #isGeneratingPostEndpointFor(String)} is set to <code>true</code>
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints without an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointFor(String name) {
        return isGeneratingListGetEndpointFor(name ) || isGeneratingPostEndpointFor(name) ;
    }

    /**
     * Determines if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model. Returns <code>true</code> if
     * {@link #isGeneratingSingleGetEndpointFor(String)} or {@link #isGeneratingPutEndpointFor(String)} or
     * {@link #isGeneratingDeleteEndpointFor(String)}is set to <code>true</code>
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate the Endpoints with an ID parameter for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingEndpointNameWithIdFor(String name) {
        return isGeneratingSingleGetEndpointFor(name ) || isGeneratingPutEndpointFor(name) || isGeneratingDeleteEndpointFor(name) ;
    }

    /**
     * Determines if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model.
     * For example if set to <code>true</code>  for the model 'Study' the generator will create the NewStudyRequest schema and the 'Study' schema,
     * whereas if set <code>false</code> generator will create only create the 'Study' schema
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator should generate a NewRequest schema, separate from the standard schema for a specific Primary Model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingNewRequestFor(String name) {
        return creatingNewRequestFor.getOrDefault(name, creatingNewRequest) ;
    }

    /**
     * Gets the name for the NewRequest schema for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the NewRequest schema name for a specific Primary Model
     */
    @JsonIgnore
    public String getNewRequestNameFor(String name) {
        return String.format(newRequestNameFormat, name) ;
    }

    /**
     * Determines if the Generator should generate the List Response for a specific Primary Model.
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator generate the List Response for a specific Primary Model, <code>false</code> otherwise
     */
    public boolean isGeneratingListResponseFor(String name) {
        return listGet != null && listGet.isGeneratingFor(name) ;
    }

    /**
     * Gets the name for the Single Response for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Single Response name for a specific Primary Model
     */
    @JsonIgnore
    public String getSingleResponseNameFor(String name) {
        return String.format(singleResponseNameFormat, name) ;
    }

    /**
     * Gets the name for the List Response for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the List Response name for a specific Primary Model
     */
    @JsonIgnore
    public String getListResponseNameFor(String name) {
        return String.format(listResponseNameFormat, name) ;
    }

    /**
     * Determines if the Generator should generate the Search Request schema for a specific Primary Model.
     * @param name the name of the Primary Model
     * @return <code>true</code> if the Generator generate the Search Request schema for a specific Primary Model, <code>false</code> otherwise
     */
    public boolean isGeneratingSearchRequestFor(String name) {
        return search != null && search.isGeneratingFor(name) ;
    }

    /**
     * Gets the name for the Search Request schema for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Search Request schema name for a specific Primary Model
     */
    @JsonIgnore
    public String getSearchRequestNameFor(String name) {
        return String.format(searchRequestNameFormat, name) ;
    }

    /**
     * Gets the Pluralise name for a specific Primary Model
     * @param name the name of the Primary Model
     * @return the Pluralise name for a specific Primary Model
     */
    @JsonIgnore
    public String getPluralFor(String name) {
        return toPlural(name) ;
    }

    /**
     * Gets the singular name for pluralised property name
     * @param propertyName the pluralised property name
     * @return the Pluralise name for a specific Primary Model
     */
    @JsonIgnore
    public String getSingularForProperty(String propertyName) {
        return toSingular(propertyName) ;
    }

    /**
     *
     */
    public static class OpenAPIGeneratorOptionsBuilder {}
}
