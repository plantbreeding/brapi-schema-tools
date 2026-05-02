package org.brapi.schematools.core.sql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.ControlledVocabularyOptions;
import org.brapi.schematools.core.options.AbstractMainGeneratorOptions;
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
public class SQLGeneratorOptions extends AbstractMainGeneratorOptions {

    private Boolean overwrite;
    private Boolean addTableHeaderComments;
    private Boolean addTableComments;
    private Boolean addTableColumnComments;
    private Boolean addGeneratorComments;
    private Boolean addPrimaryKeyConstraints;
    private Boolean addForeignKeyConstraints;
    private Boolean addNotNullConstraints;
    private Integer indent ;
    private String tableUsing;
    private Map<String, Object> tableProperties;
    private Boolean clustering;
    private Boolean ifNotExists;
    private Boolean addConstraintIfExists;
    private Boolean dropTable;
    @Setter(AccessLevel.PRIVATE)
    private PropertiesOptions properties;
    @Setter(AccessLevel.PRIVATE)
    private ControlledVocabularyOptions controlledVocabulary;
    private Boolean generateLinkTables;
    private Boolean snakeCaseTableNames;
    private Boolean pluralTableNames;
    private Boolean generateDropScript;
    private Boolean generateForeignKeyConstraintScript;
    private Boolean addConstraintsInArrayStructs;

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static SQLGeneratorOptions load() {
        try {
            SQLGeneratorOptions options = ConfigurationUtils.load("sql-options.yaml", SQLGeneratorOptions.class);

            loadBrAPISchemaReaderOptions(options) ;

            return options ;
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
            .assertEqualsOnCondition(isAddingForeignKeyConstraints(), Boolean.TRUE, isAddingPrimaryKeyConstraints(), "If addForeignKeyConstraints is true if addPrimaryKeyConstraints must also be true")
            .assertEqualsOnCondition(isGeneratingForeignKeyConstraintScript(), Boolean.TRUE, isAddingPrimaryKeyConstraints(), "If generateForeignKeyConstraintScript is true if addPrimaryKeyConstraints must also be true")
            .assertEqualsOnCondition(isAddingConstraintIfExists(), Boolean.TRUE, isGeneratingForeignKeyConstraintScript(), "If addConstraintIfExists is true if generateForeignKeyConstraintScript must also be true")
            .assertFlagsMutuallyExclusive(this, "addForeignKeyConstraints", "generateForeignKeyConstraintScript")
            .assertNotNull(properties, "Properties Options are null")
            .merge(properties)
            .assertNotNull(controlledVocabulary, "Controlled Vocabulary Options are null")
            .merge(controlledVocabulary)
            .assertFlagsMutuallyExclusive(this, "ifNotExists", "dropTable") ;
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

        if (overrideOptions.addNotNullConstraints != null) {
            addNotNullConstraints = overrideOptions.addNotNullConstraints;
        }

        if (overrideOptions.indent != null) {
            indent = overrideOptions.indent;
        }

        if (overrideOptions.tableUsing != null) {
            tableUsing = overrideOptions.tableUsing;
        }

        if (overrideOptions.tableProperties != null && !overrideOptions.tableProperties.isEmpty()) {
            if (tableProperties == null) {
                tableProperties = new HashMap<>();
            }
            overrideOptions.tableProperties.forEach((key, value) -> {
                if (value == null) tableProperties.remove(key);
                else tableProperties.put(key, value);
            });
        }

        if (overrideOptions.clustering != null) {
            clustering = overrideOptions.clustering;
        }

        if (overrideOptions.ifNotExists != null) {
            ifNotExists = overrideOptions.ifNotExists;
        }

        if (overrideOptions.addConstraintIfExists != null) {
            addConstraintIfExists = overrideOptions.addConstraintIfExists;
        }

        if (overrideOptions.dropTable != null) {
            dropTable = overrideOptions.dropTable;
        }

        if (overrideOptions.properties != null) {
            properties.override(overrideOptions.getProperties()) ;
        }

        if (overrideOptions.controlledVocabulary != null) {
            controlledVocabulary = overrideOptions.controlledVocabulary ;
        }

        if (overrideOptions.generateLinkTables != null) {
            generateLinkTables = overrideOptions.generateLinkTables;
        }

        if (overrideOptions.snakeCaseTableNames != null) {
            snakeCaseTableNames = overrideOptions.snakeCaseTableNames;
        }

        if (overrideOptions.pluralTableNames != null) {
            pluralTableNames = overrideOptions.pluralTableNames;
        }

        if (overrideOptions.generateDropScript != null) {
            generateDropScript = overrideOptions.generateDropScript;
        }

        if (overrideOptions.generateForeignKeyConstraintScript != null) {
            generateForeignKeyConstraintScript = overrideOptions.generateForeignKeyConstraintScript;
        }

        if (overrideOptions.addConstraintsInArrayStructs != null) {
            addConstraintsInArrayStructs = overrideOptions.addConstraintsInArrayStructs;
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
     * Determines if the Generator is adding primary key constraints to the columns in the table 'Create' statement for the primary key property
     *
     * @return {@code true} if the Generator add primary key constraints to the columns in the table 'Create' statement for the primary key property
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingPrimaryKeyConstraints() {
        return addPrimaryKeyConstraints != null && addPrimaryKeyConstraints;
    }

    /**
     * Determines if the Generator is adding foreign key constraints to the columns in the table 'Create' statement for the foreign key property
     *
     * @return {@code true} if the Generator add primary key constraints to the columns in the table 'Create' statement for the foreign key property
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingForeignKeyConstraints() {
        return addForeignKeyConstraints != null && addForeignKeyConstraints;
    }

    /**
     * Determines if the Generator is adding not null constraints to the columns in the table 'Create' statement for non-nullable properties
     *
     * @return {@code true} if the Generator add not null constraints to the columns in the table 'Create' statement for non-nullable properties
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingNotNullConstraints() {
        return addNotNullConstraints != null && addNotNullConstraints;
    }

    /**
     * Gets the indent size for formatting files.
     *
     * @return {@code true} if the Generator should format files, {@code false} otherwise
     */
    @JsonIgnore
    public int getIndentSize() {
        return indent != null ? indent : 0;
    }

    /**
     * Determines if the Generator should add a 'Clustering By' to the 'Create Table' for
     * each primary and foreign key.
     *
     * @return {@code true} if the Generator should add a 'Clustering By' to the 'Create Table', {@code false} otherwise
     */
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

    /**
     * Determines if the Generator create link tables for many-to-many relationships when the
     * link type is {@link org.brapi.schematools.core.options.LinkType#SUB_QUERY}
     *
     * @return {@code true} if the Generator create link tables for many-to-many relationships when the
     * link type is {@link org.brapi.schematools.core.options.LinkType#SUB_QUERY}, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isGeneratingLinkTables() {
        return generateLinkTables != null && generateLinkTables ;
    }

    /**
     * Determines if the Generator should use snake_case table names for all tables
     *
     * @return {@code true} if the Generator should use snake_case table names for all tables, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isUsingSnakeCaseTableNames() {
        return snakeCaseTableNames != null && snakeCaseTableNames ;
    }

    /**
     * Determines if the Generator should use plural table names for entity tables
     *
     * @return {@code true} if the Generator should use plural table names for tables, {@code false} otherwise
     */
    @JsonIgnore
    public boolean isUsingPluralTableNames() {
        return pluralTableNames != null && pluralTableNames ;
    }

    /**
     * Determines if the Generator should create a SQL file with Drop statements for each table.
     *
     * @return {@code true} if the Generator should create a SQL file with Drop statements for each table, {@code false} otherwise
     */
    public boolean isGeneratingDropScript() {
        return generateDropScript != null && generateDropScript ;
    }

    /**
     * Determines if the Generator should create a SQL file with Foreign constraint statements for each table.
     *
     * @return {@code true} if the Generator should create a SQL file with Drop statements for each table, {@code false} otherwise
     */
    public boolean isGeneratingForeignKeyConstraintScript() {
        return generateForeignKeyConstraintScript != null && generateForeignKeyConstraintScript ;
    }

    /**
     * Determines if the Generator should add NOT NULL and PRIMARY KEY constraints on fields nested
     * inside ARRAY&lt;STRUCT&lt;...&gt;&gt; types. Required for dialects such as Databricks Delta Lake that
     * do not support constraints on nested struct fields.
     *
     * @return {@code true} if the Generator should add constraints inside ARRAY&lt;STRUCT&lt;&gt;&gt;,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingConstraintsInArrayStructs() {
        return addConstraintsInArrayStructs != null && addConstraintsInArrayStructs;
    }

    /**
     * Determines if the Generator should add IF EXISTS when adding constraints using ALTER TABLE statements.
     * Required for dialects such as Databricks Delta Lake that do not support adding constraints if they already exist.
     *
     * @return {@code true} if the Generator should add IF EXISTS when adding constraints using ALTER TABLE statements
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingConstraintIfExists() {
        return addConstraintIfExists != null && addConstraintIfExists;
    }
}