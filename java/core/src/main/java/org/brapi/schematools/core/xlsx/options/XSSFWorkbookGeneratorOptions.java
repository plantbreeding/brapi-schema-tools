package org.brapi.schematools.core.xlsx.options;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Options for the {@link GraphQLGenerator}.
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class XSSFWorkbookGeneratorOptions implements Options {

    List<ColumnOption> dataClassProperties ;
    String dataClassFieldHeader ;
    List<ColumnOption> dataClassFieldProperties ;

    /**
     * Load the default options
     * @return The default options
     */
    public static XSSFWorkbookGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("xlsx-options.yaml", XSSFWorkbookGeneratorOptions.class) ;
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
    public static XSSFWorkbookGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, XSSFWorkbookGeneratorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static XSSFWorkbookGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, XSSFWorkbookGeneratorOptions.class)) ;
    }

    public Validation validate() {

        return Validation.valid()
            .merge(dataClassProperties)
            .assertNotNull(dataClassFieldHeader, "'dataClassFieldHeader' option on %s is null", this.getClass().getSimpleName())
            .merge(dataClassFieldProperties) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public XSSFWorkbookGeneratorOptions override(XSSFWorkbookGeneratorOptions overrideOptions) {

        if (overrideOptions.dataClassProperties != null) {
            setDataClassProperties(overrideOptions.dataClassProperties) ;
        }

        if (overrideOptions.dataClassFieldHeader != null) {
            dataClassFieldHeader = overrideOptions.dataClassFieldHeader ;
        }

        if (overrideOptions.dataClassFieldProperties != null) {
           setDataClassFieldProperties(overrideOptions.getDataClassFieldProperties()) ;
        }

        return this ;
    }
}