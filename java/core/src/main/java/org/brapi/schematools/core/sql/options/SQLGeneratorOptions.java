package org.brapi.schematools.core.sql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.AbstractGeneratorSubOptions;
import org.brapi.schematools.core.options.PropertiesOptions;
import org.brapi.schematools.core.sql.SQLGenerator;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
    private Boolean addTableHeaderComments;
    private Boolean addTableComments;
    private Boolean addTableColumnComments;
    private Boolean addGeneratorComments;
    private Boolean format;
    private String tableUsing;
    private Map<String, Object> tableProperties;
    private Boolean clustering;
    private Boolean ifNotExists;
    private Boolean dropTable;
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

    @Override
    public Validation validate() {
        return super.validate()
            .assertNotNull(properties, "Properties Options are null")
            .assertFlagsMutuallyExclusive(this, "ifNotExists", "dropTable");
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

        if (overrideOptions.addTableHeaderComments != null) {
            addTableHeaderComments = overrideOptions.addTableHeaderComments;
        }

        if (overrideOptions.addTableComments != null) {
            addTableComments = overrideOptions.addTableComments;
        }

        if (overrideOptions.addTableColumnComments != null) {
            addTableColumnComments = overrideOptions.addTableColumnComments;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
        }

        if (overrideOptions.format != null) {
            format = overrideOptions.format;
        }

        if (overrideOptions.tableUsing != null) {
            tableUsing = overrideOptions.tableUsing;
        }

        if (overrideOptions.tableProperties != null && !overrideOptions.tableProperties.isEmpty()) {
            if (tableProperties == null) {
                tableProperties = new HashMap<>();
            }

            tableProperties.putAll(overrideOptions.tableProperties); ;
        }

        if (overrideOptions.clustering != null) {
            clustering = overrideOptions.clustering;
        }

        if (overrideOptions.ifNotExists != null) {
            ifNotExists = overrideOptions.ifNotExists;
        }

        if (overrideOptions.dropTable != null) {
            dropTable = overrideOptions.dropTable;
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
     * Determines if the Generator should create a description comment at the top of the SQL for each table.
     *
     * @return {@code true} if the Generator should create a description comment at the top of the SQL,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingTableHeaderComments() {
        return addTableHeaderComments != null && addTableHeaderComments;
    }

    /**
     * Determines if the Generator should create a comment in the table 'Create' statement
     *
     * @return {@code true} if the Generator should create a comment in the table 'Create' statement,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingTableComments() {
        return addTableComments != null && addTableComments;
    }

    /**
     * Determines if the Generator should create a comment for columns in the table 'Create' statement
     *
     * @return {@code true} if the Generator should create a comment for columns in the table 'Create' statement,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingTableColumnComments() {
        return addTableColumnComments != null && addTableColumnComments;
    }

    /**
     * Determines if the Generator should create a comment at the bottom of the SQL for each file
     *
     * @return {@code true} if the Generator should create a comment at the bottom of the SQL for each file,
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

    /**
     * Determines if the Generator should add a 'Clustering By' to the 'Create Table' for
     * each primary and foreign key.
     *
     * @return {@code true} if the Generator should add a 'Clustering By' to the 'Create Table', {@code false} otherwise
     */
    @JsonIgnore
    public boolean isClustering() {
        return clustering != null && clustering;
    }

    /**
     * Determines if the Generator should add an 'IF NOT EXISTS' to the 'Create Table'
     *
     * @return {@code true} if the Generator should add an 'IF NOT EXISTS' to the 'Create Table', {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingIfNotExists() {
        return ifNotExists != null && ifNotExists;
    }

    /**
     * Determines if the Generator should add a 'DROP TABLE; statement before each 'Create Table'
     *
     * @return {@code true} if the Generator should add a 'DROP TABLE;, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingDropTable() {
        return dropTable != null && dropTable ;
    }
}