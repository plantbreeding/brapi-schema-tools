package org.brapi.schematools.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.SpecVersion;

/**
 * Provides utility methods for OpenAPI
 */
public class OpenAPIUtils {

    public final static String OUTPUT_FORMAT_YAML = "YAML";
    public final static String OUTPUT_FORMAT_JSON = "JSON";
    private static final int DEFAULT_INDENT = 4 ;

    /**
     * Pretty print an OpenAPI object to a JSON String, with a default indentation of 4 spaces.
     * @param openAPI the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI) throws JsonProcessingException {
        return prettyPrint(openAPI, DEFAULT_INDENT, OUTPUT_FORMAT_YAML) ;
    }
    /**
     * Pretty print an OpenAPI object to a JSON String, with a default indentation of 4 spaces.
     * @param openAPI the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI, String format) throws JsonProcessingException {
        return prettyPrint(openAPI, DEFAULT_INDENT, format) ;
    }

    /**
     * Pretty print an OpenAPI object to a JSON String.
     * @param openAPI the object to be Pretty printed
     * @param indent the number of spaces to indent
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI, int indent, String format) throws JsonProcessingException {
        return getPrettyObjectWriter(openAPI.getSpecVersion(), indent, format).writeValueAsString(openAPI);
    }

    /**
     * Gets the ObjectWriter for writing to YAML or JSON
     * @param specVersion the specification version
     * @param format the file format
     * @return the ObjectWriter for writing to YAML or JSON
     */
    public static ObjectWriter getPrettyObjectWriter(SpecVersion specVersion, String format) {
        return getPrettyObjectWriter(specVersion, DEFAULT_INDENT, format) ;
    }

    /**
     * Gets the ObjectWriter for writing to YAML or JSON
     * @param specVersion the specification version
     * @param indent the number of spaces to indent
     * @param format the file format
     * @return the ObjectWriter for writing to YAML or JSON
     */
    public static ObjectWriter getPrettyObjectWriter(SpecVersion specVersion, int indent, String format) {
        ObjectMapper mapper;

        if (OUTPUT_FORMAT_JSON.equals(format)){
            mapper = switch (specVersion) {
                case V30 -> Json.mapper();
                case V31 -> Json31.mapper();
            };
        } else {
            mapper = switch (specVersion) {
                case V30 -> Yaml.mapper();
                case V31 -> Yaml31.mapper();
            };
        }

        DefaultPrettyPrinter.Indenter indenter =
            new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return mapper.writer(printer) ;
    }
}
