package org.brapi.schematools.core.validiation;

import lombok.Getter;
import org.apache.commons.beanutils.PropertyUtils;
import org.brapi.schematools.core.response.Response;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
            addError(errorMessage, args) ;
        }

        return this;
    }

    /**
     * Validate that only one of the provided properties is non-null
     * @param value the value to be tested
     * @param properties the array of properties to be checked
     * @return this Validation
     */
    public Validation assertMutuallyExclusive(Object value, String... properties) {
        if (value == null) {
            addError("Value is null!") ;
        } else {
            int count = 0;

            for (String property : properties) {
                try {
                    if (PropertyUtils.getProperty(value, property) != null) {
                        ++count;
                    }
                } catch (Exception e) {
                    addError(e);
                }
            }

            if (count > 1) {
                addError("Only one of '%s' can be provided", String.join(", ", Arrays.asList(properties)));
            }
        }

        return this ;
    }

    /**
     * Checks that the provided value is one of the provided classes
     * @param value the value to be checked
     * @param classes the list of permitted classes that the provided value must adhere to
     * @return this Validation
     */
    public Validation assertClass(Object value, List<Class<?>> classes) {
        if (value != null && classes.stream().noneMatch(aClass -> value.getClass().isAssignableFrom(aClass))) {
            addError("Value must one of '%s', but was '%s'",
                classes.stream().map(Class::getName).collect(Collectors.joining(", ")), value.getClass().getName());
        }

        return this ;
    }

    /**
     * Was the validation successful,
     * @return {@code true} if the validation was successful, {@code false} otherwise
     */
    public boolean isValid() {
        return errors.isEmpty();
    }

    /**
     * Merge the objects to Validation, by calling the {@link Validatable#validate()} method
     * and adding any errors to this Validation
     * @param validatable the object to be validated
     * @return this Validation
     */
    public Validation merge(Validatable validatable) {
        if (validatable != null) {
            addAllErrors(validatable.validate().getErrors());
        } else {
            addError("Can not merge null Validatable!");
        }

        return this ;
    }

    /**
     * Merge the Response to Validation, by adding any errors to this Validation
     * @param response the object to be validated
     * @return this Validation
     */
    public Validation merge(Response<?> response) {
        if (response != null) {
            addAllErrors(response.getAllErrors());
        } else {
            addError("Can not merge null Validatable!");
        }

        return this ;
    }

    /**
     * Merge the objects to Validation, by calling the {@link Validatable#validate()} method
     * and adding any errors to this Validation
     * @param validatable the object to be validated
     * @param prefixMessage prefix any errors with this message
     * @return this Validation
     */
    public Validation merge(Validatable validatable, String prefixMessage) {
        if (validatable != null) {
            addAllErrors(validatable.validate().getErrors(), prefixMessage);
        } else {
            addError("Can not merge null Validatable!");
        }

        return this ;
    }

    /**
     * If the condition is true, merge the objects to Validation, by calling the {@link Validatable#validate()} method
     * and adding any errors to this Validation
     * @param condition if {@code true} merge, otherwise don't
     * @param validatable the object to be validated
     * @return this Validation
     */
    public Validation mergeOnCondition(boolean condition, Validatable validatable) {
        if (condition) {
            if (validatable != null) {
                addAllErrors(validatable.validate().getErrors());
            } else {
                addError("Can not merge null Validatable!");
            }
        }

        return this ;
    }

    /**
     * If the condition is true, merge the objects to Validation, by calling the {@link Validatable#validate()} method
     * and adding any errors to this Validation
     * @param condition if {@code true} merge, otherwise don't
     * @param validatable the object to be validated
     * @param prefixMessage prefix any errors with this message
     * @return this Validation
     */
    public Validation mergeOnCondition(boolean condition, Validatable validatable, String prefixMessage) {
        if (condition) {
            if (validatable != null) {
                addAllErrors(validatable.validate().getErrors(), prefixMessage);
            } else {
                addError("Can not merge null Validatable!");
            }
        }

        return this ;
    }

    /**
     * Gets a list of all error messages or an empty list if there are no errors.
     * @return a list of all error messages or an empty list if there are no errors.
     */
    public List<String> getErrorMessages() {
        return errors.stream().map(Response.Error::getMessage).toList() ;
    }

    /**
     * Gets concatenated a list of all error messages or <code>null</code> if there are no errors.
     * @return a concatenated a list of all error messages or <code>null</code> if there are no errors.
     */
    public String getAllErrorsMessage() {
        if (errors.isEmpty()) {
            return null ;
        } else {
            return errors.stream().map(Response.Error::getMessage).collect(Collectors.joining(", "));
        }
    }

    /**
     * Merge the objects to Validation, by calling the {@link Validatable#validate()} method
     * in each object and adding any errors to this Validation
     * @param validatableList the options to be validated
     * @return this Validation
     * @param <T> the class of Validatable object
     */

    public <T extends Validatable> Validation merge(List<T> validatableList) {
        if (validatableList != null) {
            validatableList
                .stream()
                .map(Validatable::validate)
                .map(Validation::getErrors)
                .forEach(this::addAllErrors);
        }

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

    private void addError(String errorMessage, Object... args) {
        String message = args != null && args.length > 0 ? String.format(errorMessage, args) : errorMessage;
        errors.add(Response.Error.of("", message, Response.ErrorType.VALIDATION));
    }

    private void addError(String message) {
        errors.add(Response.Error.of("", message, Response.ErrorType.VALIDATION));
    }

    private void addError(Exception e) {
        errors.add(Response.Error.of("", e.getMessage(), Response.ErrorType.VALIDATION));
    }

    private void addAllErrors(Collection<Response.Error> errors) {
        this.errors.addAll(errors);
    }

    private void addAllErrors(Collection<Response.Error> errors, String prefixMessage) {
        errors.forEach(error ->
            this.errors.add(Response.Error.of(error.getCode(), String.format("%s %s", prefixMessage, error.getMessage()), error.getType()))
        );
    }
}
