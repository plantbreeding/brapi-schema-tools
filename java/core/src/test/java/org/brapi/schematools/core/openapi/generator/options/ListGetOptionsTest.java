package org.brapi.schematools.core.openapi.generator.options;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ListGetOptionsTest {

    ListGetOptions subject ;
    @BeforeEach
    void setUp() {
        subject = OpenAPIGeneratorOptions.load().getListGet() ;
    }

    @Test
    void isGenerating() {
        assertTrue(subject.isGenerating());
    }

    @Test
    void isGeneratingFor() {
        assertTrue(subject.isGeneratingFor("Trial"));
        assertTrue(subject.isGeneratingFor("BreedingMethod"));
        assertFalse(subject.isGeneratingFor("AlleleMatrix"));
    }

    @Test
    void getSummaryFormat() {
    }

    @Test
    void getDescriptionFormat() {
    }

    @Test
    void isPagedDefault() {
    }

    @Test
    void getPaged() {
    }

    @Test
    void builder() {
    }
}