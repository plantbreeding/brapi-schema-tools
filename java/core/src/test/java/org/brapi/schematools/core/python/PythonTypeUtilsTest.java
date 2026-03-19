package org.brapi.schematools.core.python;

import org.brapi.schematools.core.model.BrAPIPrimitiveType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class PythonTypeUtilsTest {

    @Test
    void testFindPyType_String() {
        try {
            assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType()).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_StringWithDateTimeFormat() {
        try {
            assertEquals("datetime", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("date-time")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_StringWithDateFormat() {
        try {
            assertEquals("date", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("date")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_StringWithUnknownFormat() {
        try {
            assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("email")).getResultOrThrow());
            assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("uri")).getResultOrThrow());
            assertEquals("str", PythonTypeUtils.findPyType(BrAPIPrimitiveType.stringType("uuid")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_Integer() {
        try {
            assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType()).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_IntegerWithFormat() {
        try {
            assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType("int32")).getResultOrThrow());
            assertEquals("int", PythonTypeUtils.findPyType(BrAPIPrimitiveType.integerType("int64")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_Number() {
        try {
            assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType()).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_NumberWithFormat() {
        try {
            assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType("float")).getResultOrThrow());
            assertEquals("float", PythonTypeUtils.findPyType(BrAPIPrimitiveType.numberType("double")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_Boolean() {
        try {
            assertEquals("bool", PythonTypeUtils.findPyType(BrAPIPrimitiveType.booleanType()).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    void testFindPyType_BooleanWithFormat() {
        try {
            assertEquals("bool", PythonTypeUtils.findPyType(BrAPIPrimitiveType.booleanType("someFormat")).getResultOrThrow());
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }
}

