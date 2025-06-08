package org.brapi.schematools.core.openapi.generator.options;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteOptionsTest {

    DeleteOptions subject ;
    @BeforeEach
    void setUp() {
        subject = OpenAPIGeneratorOptions.load().getDelete() ;

        subject.setGenerate(true);
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
    void getSummaryFor() {
    }

    @Test
    void getDescriptionFor() {
    }
}