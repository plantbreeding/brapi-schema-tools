package org.brapi.schematools.core.openapi.comparator.options;

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
 * Options for the {@link OpenAPIComparator}.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Accessors(chain = true)
public class OpenAPIComparatorOptions implements Options {
    @Setter(AccessLevel.PRIVATE)
    private AsciiDocRenderOptions asciiDoc;
    @Setter(AccessLevel.PRIVATE)
    private HTMLRenderOptions html;
    @Setter(AccessLevel.PRIVATE)
    private JSONRenderOptions json;
    @Setter(AccessLevel.PRIVATE)
    private MarkdownRenderOptions markdown;

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
     * Load the options from an options file in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param optionsFile The path to the options file in YAML or Json.
     * @return The options loaded from the YAML or Json file.
     * @throws IOException if the options file can not be found or is incorrectly formatted.
     */
    public static OpenAPIComparatorOptions load(Path optionsFile) throws IOException {
        return load().override(ConfigurationUtils.load(optionsFile, OpenAPIComparatorOptions.class)) ;
    }

    /**
     * Load the options from an options input stream in YAML or Json. The options file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json.
     * @return The options loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static OpenAPIComparatorOptions load(InputStream inputStream) throws IOException {
        return load().override(ConfigurationUtils.load(inputStream, OpenAPIComparatorOptions.class)) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     * @return this object for method chaining
     */
    public OpenAPIComparatorOptions override(OpenAPIComparatorOptions overrideOptions) {
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

        return this ;
    }

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(asciiDoc, "Ascii Doc Render Options are null")
            .assertNotNull(html, "HTML Render Options are null")
            .assertNotNull(json, "JSON Render Options are null")
            .assertNotNull(markdown, "Markdown Render Options are null") ;
    }
}
