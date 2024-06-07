package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toPlural;
import static org.brapi.schematools.core.utils.StringUtils.toSingular;


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

    public static OpenAPIGeneratorOptions load(Path optionsFile) throws IOException {
        return load(Files.newInputStream(optionsFile));
    }

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

    private static OpenAPIGeneratorOptions load(InputStream inputStream) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        return mapper.readValue(inputStream, OpenAPIGeneratorOptions.class);
    }

    @JsonIgnore
    public boolean isGeneratingSingleGet() {
        return singleGet != null && (singleGet.isGenerating() || singleGet.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingListGet() {
        return listGet != null && (listGet.isGenerating() || listGet.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingSearch() {
        return search != null && (search.isGenerating() || search.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingPost() {
        return post != null && (post.isGenerating() || post.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingPut() {
        return put != null && (put.isGenerating() || put.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingDelete() {
        return delete != null && (delete.isGenerating() || delete.getGeneratingFor().values().stream().anyMatch(value -> value));
    }

    @JsonIgnore
    public boolean isGeneratingEndpoint() {
        return isGeneratingListGet() || isGeneratingPost();
    }

    @JsonIgnore
    public boolean isGeneratingEndpointWithId() {
        return isGeneratingSingleGet() || isGeneratingPut() || isGeneratingDelete();
    }

    @JsonIgnore
    public boolean isGeneratingSearchEndpoint() {
        return isGeneratingSingleGet() || isGeneratingPut() || isGeneratingDelete();
    }

    @JsonIgnore
    public boolean isGeneratingSingleGetEndpointFor(String name) {
        return singleGet != null && singleGet.generatingFor.getOrDefault(name, singleGet.isGenerating()) ;
    }

    @JsonIgnore
    public boolean isGeneratingListGetEndpointFor(String name) {
        return listGet != null && listGet.generatingFor.getOrDefault(name, listGet.isGenerating()) ;
    }

    @JsonIgnore
    public boolean isGeneratingPostEndpointFor(String name) {
        return post != null && post.generatingFor.getOrDefault(name, post.isGenerating()) ;
    }

    @JsonIgnore
    public boolean isGeneratingPutEndpointFor(String name) {
        return put != null && put.generatingFor.getOrDefault(name, put.isGenerating()) ;
    }

    @JsonIgnore
    public boolean isGeneratingDeleteEndpointFor(String name) {
        return delete != null && delete.generatingFor.getOrDefault(name, delete.isGenerating()) ;
    }

    @JsonIgnore
    public boolean isGeneratingSearchEndpointFor(String name) {
        return search != null && search.getGeneratingFor().getOrDefault(name, isGeneratingSearch()) ;
    }

    @JsonIgnore
    public boolean isGeneratingEndpointFor(String name) {
        return isGeneratingListGetEndpointFor(name ) || isGeneratingPostEndpointFor(name) ;
    }

    @JsonIgnore
    public boolean isGeneratingEndpointNameWithIdFor(String name) {
        return isGeneratingSingleGetEndpointFor(name ) || isGeneratingPutEndpointFor(name) || isGeneratingDeleteEndpointFor(name) ;
    }

    @JsonIgnore
    public boolean isGeneratingNewRequestFor(String name) {
        return creatingNewRequestFor.getOrDefault(name, creatingNewRequest) ;
    }

    @JsonIgnore
    public String getNewRequestNameFor(String name) {
        return String.format(newRequestNameFormat, name) ;
    }

    public boolean isGeneratingListResponseFor(String name) {
        return listGet != null && listGet.isGeneratingFor(name) ;
    }

    @JsonIgnore
    public String getSingleResponseNameFor(String name) {
        return String.format(singleResponseNameFormat, name) ;
    }

    @JsonIgnore
    public String getListResponseNameFor(String name) {
        return String.format(listResponseNameFormat, name) ;
    }

    public boolean isGeneratingSearchRequestFor(String name) {
        return search != null && search.isGeneratingFor(name) ;
    }

    @JsonIgnore
    public String getSearchRequestNameFor(String name) {
        return String.format(searchRequestNameFormat, name) ;
    }

    @JsonIgnore
    public String getPluralFor(String name) {
        return toPlural(name) ;
    }

    @JsonIgnore
    public String getSingularForProperty(String name) {
        return toSingular(name) ;
    }

    public static class OpenAPIGeneratorOptionsBuilder {}
}
