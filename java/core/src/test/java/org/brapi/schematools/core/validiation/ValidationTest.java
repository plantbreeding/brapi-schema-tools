package org.brapi.schematools.core.validiation;

import org.brapi.schematools.core.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class ValidationTest {

    private Validation subject;

    @Test
    void valid() {
        Assertions.assertNotNull(Validation.valid()) ;
        Assertions.assertTrue(Validation.valid().isValid()) ;
    }

    @Test
    void fail() {
        Assertions.assertNotNull(Validation.fail()) ;
        Assertions.assertFalse(Validation.fail().isValid()) ;
    }

    @Test
    void assertNotNull() {
        Assertions.assertTrue(Validation.valid().assertNotNull("Test Field", "Value must not be null").isValid());
        Assertions.assertTrue(Validation.valid().assertNotNull("Test Field", "Value must not be null").getErrors().isEmpty()) ;
        Assertions.assertFalse(Validation.valid().assertNotNull(null, "Value must not be null").isValid());
        Assertions.assertFalse(Validation.valid().assertNotNull(null, "Value must not be null").getErrors().isEmpty()) ;
        Assertions.assertEquals(Collections.singletonList("Value must not be null"), Validation.valid().assertNotNull(null, "Value must not be null").getErrors().stream().map(Response.Error::getMessage).toList());
    }

    @Test
    void assertMutuallyExclusive() {
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(null, "property1", "property2").isValid());

        MockObject myMock = new MockObject();
        myMock.setProperty1(null);
        myMock.setProperty2(null);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1(null);
        myMock.setProperty2("New Value2");
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1("New Value1");
        myMock.setProperty2(null);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1("New Value1");
        myMock.setProperty2("New Value2");
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(myMock, "property1", "property2").isValid());


        myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(false);
        // fails because false is non-null
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(false);
        // fails because false is non-null
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(true);
        // fails because false is non-null
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(true);
        // fails because false is non-null
        Assertions.assertFalse(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(true);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(false);
        Assertions.assertTrue(Validation.valid().assertMutuallyExclusive(myMock, "property3", "property4").isValid());
    }

    @Test
    void assertFlagsMutuallyExclusive() {
        MockObject myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(false);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(false);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(true);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(true);
        Assertions.assertFalse(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(true);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(false);
        myMock.setProperty4(null);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(true);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty3(null);
        myMock.setProperty4(false);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property3", "property4").isValid());

        myMock = new MockObject();
        myMock.setProperty1(null);
        myMock.setProperty2(null);
        Assertions.assertTrue(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1(null);
        myMock.setProperty2("New Value2");
        // fails because property2 is non boolean
        Assertions.assertFalse(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1("New Value1");
        myMock.setProperty2(null);
        // fails because property1 is non boolean
        Assertions.assertFalse(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property1", "property2").isValid());

        myMock = new MockObject();
        myMock.setProperty1("New Value1");
        myMock.setProperty2("New Value2");
        // fails because property 1 and property2 are non boolean
        Assertions.assertFalse(Validation.valid().assertFlagsMutuallyExclusive(myMock, "property1", "property2").isValid());

    }

    @Test
    void assertClass() {
    }

    @Test
    void isValid() {
        Assertions.assertTrue(Validation.valid().isValid());
        Assertions.assertFalse(Validation.fail().isValid());
    }

    @Test
    void merge() {
    }

    @Test
    void testMerge() {
    }

    @Test
    void testMerge1() {
    }

    @Test
    void mergeOnCondition() {
    }

    @Test
    void testMergeOnCondition() {
    }

    @Test
    void getErrorMessages() {
    }

    @Test
    void getAllErrorsMessage() {
    }

    @Test
    void testMerge2() {
    }

    @Test
    void asResponse() {
    }

    @Test
    void getErrors() {
    }


}