package org.brapi.schematools.core.openapi.comparator.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.utils.ConfigurationUtils;
import org.brapi.schematools.core.validiation.Validation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

/**
 * Options for the {@link org.brapi.schematools.core.openapi.comparator.OpenAPIComparator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class OpenAPIComparatorOptions implements Options {
    private String tempFilePrefix ;
    @Setter(AccessLevel.PRIVATE)
    private AsciiDocOutputOptions asciiDoc;
    @Setter(AccessLevel.PRIVATE)
    private HTMLOutputOptions html;
    @Setter(AccessLevel.PRIVATE)
    private JSONOutputOptions json;
    @Setter(AccessLevel.PRIVATE)
    private MarkdownOutputOptions markdown;
    private List<String> ignoreMissingEndpoints ;
    private List<String> ignoreNewEndpoints ;
    @Getter(AccessLevel.NONE)
    private Boolean ignoreDepreciatedEndpoints ;
    private String comparisonAPI ;

    /**
     * Load the default options
     * @return The default options
     */
    public static OpenAPIComparatorOptions load() {
        try {
            return ConfigurationUtils.load("openapi-comparator-options.yaml", OpenAPIComparatorOptions.class) ;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the options from an option file in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or JSON.
     * @return The options loaded from the YAML or JSON file.
     * @throws IOException if the option file cannot be found or is incorrectly formatted.
     */
    public static OpenAPIComparatorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, OpenAPIComparatorOptions.class)) ;
    }

    /**
     * Load the options from an option input stream in YAML or JSON. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or JSON.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static OpenAPIComparatorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, OpenAPIComparatorOptions.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the Options which will be used to override this Options Object
     * @return this object for method chaining
     */
    public OpenAPIComparatorOptions override(OpenAPIComparatorOptions overrideOptions) {
        if (overrideOptions.tempFilePrefix != null) {
            this.tempFilePrefix = overrideOptions.tempFilePrefix ;
        }

        if (overrideOptions.asciiDoc != null) {
            asciiDoc.override(overrideOptions.asciiDoc);
        }

        if (overrideOptions.html != null) {
            html.override(overrideOptions.html);
        }

        if (overrideOptions.json != null) {
            json.override(overrideOptions.json);
        }

        if (overrideOptions.markdown != null) {
            markdown.override(overrideOptions.markdown);
        }

        if (overrideOptions.ignoreMissingEndpoints != null) {
            ignoreMissingEndpoints = overrideOptions.ignoreMissingEndpoints ;
        }

        if (overrideOptions.ignoreNewEndpoints != null) {
            ignoreNewEndpoints = overrideOptions.ignoreNewEndpoints ;
        }

        if (overrideOptions.ignoreDepreciatedEndpoints != null) {
            this.ignoreDepreciatedEndpoints = overrideOptions.ignoreDepreciatedEndpoints ;
        }

        if (overrideOptions.comparisonAPI != null) {
            this.comparisonAPI = overrideOptions.comparisonAPI ;
        }

        return this ;
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(asciiDoc, "Ascii Doc Render Options are null")
            .assertNotNull(html, "HTML Render Options are null")
            .assertNotNull(json, "JSON Render Options are null")
            .assertNotNull(markdown, "Markdown Render Options are null")
            .assertNotNull(comparisonAPI, "Comparison API is null") ;
    }

    @JsonIgnore
    public boolean isIgnoringDeprecatedEndpoints() {
        return ignoreDepreciatedEndpoints != null ? ignoreDepreciatedEndpoints : false ;
    }
}
