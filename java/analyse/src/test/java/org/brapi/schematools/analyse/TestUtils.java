package org.brapi.schematools.analyse;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test Utils
 */
public class TestUtils {

    /**
     * Loads a string from a file
     * @param pathString the path of the file
     * @return the string
     * @throws IOException if there is a read issue
     */
    public static String getResourceAsString(String pathString) throws IOException, URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(TestUtils.class.getClassLoader()
            .getResource(pathString)).toURI());

        Stream<String> lines = Files.lines(path);
        String data = lines.collect(Collectors.joining(System.lineSeparator()));
        lines.close();

        return data.trim() ;
    }
}