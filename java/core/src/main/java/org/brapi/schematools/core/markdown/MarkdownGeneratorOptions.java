package org.brapi.schematools.core.markdown;

import com.fasterxml.jackson.annotation.JsonIgnore;
import graphql.schema.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.graphql.GraphQLGenerator;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;

import static org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator.LIST_RESPONSE_PATTERN;
import static org.brapi.schematools.core.markdown.GraphQLMarkdownGenerator.SEARCH_RESPONSE_PATTERN;

/**
 * Options for the {@link MarkdownGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class MarkdownGeneratorOptions implements Options {

    private Boolean overwrite;
    private Boolean addGeneratorComments;

    /**
     * Load the default options
     *
     * @return The default options
     */
    public static MarkdownGeneratorOptions load() {
        try {
            return ConfigurationUtils.load("markdown-options.yaml", MarkdownGeneratorOptions.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static MarkdownGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, MarkdownGeneratorOptions.class));
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static MarkdownGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, MarkdownGeneratorOptions.class));
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(overwrite, "'overwrite' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public MarkdownGeneratorOptions override(MarkdownGeneratorOptions overrideOptions) {
        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
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
        return overwrite;
    }

    /**
     * Determines if the Generator should create a hidden comment at the bottom of the Markdown.
     *
     * @return {@code true} if the Generator should create a hidden comment at the bottom of the Markdown.,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingGeneratorComments() {
        return addGeneratorComments != null && addGeneratorComments;
    }
}