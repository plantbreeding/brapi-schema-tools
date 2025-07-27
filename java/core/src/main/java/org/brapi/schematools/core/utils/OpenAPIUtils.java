package org.brapi.schematools.core.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.core.util.Json31;
import io.swagger.v3.oas.models.OpenAPI;

public class OpenAPIUtils {
    /**
     * Pretty print an OpenAPI object to a JSON String, with a default indentation of 4 spaces.
     * @param openAPI the object to be Pretty printed
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI) throws JsonProcessingException {
        return prettyPrint(openAPI, 4) ;
    }

    /**
     * Pretty print an OpenAPI object to a JSON String.
     * @param openAPI the object to be Pretty printed
     * @param indent the number of spaces to indent
     * @return Pretty print JSON String version of the object
     * @throws JsonProcessingException if the object cannot be converted to JSON.
     */
    public static String prettyPrint(OpenAPI openAPI, int indent) throws JsonProcessingException {
        ObjectMapper mapper = switch (openAPI.getSpecVersion()) {
            case V30 -> Json.mapper() ;
            case V31 -> Json31.mapper() ;
        } ;

        DefaultPrettyPrinter.Indenter indenter =
            new DefaultIndenter(" ".repeat(indent), DefaultIndenter.SYS_LF);
        DefaultPrettyPrinter printer = new DefaultPrettyPrinter();
        printer.indentObjectsWith(indenter);
        printer.indentArraysWith(indenter);

        return mapper.writer(printer).writeValueAsString(openAPI);
    }

}
