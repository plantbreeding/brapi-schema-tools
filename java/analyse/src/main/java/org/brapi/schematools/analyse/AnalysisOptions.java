package org.brapi.schematools.analyse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;


/**
 * Options for the {@link OpenAPISpecificationAnalyser}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class AnalysisOptions implements Options {


    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseGetEntity;
    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseListEntity;
    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseCreateEntity;
    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseUpdateEntity;
    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseDeleteEntity;
    @Getter(AccessLevel.PRIVATE)
    private Boolean analyseSearchEntity;

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

        if (overrideOptions.analyseGetEntity != null) {
            analyseGetEntity = overrideOptions.analyseListEntity;
        }

        if (overrideOptions.analyseListEntity != null) {
            analyseListEntity = overrideOptions.analyseListEntity;
        }

        return this ;
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(analyseGetEntity, "'analyseGetEntity' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(analyseListEntity, "'analyseListEntity' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Determines if the Analyser should analyse the get API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingGetEntity(String entityName) {
        return analyseGetEntity ;
    }

    /**
     * Determines if the Analyser should analyse the list API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingListEntity(String entityName) {
        return analyseListEntity;
    }

    /**
     * Determines if the Analyser should analyse the get API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingCreateEntity(String entityName) {
        return analyseCreateEntity ;
    }

    /**
     * Determines if the Analyser should analyse the Update API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingUpdateEntity(String entityName) {
        return analyseUpdateEntity;
    }

    /**
     * Determines if the Analyser should analyse the Delete API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the get API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingDeleteEntity(String entityName) {
        return analyseDeleteEntity ;
    }

    /**
     * Determines if the Analyser should analyse the Search API for an Entity.
     * @param entityName the name of the entity
     * @return {@code true} if the Analyser should analyse the list API for an Entity, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAnalysingSearchEntity(String entityName) {
        return analyseSearchEntity;
    }
}
