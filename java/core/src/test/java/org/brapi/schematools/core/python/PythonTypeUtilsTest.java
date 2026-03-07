package org.brapi.schematools.core.python;

import org.brapi.schematools.core.model.BrAPIPrimitiveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PythonTypeUtilsTest {

    @Test
    void testFindPyType_String() {
        assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType()));
    }

    @Test
    void testFindPyType_StringWithDateTimeFormat() {
        assertEquals("datetime", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("date-time")));
    }

    @Test
    void testFindPyType_StringWithDateFormat() {
        assertEquals("date", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("date")));
    }

    @Test
    void testFindPyType_StringWithUnknownFormat() {
        assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("email")));
        assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("uri")));
        assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("uuid")));
    }

    @Test
    void testFindPyType_Integer() {
        assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType()));
    }

    @Test
    void testFindPyType_IntegerWithFormat() {
        assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType("int32")));
        assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType("int64")));
    }

    @Test
    void testFindPyType_Number() {
        assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType()));
    }

    @Test
    void testFindPyType_NumberWithFormat() {
        assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType("float")));
        assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType("double")));
    }

    @Test
    void testFindPyType_Boolean() {
        assertEquals("bool", PythonTypeUtils.findPyType(BrAPIPrimitiveType.booleanType()));
    }

    @Test
    void testFindPyType_BooleanWithFormat() {
        assertEquals("bool", PythonTypeUtils.findPyType(BrAPIPrimitiveType.booleanType("someFormat")));
    }
}

