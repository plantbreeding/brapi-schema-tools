package org.brapi.schematools.core.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigurationUtils {

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    
    /**
     * Load the configurations from a file in YAML or Json. The file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param configurationsFile The path to the configuration file in YAML or Json.
     * @param configurationClass The configuration class.
     * @return The configurations loaded from the YAML or Json file.
     * @throws IOException if the file can not be found or is incorrectly formatted.
     */
    public static <T> T load(Path configurationsFile, Class<T> configurationClass) throws IOException {
        return load(Files.newInputStream(configurationsFile), configurationClass);
    }

    /**
     * Load the default configurations from the classpath
     * @param configurationClass The configuration class.
     * @return The default configurations
     */
    public static <T> T load(String classPath, Class<T> configurationClass) {

        try {
            InputStream inputStream = configurationClass
                .getClassLoader()
                .getResourceAsStream(classPath);
            return load(inputStream, configurationClass);
        } catch (Exception e) { // The default configurations should be present on the classpath
            throw new RuntimeException(e);
        }
    }

    /**
     * Load the configurations from an input stream in YAML or Json. The configuration file may have missing
     * (defined) values, in these cases the default values are loaded. See {@link #load()}
     * @param inputStream The input stream in YAML or Json
     * @param configurationClass The configuration class.
     * @return The configurations loaded from input stream.
     * @throws IOException if the input stream is not valid or the content is incorrectly formatted.
     */
    public static <T> T load(InputStream inputStream, Class<T> configurationClass) throws IOException {
        return mapper.readValue(inputStream, configurationClass);
    }
}
