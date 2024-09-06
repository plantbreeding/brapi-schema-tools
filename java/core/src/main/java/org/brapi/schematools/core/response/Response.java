package org.brapi.schematools.core.response;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.options.Validation;

import java.nio.file.Path;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A generic Response class for functional programming.
 * A successful Response is one that has no errors, whereas a failed Response has errors
 *
 * @param <T> The type of result
 */
@Slf4j
public class Response<T> {
    private T result;

    private final EnumMap<ErrorType, Set<Error>> errors = new EnumMap<>(ErrorType.class);

    private Response(T result) {
        this.errors.put(ErrorType.VALIDATION, new HashSet<>());
        this.errors.put(ErrorType.PERMISSION, new HashSet<>());
        this.errors.put(ErrorType.OTHER, new HashSet<>());
        this.result = result;
    }

    private Response() {
        this.errors.put(ErrorType.VALIDATION, new HashSet<>());
        this.errors.put(ErrorType.PERMISSION, new HashSet<>());
        this.errors.put(ErrorType.OTHER, new HashSet<>());
    }

    private Response(Response<T> response) {
        this() ;
        this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
        this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
        this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
        this.result = response.result;
    }

    private Response(Validation validation) {
        this() ;
        validation.getErrors().forEach(error -> {
            switch (error.getType()) {
                case VALIDATION:
                    this.getErrors(ErrorType.VALIDATION).add(error);
                    break;
                case PERMISSION:
                    this.getErrors(ErrorType.PERMISSION).add(error);
                    break;
                case OTHER:
                    this.getErrors(ErrorType.OTHER).add(error);
            }
        });
    }

    /**
     * Creates a successful response for the provided result
     * @param result the result
     * @return a successful response for the provided result
     * @param <T> The type of the result
     */
    public static <T> Response<T> success(T result) {
        return new Response<>(result);
    }

    /**
     * Creates an empty response
     * @return a empty response
     * @param <T> The type of the result
     */
    public static <T> Response<T> empty() {
        return new Response<>();
    }

    private static <T> Response<T> fail(ErrorType type, Error error) {
        Response<T> response = new Response<>();
        switch (type) {
            case VALIDATION:
                response.getErrors(ErrorType.VALIDATION).add(error);
                break;
            case PERMISSION:
                response.getErrors(ErrorType.PERMISSION).add(error);
                break;
            case OTHER:
                response.getErrors(ErrorType.OTHER).add(error);
        }
        return response;
    }

    /**
     * Creates a failed response
     * @param type The type of error
     * @param code The error code
     * @param message The error message
     * @return an empty response with the added error
     * @param <T> The type of the result
     */
    public static <T> Response<T> fail(ErrorType type, String code, String message) {
        return fail(type, Error.of(code, message, type));
    }

    /**
     * Creates a failed response
     * @param type The type of error
     * @param message The error message
     * @return an empty response with the added error
     * @param <T> The type of the result
     */
    public static <T> Response<T> fail(ErrorType type, String message) {
        return fail(type, "", message);
    }

    /**
     * Creates a failed response for the validation of a file path
     * @param type The type of error
     * @param path the path of the file being validated
     * @param message The error message
     * @return an empty response with the added error
     * @param <T> The type of the result
     */
    public static <T> Response<T> fail(ErrorType type, Path path, String message) {
        return fail(type, "", String.format("In '%s' %s", path.toFile(), message));
    }

    /**
     * Creates a failed response, and executes a logging call back
     * @param type The type of error
     * @param message The error message
     * @param errorLogging the error logging functional interface
     * @return an empty response with the added error
     * @param <T> The type of the result
     */
    public static <T> Response<T> failWithLogging(ErrorType type, String message, ErrorLogging errorLogging) {
        errorLogging.executeLogging();
        return fail(type, "", message);
    }

    /**
     * Functional Interface to support error logging.
     */
    @FunctionalInterface
    public interface ErrorLogging {
        /**
         * Execute the error logging
         */
        void executeLogging();
    }

    /**
     * Merge the errors from the provided response into this response
     * @param response the provided response which is the source of the errors
     * @return this response
     */
    public Response<T> mergeErrors(Response<?> response) {
        this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
        this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
        this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
        return this;
    }

    /**
     * Merge the errors from the provided response supplier into this response
     * @param supplier the supplier which providers the response that is the source of the errors
     * @return this response
     * @param <C> the result type of the output response for the provided supplier
     */
    public <C> Response<T> mergeErrors(Supplier<Response<C>> supplier) {
        Response<C> response = supplier.get();
        this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
        this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
        this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
        return this;
    }

    /**
     * Map the errors response of the provided function executed on this response, removing any
     * existing errors on this response
     * @param function the function which providers the response that is the source of the errors
     * @return this response
     * @param <C> the result type of the output response for the provided function
     */
    public <C> Response<T> mapErrors(Function<Response<T>, Response<C>> function) {
        Response<C> response = function.apply(this);
        this.getErrors(ErrorType.VALIDATION).clear();
        this.getErrors(ErrorType.PERMISSION).clear();
        this.getErrors(ErrorType.OTHER).clear();
        this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
        this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
        this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
        return this;
    }

    /**
     * If this response has no errors it maps result of the provided function executed on the result on this response
     * @param function the function which takes the result of this and returns a new response
     * @return this response
     * @param <C> the result type of the output response for the provided function
     */
    public <C> Response<T> mapResultErrors(Function<T, Response<C>> function) {
        if (this.hasNoErrors()) {
            Response<C> response = function.apply(this.getResult());
            this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
            this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
            this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
        }
        return this;
    }

    /**
     * Get the result of this response if there are no errors. If there are errors a {@link RuntimeException} is thrown
     * @return the result of this response if there are no errors
     */
    public T getResult() {
        if (this.hasNoErrors()) return this.result;
        else
            throw new RuntimeException(String.format("Trying to access the result while an error has occurred, [%s]", this.getMessagesCombined(";")));
    }

    /**
     * Get the result of this response if there are no errors in this response, otherwise returns the provided result
     * @param result the result that is return if there are errors in this response
     * @return the result of this response if there are no errors in this response or the provided result
     */
    public T orElseResult(T result) {
        return this.hasErrors() ? result : this.getResult();
    }

    /**
     * Get the result of this response if there are no errors in this response, otherwise returns supplier
     * is used to provide result
     * @param supplier the supplier that provides the result if there are errors in this response
     * @return the result of this response if there are no errors in this response or the result from the supplier
     */
    public T orElseGetResult(Supplier<T> supplier) {
        return this.hasErrors() ? supplier.get() : this.getResult();
    }

    /**
     * Get the result of this response if there are no errors in this response, otherwise throws the
     * provided exception
     * @param ex the exception to be thrown if there are errors in this response
     * @return the result of this response if there are no errors in this response
     * @throws Exception the provided exception if there are errors
     */
    public T getResultOrThrow(Exception ex) throws Exception {
        if (this.hasErrors()) throw ex;
        return this.getResult();
    }

    /**
     * Get the result of this response if there are no errors in this response, otherwise obtains
     * an exception from the supplier to be thrown.
     * @param supplier the supplier exception to be used if there are errors in this response
     * @return the result of this response if there are no errors in this response
     */
    public T getResultOrThrow(Supplier<RuntimeException> supplier) {
        if (this.hasErrors()) throw supplier.get();
        return this.getResult();
    }

    /**
     * Get the result of this response if there are no errors in this response, otherwise obtains
     * an exception from the function to be thrown.
     * @param function the function use to create the exception to be thrown if there are errors in this response
     * @return the result of this response if there are no errors in this response
     */
    public T getResultOrThrow(Function<Response<?>, ? extends RuntimeException> function) {
        if (this.hasErrors()) throw function.apply(this);
        return this.getResult();
    }

    /**
     * Get the result of this response if there are no errors in this response, or throws an
     * {@link ResponseFailedException}
     * @return the result of this response if there are no errors in this response
     */
    public T getResultOrThrow() {
        return getResultOrThrow(() -> new ResponseFailedException(this));
    }

    /**
     * Determines if this response has any errors.
     * @return <code>true</code> if this response has any errors, <code>false</code> otherwise
     */
    public boolean hasErrors() {
        return !this.getValidationErrors().isEmpty() || !this.getPermissionErrors().isEmpty() || !this.getOtherErrors().isEmpty();
    }

    /**
     * Determines if this response has no errors.
     * @return <code>true</code> if this response has no errors, <code>false</code> otherwise
     */
    public boolean hasNoErrors() {
        return this.getValidationErrors().isEmpty() && this.getPermissionErrors().isEmpty() && this.getOtherErrors().isEmpty();
    }

    /**
     * Merges any errors from this response into provided response,
     * the result from this response is lost.
     * @param response the response used as a source of errors.
     * @return the provided response
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> merge(Response<U> response) {
        response.getErrors(ErrorType.VALIDATION).addAll(this.getErrors(ErrorType.VALIDATION));
        response.getErrors(ErrorType.PERMISSION).addAll(this.getErrors(ErrorType.PERMISSION));
        response.getErrors(ErrorType.OTHER).addAll(this.getErrors(ErrorType.OTHER));
        return response;
    }

    /**
     * Merges any errors from this response into response obtained from the provider supplier,
     * the result from this response is lost.
     * @param responseSupplier the supplier used to provide the response
     * @return the provided response from the supplier
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> merge(Supplier<Response<U>> responseSupplier) {
        final var response = responseSupplier.get();
        response.getErrors(ErrorType.VALIDATION).addAll(this.getErrors(ErrorType.VALIDATION));
        response.getErrors(ErrorType.PERMISSION).addAll(this.getErrors(ErrorType.PERMISSION));
        response.getErrors(ErrorType.OTHER).addAll(this.getErrors(ErrorType.OTHER));
        return response;
    }

    /**
     * Merges any errors from the validation,
     * @param validation the validation used as a source of errors.
     * @return the provided response
     * @param <U> the result type of the provided validation
     */
    public <U> Response<U> merge(Validation validation) {
        return new Response<>(validation);
    }

    /**
     * If the condition is <code>true</code> merges any errors from this response into response obtained
     * from the provided supplier, the result from this response is lost.
     * @param condition set to <code>true</code> to perform the merge, <code>false</code> not to perform the merge
     * @param supplier a supplier of the provided response
     * @return the provided response from the supplier
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> mergeOnCondition(boolean condition, Supplier<Response<U>> supplier) {
        return condition ? this.merge(supplier.get()) :
            new Response<U>().mergeErrors(this);
    }

    /**
     * If the condition is <code>true</code> and there are no errors from this response
     * merge the response obtained from the provided function, otherwise create a new response and merge in the errors
     * from this response. In either case the result from this response is lost.
     * @param condition set to <code>true</code> to perform the mapping, <code>false</code> not to perform the mapping
     * @param function a function that takes this response and provides a new response
     * @return the provided response from the function if the condition is <code>true</code> and there are no errors
     * from this response or a new response
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> mapOnCondition(boolean condition, Function<Response<T>, Response<U>> function) {
        return condition && this.hasNoErrors() ? this.merge(function.apply(this)) :
            new Response<U>().mergeErrors(this);
    }

    /**
     * If the condition is <code>true</code> and there are no errors from this response
     * merge any errors from this response into response obtained from the provided supplier,
     * otherwise create a new response and merge in the errors
     * from this response. In either case the result from this response is lost.
     * @param condition set to <code>true</code> to perform the merge, <code>false</code> not to perform the merge
     * @param supplier a supplier that provides a new response
     * @return the provided response from the supplier
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> mapOnCondition(boolean condition, Supplier<Response<U>> supplier) {
        return condition && this.hasNoErrors() ? this.merge(supplier.get()) :
            new Response<U>().mergeErrors(this);
    }

    /**
     * If the condition is <code>true</code> merges any errors from this response into response obtained from the
     * provided function, otherwise create a new response and merges in the errors
     * from this response. In either case the result from this response is lost.
     * @param condition set to <code>true</code> to perform the merge, <code>false</code> not to perform the merge
     * @param function a function that takes this response and provides a new response
     * @return the provided response from the function
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> mapResponseOnCondition(boolean condition, Function<T, Response<U>> function) {
        return condition && this.hasNoErrors() ? this.merge(function.apply(this.getResult())) :
            new Response<U>().mergeErrors(this);
    }

    /**
     * Merges the response from the supplier if this response has no errors, otherwise create a new response and merges in the errors
     * from this response. In either case the result from this response is lost.
     * @param supplier a supplier that provides a new response
     * @return the provided response from the supplier
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> map(Supplier<Response<U>> supplier) {
        return mergeOnCondition(this.hasNoErrors(), supplier);
    }

    /**
     * Merges the response from the function if this response has no errors, otherwise create a new response and merges in the errors
     * from this response. In either case the result from this response is lost.
     * @param function a function that takes this response and provides a new response
     * @return the provided response from the function
     * @param <U> the result type of the provided response
     */
    public <U> Response<U> map(Function<Response<T>, Response<U>> function) {
        return this.hasNoErrors() ? function.apply(this) : new Response<U>().mergeErrors(this);
    }

    /**
     * Returns this response if it has no errors, otherwise returns the provided response from the supplier
     * @param supplier a supplier that provides a new response
     * @return this response if it has no errors, otherwise returns the provided response from the supplier
     */
    public Response<T> or(Supplier<Response<T>> supplier) {
        return this.hasErrors() ? supplier.get() : this;
    }

    /**
     * Returns this response if it has no errors, otherwise returns the provided response
     * @param response a new response
     * @return this response if it has no errors, otherwise returns the provided response
     */
    public Response<T> orElse(Response<T> response) {
        return this.or(() -> response);
    }

    /**
     * If this response has no errors returns a new response that takes the
     * result of provided function that takes the result of this resource as an input,
     * otherwise create a new response and merges in the errors
     * from this response.
     * @param function a function that takes the result of this response as an input
     * @return a new response with result of the function, or with any merged errors
     * @param <U> the result type of the new response
     */
    public <U> Response<U> mapResult(Function<T, U> function) {
        if (this.hasNoErrors()) {
            return new Response<>(function.apply(this.getResult()));
        } else {
            return new Response<U>().mergeErrors(this);
        }
    }

    /**
     * If this response has no errors creates a new Response with the provided result, otherwise
     * a new Response with the errors from this resource.
     * @param result the new result
     * @return the new Response
     * @param <RESULT> the result type of the new response
     */
    public <RESULT> Response<RESULT> withResult(RESULT result) {
        if (this.hasNoErrors()) {
            return new Response<>(result);
        } else {
            return new Response<RESULT>().mergeErrors(this);
        }
    }

    /**
     * If this response has no errors creates a new Response with the result from the supplier, otherwise
     * a new Response with the errors from this resource.
     * @param supplier a supplier for this new result
     * @return the new Response
     * @param <U> the result type of the new response
     */
    public <U> Response<U> withResult(Supplier<U> supplier) {
        if (this.hasNoErrors()) {
            return new Response<>(supplier.get());
        } else {
            return new Response<U>().mergeErrors(this);
        }
    }

    /**
     * If the condition is <code>true</code> and this response has no errors creates a new Response
     * with the result from the function, otherwise a new Response with the errors from this resource.
     * @param condition <code>true</code> map the result, <code>false</code> do not map the result
     * @param function that takes the result from this response and returns a new result of the same type
     * @return a new Response or returns this
     */
    public Response<T> conditionalMapResult(boolean condition, UnaryOperator<T> function) {
        if (this.hasNoErrors()) {
            return condition ? new Response<>(function.apply(this.getResult())) : this;
        } else {
            return this;
        }
    }

    /**
     * If the predicate returns <code>true</code> and this response has no errors creates a new Response
     * with the result from the function, otherwise a new Response with the errors from this resource.
     * @param predicate if the predicate returns <code>true</code> map the result, <code>false</code> do not map the result
     * @param supplier that provides a new result of the same type
     * @return a new Response or returns this
     */
    public Response<T> conditionalMapResultToResponse(Predicate<T> predicate, Supplier<Response<T>> supplier) {
        if (this.hasNoErrors()) {
            return predicate.test(this.getResult()) ? supplier.get() : this;
        } else {
            return this;
        }
    }

    /**
     * If the predicate returns <code>true</code> and this response has no errors creates a new Response
     * with the result from the function, otherwise a new Response with the errors from this resource.
     * @param predicate if the predicate returns <code>true</code> map the result, <code>false</code> do not map the result
     * @param function that takes the result from this response and returns a new result of the same type
     * @return a new Response or returns this
     */
    public Response<T> conditionalMapResultToResponse(Predicate<T> predicate, Function<T, Response<T>> function) {
        if (this.hasNoErrors()) {
            return predicate.test(this.getResult()) ? function.apply(this.getResult()) : this;
        } else {
            return this;
        }
    }

    /**
     * If the condition is <code>true</code> and this response has no errors use the supplier to get
     * the new response
     * @param condition <code>true</code> map the result, <code>false</code> do not map the result
     * @param supplier that providers a new Response
     * @return a new Response or returns this
     */
    public Response<T> conditionalMapResultToResponse(boolean condition, Supplier<Response<T>> supplier) {
        if (this.hasNoErrors()) {
            return condition ? supplier.get() : this;
        } else {
            return this;
        }
    }

    /**
     * If the condition is <code>true</code> and this response has no errors use the function to get
     * the new response
     * @param condition <code>true</code> map the result, <code>false</code> do not map the result
     * @param function a function that providers a new Response taking the result of this response as input
     * @return a new Response or returns this
     */
    public Response<T> conditionalMapResultToResponse(boolean condition, Function<T, Response<T>> function) {
        if (this.hasNoErrors()) {
            return condition ? function.apply(this.getResult()) : this;
        } else {
            return this;
        }
    }

    /**
     * If this response has no errors use the function to get the new response
     * @param function a function that providers a new Response taking the result of this response as input
     * @return a new Response or returns this
     * @param <U> the result type of the new response
     */
    public <U> Response<U> mapResultToResponse(Function<T, Response<U>> function) {
        if (this.hasNoErrors()) {
            return function.apply(this.getResult());
        } else {
            return new Response<U>().mergeErrors(this);
        }
    }

    /**
     * Call the supplier and return this
     * @param supplier the supplier to be called.
     * @return this response
     */
    public Response<T> justDo(Supplier<Void> supplier) {
        supplier.get();
        return this;
    }

    /**
     * If the condition is <code>true</code> call the supplier, otherwise don't call the supplier
     * @param condition <code>true</code> call the supplier, <code>false</code> don't call the supplier
     * @param supplier the supplier to be called if the condition is <code>true</code>
     * @return this response
     */
    public Response<T> justDoOnCondition(boolean condition, Supplier<Void> supplier) {
        if (condition) supplier.get();
        return this;
    }

    /**
     * If this response has no errors pass this response to the provider consumer
     * @param consumer a consumer for this response
     * @return this response
     */
    public Response<T> onSuccessDo(Consumer<Response<T>> consumer) {
        if (!this.hasErrors()) {
            consumer.accept(this);
        }
        return this;
    }

    /**
     * If this response has no errors call the supplier and return this
     * @param supplier the supplier to be called if there are no errors
     * @return this response
     */
    public Response<T> onSuccessDo(Supplier<Void> supplier) {
        if (!this.hasErrors()) {
            supplier.get();
        }
        return this;
    }

    /**
     * If this response has no errors run the action and return this
     * @param action the action to be run if there are no errors
     * @return this response
     */
    public Response<T> onSuccessDo(Runnable action) {
        if (this.hasErrors()) {
            return this;
        } else {
            action.run();
            return this;
        }
    }

    /**
     * If this response has no errors pass the result of this response to the provider consumer
     * @param consumer a consumer for the result of this response
     * @return this response
     */
    public Response<T> onSuccessDoWithResult(Consumer<T> consumer) {
        if (this.hasErrors()) {
            return this;
        } else {
            consumer.accept(this.getResult());
            return this;
        }
    }

    /**
     * If the predicate returns <code>true</code> and this response has no errors call the supplier and return this
     * @param predicate if the predicate returns <code>true</code> call the supplier, <code>false</code> do not call the supplier
     * @param supplier the supplier to be called if the predicate returns <code>true</code> and there are no errors
     * @return this response
     */
    public Response<T> onSuccessDoOnCondition(Predicate<T> predicate, Supplier<Void> supplier) {
        if (!this.hasErrors()) {
            if (predicate.test(this.getResult())) supplier.get();
        }
        return this;
    }

    /**
     * If the predicate returns <code>true</code> and this response has no errors call run the action and return this
     * @param predicate if the predicate returns <code>true</code> run the action, <code>false</code> do not run the action
     * @param action the action to be run if the predicate returns <code>true</code> and there are no errors
     * @return this response
     */
    public Response<T> onSuccessDoOnCondition(final Predicate<T> predicate, final Runnable action) {
        if (!this.hasErrors()) {
            if (predicate.test(this.getResult())) action.run();
        }
        return this;
    }

    /**
     * If the condition is <code>true</code> and this response has no errors call run the action and return this
     * @param condition if is <code>true</code> run the action, <code>false</code> do not run the action
     * @param action the action to be run if the predicate returns <code>true</code> and there are no errors
     * @return this response
     */
    public Response<T> onSuccessDoOnCondition(final boolean condition, final Runnable action) {
        if (!this.hasErrors() && condition) {
            action.run();
        }
        return this;
    }

    /**
     * If this response has errors run the action and return this
     * @param action the action to be run if there are errors
     * @return this response
     */
    public Response<T> onFailDo(final Runnable action) {
        if (this.hasErrors()) {
            action.run();
        }

        return this;
    }

    /**
     * If this response has no errors pass this response to the provider consumer
     * @param consumer a consumer for this response
     * @return this response
     */
    public Response<T> onFailDoWithResponse(Consumer<Response<T>> consumer) {
        if (this.hasErrors()) {
            consumer.accept(this);
        }
        return this;
    }

    /**
     * Determines if response has no errors and the result is not null
     * @return If response has no errors and the result is not null
     */
    public boolean isPresent() {
        return !this.hasErrors() && result != null;
    }

    /**
     * Determines if response has no errors and the result is null
     * @return If response has no errors and the result is null
     */
    public boolean isEmpty() {
        return !this.hasErrors() && result == null;
    }

    /**
     * If response has no errors and the result is not null return the result, otherwise
     * return the result from the supplier
     * @param supplier the supplier to be used if response has no errors and the result is not null
     * @return the result from this response or from the supplier
     */
    public T getResultIfPresentOrElseResult(Supplier<T> supplier) {
        return this.isPresent() ? this.getResult() : supplier.get();
    }

    /**
     * If response has no errors and the result is not null return the result, otherwise
     * return the provider result from the supplier
     * @param result the result to returned if response has no errors and the result for this response is not null
     * @return the result from this response or from the supplier
     */
    public T getResultIfPresentOrElseResult(T result) {
        return this.isPresent() ? this.getResult() : result;
    }

    /**
     * If response has no errors and the result is not null apply the function to the result, otherwise use the
     * to get the new resource
     * @param function a function that takes the result of this response to create a new response
     * @param supplier a supplier that provides a new response
     * @return a new response from the function or supplier
     * @param <U> the result type of the new response
     */
    public <U> Response<U> ifPresentMapResultToResponseOr(Function<T, Response<U>> function, Supplier<Response<U>> supplier) {
        if (this.isPresent()) {
            return function.apply(this.getResult());
        } else {
            return supplier.get();
        }
    }

    /**
     * Adds an error to the response.
     *
     * @param type The type of error
     * @param error The error
     * @return this response
     */
    public Response<T> addError(ErrorType type, Error error) {
        switch (type) {
            case VALIDATION:
                this.getErrors(ErrorType.VALIDATION).add(error);
                break;
            case PERMISSION:
                this.getErrors(ErrorType.PERMISSION).add(error);
                break;
            case OTHER:
                log.error(error.getMessage());
                this.getErrors(ErrorType.OTHER).add(error);
        }
        return new Response<>(this);
    }

    /**
     * Adds an error to the response.
     *
     * @param type The type of error
     * @param code The error code
     * @param message The error message
     * @return this response
     */
    public Response<T> addError(ErrorType type, String code, String message) {
        return this.addError(type, Error.of(code, message, type));
    }

    /**
     * Gets the errors by error type
     * @param errorType the type of error required
     * @return the errors for the provided type
     */
    private Set<Error> getErrors(ErrorType errorType) {
        return this.errors.get(errorType);
    }

    /**
     * Get the validation errors in this response
     * @return the validation errors in this response
     */
    public Collection<Error> getValidationErrors() {
        return Collections.unmodifiableSet(getErrors(ErrorType.VALIDATION));
    }

    /**
     * Get the permission errors in this response
     * @return the permission errors in this response
     */
    public Collection<Error> getPermissionErrors() {
        return Collections.unmodifiableSet(getErrors(ErrorType.PERMISSION));
    }

    /**
     * Get the other errors in this response
     * @return the other errors in this response
     */
    public Collection<Error> getOtherErrors() {
        return Collections.unmodifiableSet(getErrors(ErrorType.OTHER));
    }

    /**
     * Get the all errors in this response, regardless of type
     * @return the all errors in this response, regardless of type
     */
    public Collection<Error> getAllErrors() {
        return Stream.concat(Stream.concat(getErrors(ErrorType.VALIDATION).stream(), getErrors(ErrorType.PERMISSION).stream()), getErrors(ErrorType.OTHER).stream()).collect(Collectors.toList());
    }

    /**
     * Gets a list of all the error message for this response
     * @return a list of all the error message for this response
     */
    public List<String> getMessages() {
        return Stream.concat(Stream.concat(getErrors(ErrorType.VALIDATION).stream(), getErrors(ErrorType.PERMISSION).stream()), getErrors(ErrorType.OTHER).stream())
            .map(Error::getMessage)
            .collect(Collectors.toList());
    }

    /**
     * Gets a combined error message for all the error messages for this response, separated with the provided delimiter
     * @param delimiter the delimiter to separate the individual error messages in the combined error message
     * @return a combined error message for all the error messages for this response, separated with the provided delimiter
     */
    public String getMessagesCombined(String delimiter) {
        return String.join(delimiter, getMessages());
    }

    /**
     * Combines all the results from a stream of Responses of type {@link T} into a single Response that has a result of type {@link List<T>},
     * merging any errors from the individual responses into the returned response.
     * @return a single Response that has a result of type {@link List<T>}
     * @param <T> the type of the result
     */
    public static <T> Collector<Response<T>, Response<List<T>>, Response<List<T>>> toList() {
        return Collector.of(() -> new Response<>(new ArrayList<>()), getResultToResultListAccumulator(), getListCombiner(),
            Collector.Characteristics.IDENTITY_FINISH);
    }

    /**
     * Combines all the results a stream of Responses of type{@link T} into a single Response that has a result of type {@link Set<T>},
     * merging any errors from the individual responses into the returned response.
     * @return a single Response that has a result of type {@link Set<T>}
     * @param <T> the type of the result
     */
    public static <T> Collector<Response<T>, Response<Set<T>>, Response<Set<T>>> toSet() {
        return Collector.of(() -> new Response<>(new HashSet<>()), getResultToResultSetAccumulator(), getSetCombiner(),
            Collector.Characteristics.UNORDERED);
    }

    /**
     * Combines all the results a stream of Responses of type {@link List<T>}into a single Response that has a result of type {@link List<T>},
     * merging any errors from the individual responses into the returned response.
     * @return a single Response that has a result of {@link List<T>}
     * @param <T> the type of the result
     */
    public static <T> Collector<Response<List<T>>, Response<List<T>>, Response<List<T>>> mergeLists() {
        return Collector.of(() -> new Response<>(new ArrayList<>()), getResultListAccumulator(), getListCombiner(),
            Collector.Characteristics.IDENTITY_FINISH);
    }

    /**
     * Wrapper around an error in the response.
     */
    @Value(staticConstructor = "of")
    public static class Error {
        String code;
        String message;
        ErrorType type;
    }

    /**
     * Type of error
     */
    public enum ErrorType {
        /**
         * Validation errors
         */
        VALIDATION,
        /**
         * Permission errors
         */
        PERMISSION,
        /**
         * Any other types of error
         */
        OTHER
    }

    private static <T> BiConsumer<Response<List<T>>, Response<T>> getResultToResultListAccumulator() {
        return (listResponse, response) -> {
            listResponse.mergeErrors(response);
            if (response.result != null) listResponse.result.add(response.result);
        };
    }

    private static <T> BiConsumer<Response<Set<T>>, Response<T>> getResultToResultSetAccumulator() {
        return (setResponse, response) -> {
            setResponse.mergeErrors(response);
            if (response.result != null) setResponse.result.add(response.result);
        };
    }

    private static <T> BiConsumer<Response<List<T>>, Response<List<T>>> getResultListAccumulator() {
        return (listResponse, response) -> {
            listResponse.mergeErrors(response);
            if (response.result != null) listResponse.result.addAll(response.result);

        };
    }

    private static <T> BinaryOperator<Response<List<T>>> getListCombiner() {
        return ((listResponse1, listResponse2) -> {
            listResponse1.mergeErrors(listResponse2);
            listResponse1.result.addAll(listResponse2.result);
            return listResponse1;
        });
    }

    private static <T> BinaryOperator<Response<Set<T>>> getSetCombiner() {
        return ((setResponse1, setResponse2) -> {
            setResponse1.mergeErrors(setResponse2);
            setResponse1.result.addAll(setResponse2.result);
            return setResponse1;
        });
    }
}
