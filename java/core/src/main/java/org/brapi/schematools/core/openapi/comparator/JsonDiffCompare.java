package org.brapi.schematools.core.openapi.comparator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.fge.jsonpatch.diff.JsonDiff;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Compares two files in JSON or YAML format
 */
public class JsonDiffCompare {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ObjectMapper objectMapperYAML = new ObjectMapper(new YAMLFactory());

    /**
     * Compare two JSON files
     * @param firstPath the first file to compared
     * @param secondPath the secondPath file to compared
     * @return A json node show the differences from the first file to the second
     * @throws IOException an exceptions reading the files to Json and making the comparison
     */
    public static JsonNode fromFilesJSON(Path firstPath, Path secondPath) throws IOException {
        JsonNode file1 = objectMapper.readTree(firstPath.toFile());
        JsonNode file2 = objectMapper.readTree(secondPath.toFile());
        return JsonDiff.asJson(file1, file2) ;
    }

    /**
     * Compare two YAML files
     * @param firstPath the first file to compared
     * @param secondPath the secondPath file to compared
     * @return A json node show the differences from the first file to the second
     * @throws IOException an exceptions reading the files to Json and making the comparison
     */
    public static JsonNode fromFilesYAML(Path firstPath, Path secondPath) throws IOException {
        JsonNode file1 = objectMapperYAML.readTree(firstPath.toFile());
        JsonNode file2 = objectMapperYAML.readTree(secondPath.toFile());
        return JsonDiff.asJson(file1, file2) ;
    }
}
