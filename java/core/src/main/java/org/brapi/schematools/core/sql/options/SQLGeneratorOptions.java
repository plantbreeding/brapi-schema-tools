package org.brapi.schematools.core.sql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractGeneratorSubOptions;
import org.brapi.schematools.core.options.PropertiesOptions;
import org.brapi.schematools.core.sql.SQLGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Options for the {@link SQLGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class SQLGeneratorOptions extends AbstractGeneratorSubOptions {

    private Boolean overwrite;
    private Boolean addDescriptionComments;
    private Boolean addGeneratorComments;
    private Boolean format;
    @Setter(AccessLevel.PRIVATE)
    private PropertiesOptions properties;

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static SQLGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("sql-options.yaml", SQLGeneratorOptions.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or JSON. 
     * The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static SQLGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, SQLGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or JSON.
     * The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static SQLGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, SQLGeneratorOptions.class));
    }

    public Validation validate() {
        return super.validate()
            .assertNotNull(overwrite, "'overwrite' option on %s is null", this.getClass().getSimpleName())
            .assertNotNull(properties, "Properties Options are null");
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public SQLGeneratorOptions override(SQLGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.addDescriptionComments != null) {
            addDescriptionComments = overrideOptions.addDescriptionComments;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
        }

        if (overrideOptions.format != null) {
            format = overrideOptions.format;
        }

        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.getProperties()) ;
        }

        return this;
    }

    /**
     * Determines if the Generator should Overwrite exiting files.
     *
     * @return {@code true} if the Generator should Overwrite exiting files, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isOverwritingExistingFiles() {
        return overwrite != null && overwrite;
    }

    /**
     * Determines if the Generator should create a description comment at the top of the SQL.
     *
     * @return {@code true} if the Generator should create a description comment at the top of the SQL,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingDescriptionComments() {
        return addDescriptionComments != null && addDescriptionComments;
    }

    /**
     * Determines if the Generator should create a comment at the bottom of the SQL.
     *
     * @return {@code true} if the Generator should create a comment at the bottom of the SQL,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingGeneratorComments() {
        return addGeneratorComments != null && addGeneratorComments;
    }

    /**
     * Determines if the Generator should format files.
     *
     * @return {@code true} if the Generator should format files, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isFormatingFiles() {
        return format != null && format;
    }

}