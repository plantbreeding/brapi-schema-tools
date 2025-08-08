package org.brapi.schematools.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.core.util.Yaml31;
import io.swagger.v3.oas.models.OpenAPI;

import java.util.Objects;

public class OpenAPIUtils {

    public final static String OUTPUT_FORMAT_YAML = "YAML";
    public final static String OUTPUT_FORMAT_JSON = "JSON";
    /**
     * Pretty print an OpenAPI object to a JSON String, with a default indentation of 4 spaces.
     * @param openAPI the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI) throws JsonProcessingException {
        return prettyPrint(openAPI, 4, OUTPUT_FORMAT_YAML) ;
    }
    /**
     * Pretty print an OpenAPI object to a JSON String, with a default indentation of 4 spaces.
     * @param openAPI the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI, String format) throws JsonProcessingException {
        return prettyPrint(openAPI, 4, format) ;
    }

    /**
     * Pretty print an OpenAPI object to a JSON String.
     * @param openAPI the object to be Pretty printed
     * @param indent the number of spaces to indent
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI, int indent, String format) throws JsonProcessingException {
        ObjectMapper mapper;

        if (OUTPUT_FORMAT_JSON.equals(format)){
            mapper = switch (openAPI.getSpecVersion()) {
                case V30 -> Json.mapper();
                case V31 -> Json31.mapper();
            };
        } else {
            mapper = switch (openAPI.getSpecVersion()) {
                case V30 -> Yaml.mapper();
                case V31 -> Yaml31.mapper();
            };
        }

        DefaultPrettyPrinter.Indenter indenter =
            new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return mapper.writer(printer).writeValueAsString(openAPI);
    }

}
