package org.brapi.schematools.core.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class TestUtils {

    /**
     * Assert if two multiline strings are equal
     * @param expected the expected string
     * @param actual the actual string
     */
    public static void assertMultilineEquals(String expected, String actual) {
        BufferedReader expectedReader = new BufferedReader(new StringReader(expected));
        BufferedReader actualReader = new BufferedReader(new StringReader(actual));

        int line = 0 ;
        String expectedLine;
        String actualLine = null;

        try {
            while ((expectedLine = expectedReader.readLine()) != null && (actualLine = actualReader.readLine()) != null) {
                assertEquals(expectedLine, actualLine, String.format("Line %d:", line++));
            }

            assertNotNull(expectedLine, "No more expected lines");
            assertNotNull(actualLine, "More lines expected");
        }
        catch (Exception e) {
            fail(e.getMessage());
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
