package org.brapi.schematools.core.markdown.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.markdown.MarkdownGenerator;
import org.brapi.schematools.core.model.BrAPIClass;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.options.AbstractGeneratorSubOptions;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * Options for the {@link MarkdownGenerator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class MarkdownGeneratorOptions extends AbstractGeneratorSubOptions {

    private Boolean overwrite;
    private Boolean addGeneratorComments;
    private Boolean generateProperties;
    private Boolean generateDuplicateProperties;
    private Boolean generateParameterClasses;
    private Boolean generateRequestClasses;

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
     * Load the options from an options file in YAML or JSON. 
     * The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     *
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the options file cannot be found or is incorrectly formatted.
     */
    public static MarkdownGeneratorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, MarkdownGeneratorOptions.class));
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
    public static MarkdownGeneratorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, MarkdownGeneratorOptions.class));
    }

    public Validation validate() {
        return super.validate()
                .assertNotNull(overwrite, "'overwrite' option on %s is null", this.getClass().getSimpleName());
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     *
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this options for method chaining
     */
    public MarkdownGeneratorOptions override(MarkdownGeneratorOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.overwrite != null) {
            overwrite = overrideOptions.overwrite;
        }

        if (overrideOptions.addGeneratorComments != null) {
            addGeneratorComments = overrideOptions.addGeneratorComments;
        }

        if (overrideOptions.generateProperties != null) {
            generateProperties = overrideOptions.generateProperties;
        }

        if (overrideOptions.generateDuplicateProperties != null) {
            generateDuplicateProperties = overrideOptions.generateDuplicateProperties;
        }

        if (overrideOptions.generateParameterClasses != null) {
            generateParameterClasses = overrideOptions.generateParameterClasses;
        }

        if (overrideOptions.generateRequestClasses != null) {
            generateRequestClasses = overrideOptions.generateRequestClasses;
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
     * Determines if the Generator should create a hidden comment at the bottom of the Markdown.
     *
     * @return {@code true} if the Generator should create a hidden comment at the bottom of the Markdown,
     * {@code false} otherwise
     */
    @JsonIgnore
    public boolean isAddingGeneratorComments() {
        return addGeneratorComments != null && addGeneratorComments;
    }

    /**
     * Determines if the Generator Markdown for properties of an object class
     *
     * @return {@code true} if the Generator Markdown for properties of an object class,
     * {@code false} otherwise
     */
    public boolean isGeneratingForProperties() {
        return generateProperties != null && generateProperties;
    }

    /**
     * Determines if the Generator Markdown for parameter object classes
     *
     * @return {@code true} if the Generator Markdown for parameter object classes
     * {@code false} otherwise
     */
    public boolean isGeneratingForParameters() {
        return generateParameterClasses != null && generateParameterClasses;
    }

    /**
     * Determines if the Generator Markdown for request object classes
     *
     * @return {@code true} if the Generator Markdown for request object classes
     * {@code false} otherwise
     */
    public boolean isGeneratingForRequests() {
        return generateRequestClasses != null && generateRequestClasses;
    }

    /**
     * Determines if the Generator Markdown for properties of an object class if the
     * property is reused across classes or alternatively if there is a duplicate property,
     * then only one markdown file is created in the
     * root fields directory
     *
     * @return {@code true} if the Generator Markdown for properties of an object class if the
     * property is reused across classes.
     * {@code false} if there is a duplicate property then only one markdown file is created in the
     * root fields directory
     */
    public boolean isGeneratingForDuplicateProperties() {
        return generateDuplicateProperties != null && generateDuplicateProperties;
    }

    /**
     * Determines if the Markdown file is generated for a specific primary model
     * @param type the primary model
     * @return {@code true} if the Markdown file is generated for a specific primary model, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isGeneratingFor(@NonNull BrAPIType type) {
        if (type instanceof BrAPIClass brAPIClass) {
           return getGenerateFor().getOrDefault(brAPIClass.getName(), isGeneratingForClass(brAPIClass)) ;
        } else {
            return isGeneratingFor(type.getName());
        }
    }

    private boolean isGeneratingForClass(BrAPIClass brAPIClass) {
        if (brAPIClass.getMetadata() != null) {
            if (brAPIClass.getMetadata().isParameters()) {
                return isGeneratingForParameters() ;
            }

            if (brAPIClass.getMetadata().isRequest()) {
                return isGeneratingForRequests() ;
            }
        }

        return isGeneratingFor(brAPIClass.getName());
    }
}