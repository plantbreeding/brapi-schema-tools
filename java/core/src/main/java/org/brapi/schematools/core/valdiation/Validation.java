package org.brapi.schematools.core.valdiation;

import lombok.Getter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.response.Response;

import java.util.ArrayList;
import java.util.List;

import static org.brapi.schematools.core.response.Response.empty;

/**
 * Provides the results of a validation
 */
@Getter
public class Validation {
    List<Response.Error> errors = new ArrayList<>();

    /**
     * Get a valid instant of the Validation
     * @return a valid instant of the Validation
     */
    public static Validation valid() {
        return new Validation();
    }

    /**
     * Checks if the value is non-null.
     * @param value the value to be tested
     * @param errorMessage the error message to be used if the value is null
     * @param args argument for te error message, See {@link String#format(String, Object...)} to see the options
     * @return The validation with the appended error if there was one generated.
     */
    public Validation assertNotNull(Object value, String errorMessage, Object... args) {
        if (value == null) {
            String message = args != null && args.length > 0 ? String.format(errorMessage, args) : errorMessage;
            errors.add(Response.Error.of("", message, Response.ErrorType.VALIDATION));
        }

        return this;
    }

    /**
     * Was the validation successful,
     * @return <code>true</code> if the validation was successful, <code>false</code> otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Merge the options to Validation, by calling the {@link Options#validate()} method
     * and adding any errors to this Validation
     * @param options the options to be validated
     * @return this Validation
     */
    public Validation merge(Options options) {
        errors.addAll(options.validate().getErrors()) ;

        return this ;
    }

    /**
     * Returns this Validation as a Response
     * @return this Validation as a Response
     * @param <U> The type of the result
     */
    public <U> Response<U> asResponse () {
        if (errors.isEmpty()) {
            return empty() ;
        } else {
            return empty().merge(this) ;
        }
    }
}
