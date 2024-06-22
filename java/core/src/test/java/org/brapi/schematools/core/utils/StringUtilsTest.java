package org.brapi.schematools.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StringUtilsTest {

    @Test
    void toSingular() {
        assertEquals("DataMatrix", StringUtils.toSingular("DataMatrices")) ;
    }

    @Test
    void toPlural() {
        assertEquals("DataMatrices", StringUtils.toPlural("DataMatrix")) ;
    }

    @Test
    void makeValidName() {
    }

    @Test
    void toSentenceCase() {
    }

    @Test
    void toParameterCase() {
    }

    @Test
    void startsWithLowerCase() {
    }

    @Test
    void startsWithUpperCase() {
    }

    @Test
    void replace() {
    }
}