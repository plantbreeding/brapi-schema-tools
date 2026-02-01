package org.brapi.schematools.core.options;

import org.brapi.schematools.core.response.Response;
import org.brapi.schematools.core.validiation.Validation;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class OptionsTestBase {
    protected void checkValidation(Options options) {
        Validation validation = options.validate();

        validation.getErrors().stream().map(Response.Error::getMessage).forEach(System.err::println);

        assertTrue(validation.isValid()) ;
    }
}
