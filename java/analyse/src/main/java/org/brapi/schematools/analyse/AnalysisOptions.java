package org.brapi.schematools.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.options.PropertiesOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


/**
 * Options for the {@link OpenAPISpecificationAnalyserFactory}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class AnalysisOptions implements Options {

    private Boolean analyseDepreciated;
    private APIRequestOptions getEntity;
    private APIRequestOptions listEntity;
    private APIRequestOptions createEntity;
    private APIRequestOptions updateEntity;
    private APIRequestOptions deleteEntity;
    private APIRequestOptions search;
    private APIRequestOptions searchResult;
    private APIRequestOptions table;
    private Boolean partitionedByCrop;
    private PropertiesOptions properties;

    /**
     * Load the default options
     * @return The default options
     */
    public static AnalysisOptions load() {
        try {
            return ConfigurationUtils.load("analyse-options.yaml", AnalysisOptions.class) ;
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
    public static AnalysisOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, AnalysisOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static AnalysisOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, AnalysisOptions.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this object for method chaining
     */
    public AnalysisOptions override(AnalysisOptions overrideOptions) {

        if (overrideOptions.analyseDepreciated != null) {
            analyseDepreciated = overrideOptions.analyseDepreciated;
        }

        if (overrideOptions.getEntity != null) {
            getEntity.override(overrideOptions.getEntity);
        }

        if (overrideOptions.listEntity != null) {
            listEntity.override(overrideOptions.listEntity);
        }

        if (overrideOptions.createEntity != null) {
            createEntity.override(overrideOptions.createEntity);
        }

        if (overrideOptions.updateEntity != null) {
            updateEntity.override(overrideOptions.updateEntity);
        }

        if (overrideOptions.deleteEntity != null) {
            deleteEntity.override(overrideOptions.deleteEntity);
        }

        if (overrideOptions.search != null) {
            search.override(overrideOptions.search);
        }

        if (overrideOptions.searchResult != null) {
            searchResult.override(overrideOptions.searchResult);
        }

        if (overrideOptions.partitionedByCrop != null) {
            partitionedByCrop = overrideOptions.partitionedByCrop;
        }

        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.getProperties()) ;
        }

        return this ;
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(analyseDepreciated, "'analyseDepreciated' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(getEntity, "'getEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(listEntity, "'listEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(createEntity, "'createEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(updateEntity, "'updateEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(deleteEntity, "'deleteEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(search, "'search' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(searchResult, "'searchResult' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(partitionedByCrop, "'partitionedByCrop' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(properties,  "Properties Options are null")
            .merge(properties) ;
    }

    /**
     * Determines if the Analyser should analyse depreciated endpoints
     * @return {@code true} if the Analyser should analyse depreciated endpoints, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingDepreciated() {
        return analyseDepreciated != null && analyseDepreciated;
    }

    /**
     * Determines if the Analyser should analyse the get API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingGetForEntity(String entityName) {
        return getEntity.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the list API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingListForEntity(String entityName) {
        return listEntity.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the get API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingCreateForEntity(String entityName) {
        return createEntity.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the Update API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingUpdateForEntity(String entityName) {
        return updateEntity.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the Delete API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingDeleteForEntity(String entityName) {
        return deleteEntity.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the Search API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingSearchForEntity(String entityName) {
        return search.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the result from Search API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingSearchResultForEntity(String entityName) {
        return searchResult.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the Analyser should analyse the get or post table API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get or post table API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingTableForEntity(String entityName) {
        return table.isAnalysingEntity(entityName);
    }

    /**
     * Determines if the request is partition by crop, so that queries are not across crops
     * @return {@code true} if the request is partition by crop, so that requests are not across crops, {@code false} otherwise
     */
    public boolean isPartitionedByCrop() {
        return partitionedByCrop != null && partitionedByCrop ;
    }
}
