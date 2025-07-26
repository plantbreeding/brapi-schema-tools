package org.brapi.schematools.core.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StringUtilsTest {

    @Test
    void toSingular() {
        assertEquals("DataMatrix", StringUtils.toSingular("DataMatrices")) ;
        assertEquals("Person", StringUtils.toSingular("People")) ;
        assertEquals("Germplasm", StringUtils.toSingular("Germplasm")) ;
        assertEquals("Study", StringUtils.toSingular("Studies")) ;
        assertEquals("dataMatrix", StringUtils.toSingular("dataMatrices")) ;
        assertEquals("person", StringUtils.toSingular("people")) ;
        assertEquals("germplasm", StringUtils.toSingular("germplasm")) ;
        assertEquals("study", StringUtils.toSingular("studies")) ;
        assertEquals("trial", StringUtils.toSingular("trials")) ;
    }

    @Test
    void toPlural() {
        assertEquals("DataMatrices", StringUtils.toPlural("DataMatrix")) ;
        assertEquals("People", StringUtils.toPlural("Person")) ;
        assertEquals("Germplasm", StringUtils.toPlural("Germplasm")) ;
        assertEquals("Studies", StringUtils.toPlural("Study")) ;
        assertEquals("dataMatrices", StringUtils.toPlural("dataMatrix")) ;
        assertEquals("people", StringUtils.toPlural("person")) ;
        assertEquals("germplasm", StringUtils.toPlural("germplasm")) ;
        assertEquals("studies", StringUtils.toPlural("study")) ;
        assertEquals("trials", StringUtils.toPlural("trial")) ;
    }

    @Test
    void makeValidName() {
        assertEquals("null", StringUtils.makeValidName(null)) ;
        assertEquals("blank", StringUtils.makeValidName("")) ;
        assertEquals("N1", StringUtils.makeValidName("1")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this/is/a/test")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this.is.a.test")) ;
        assertEquals("this_is_a_test", StringUtils.makeValidName("this-is-a-test")) ;
    }

    @Test
    void toSentenceCase() {
        assertEquals("SentenceCase", StringUtils.toSentenceCase("SentenceCase")) ;
        assertEquals("ParameterCase", StringUtils.toSentenceCase("parameterCase")) ;
    }

    @Test
    void toParameterCase() {
        assertEquals("sentenceCase", StringUtils.toParameterCase("SentenceCase")) ;
        assertEquals("parameterCase", StringUtils.toParameterCase("parameterCase")) ;
    }

    @Test
    void startsWithLowerCase() {
        assertTrue(StringUtils.startsWithLowerCase("lowerCase")) ;
        assertFalse(StringUtils.startsWithLowerCase("UpperCase")) ;
    }

    @Test
    void startsWithUpperCase() {
        assertFalse(StringUtils.startsWithUpperCase("lowerCase")) ;
        assertTrue(StringUtils.startsWithUpperCase("UpperCase")) ;
    }

    @Test
    void toLabel() {
        assertEquals("Sentence Case", StringUtils.toLabel("SentenceCase")) ;
        assertEquals("Parameter Case", StringUtils.toLabel("parameterCase")) ;
    }
}