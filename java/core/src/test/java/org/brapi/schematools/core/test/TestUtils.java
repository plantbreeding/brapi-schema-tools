package org.brapi.schematools.core.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Slf4j
public class TestUtils {

    /**
     * Assert if two multiline strings are equal
     * @param expected the expected string
     * @param actual the actual string
     */
    public static void assertMultilineEqual(String expected, String actual) {
        BufferedReader expectedReader = new BufferedReader(new StringReader(expected));
        BufferedReader actualReader = new BufferedReader(new StringReader(actual));

        boolean equals = true;

        try {
            String expectedLine = expectedReader.readLine() ;
            String actualLine = actualReader.readLine() ;

            int line = 1 ;

            while (equals && expectedLine != null && actualLine != null) {
                equals = expectedLine.equals(actualLine);
                assertEquals(expectedLine, actualLine, String.format("Expected '%s' but got '%s' at line %d", expectedLine, actualLine, line));

                expectedLine = expectedReader.readLine() ;
                actualLine = actualReader.readLine() ;

                ++line;
            }

            assertNull(expectedLine, String.format("Expected no more line in expected string at line %d", line));
            assertNull(actualLine, String.format("Expected no more line in expected string at line %d", line));
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            Assertions.fail(e) ;
        }
    }

    /**
     * Assert if two Objects when converted to JSON objects are equal
     * @param expected the expected object
     * @param actual the actual object
     */
    public static void assertJSONEquals(String expected, String actual) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();

        assertEquals(mapper.readTree(expected), mapper.readTree(actual));
    }
}
