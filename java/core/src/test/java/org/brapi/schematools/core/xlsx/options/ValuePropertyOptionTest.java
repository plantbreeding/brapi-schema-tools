package org.brapi.schematools.core.xlsx.options;

import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.validiation.Validation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValuePropertyOptionTest {

    @Test
    void validate() {
        ValuePropertyOption option = new ValuePropertyOption() ;

        option.setName("test");

        ValuePropertyOption childProperty = new ValuePropertyOption() ;

        childProperty.setName("test");

        option.setChildProperty(childProperty);

        Validation validation = option.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;
    }

    @Test
    void noName() {
        ValuePropertyOption option = new ValuePropertyOption() ;

        Validation validation = option.validate();

        assertFalse(validation.isValid()) ;

        assertEquals(1, validation.getErrors().size());
    }

    @Test
    void indexAndKey() {
        ValuePropertyOption option = new ValuePropertyOption() ;

        option.setName("test");
        option.setKey("test");
        option.setIndex(0);

        Validation validation = option.validate();

        assertFalse(validation.isValid()) ;

        assertEquals(1, validation.getErrors().size());
    }

    @Test
    void invalidChildProperty() {
        ValuePropertyOption option = new ValuePropertyOption() ;

        option.setName("test");

        ValuePropertyOption childProperty = new ValuePropertyOption() ;

        option.setChildProperty(childProperty);

        Validation validation = option.validate();

        assertFalse(validation.isValid()) ;

        assertEquals(1, validation.getErrors().size());
    }

    @Test
    void invalidDefaultValue() {
        ValuePropertyOption option = new ValuePropertyOption() ;

        option.setName("test");

        option.setDefaultValue(1.2);

        Validation validation = option.validate();

        assertFalse(validation.isValid()) ;

        assertEquals(1, validation.getErrors().size());
    }
}