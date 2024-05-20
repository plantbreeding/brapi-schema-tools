package org.brapi.schematools.core.response;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
    this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
    this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
    this.result = response.result;
  }

  public static <T> Response<T> success(T result) {
    return new Response<>(result);
  }

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

  public static <T> Response<T> fail(ErrorType type, String code, String message) {
    return fail(type, Error.of(code, message, type));
  }

  public static <T> Response<T> fail(ErrorType type, String message) {
    return fail(type, "", message);
  }

  public static <T> Response<T> failWithLogging(ErrorType type, String message, ErrorLogging errorLogging) {
    errorLogging.executeLogging();
    return fail(type, "", message);
  }

  @FunctionalInterface
  public interface ErrorLogging {
    void executeLogging();
  }

  public Response<T> mergeErrors(Response<?> response) {
    this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
    this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
    this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
    return this;
  }

  public <C> Response<T> mergeErrors(Supplier<Response<C>> supplier) {
    Response<C> response = supplier.get();
    this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
    this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
    this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
    return this;
  }

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

  public <C> Response<T> mapResultErrors(Function<T, Response<C>> function) {
    if (this.hasNoErrors()) {
      Response<C> response = function.apply(this.getResult());
      this.getErrors(ErrorType.VALIDATION).addAll(response.getErrors(ErrorType.VALIDATION));
      this.getErrors(ErrorType.PERMISSION).addAll(response.getErrors(ErrorType.PERMISSION));
      this.getErrors(ErrorType.OTHER).addAll(response.getErrors(ErrorType.OTHER));
    }
    return this;
  }

  public T getResult() {
    if (this.hasNoErrors()) return this.result;
    else
      throw new RuntimeException(String.format("Trying to access the result while an error has occurred, [%s]", this.getMessagesCombined(";")));
  }

  public T orElseResult(T result) {
    return this.hasErrors() ? result : this.getResult();
  }

  public T orElseGetResult(Supplier<T> supplier) {
    return this.hasErrors() ? supplier.get() : this.getResult();
  }

  public T getResultOrThrow(Exception ex) throws Exception {
    if (this.hasErrors()) throw ex;
    return this.getResult();
  }

  public T getResultOrThrow(Supplier<RuntimeException> supplier) {
    if (this.hasErrors()) throw supplier.get();
    return this.getResult();
  }

  public T getResultOrThrow(Function<Response<?>, ? extends RuntimeException> function) {
    if (this.hasErrors()) throw function.apply(this);
    return this.getResult();
  }

  public T getResultOrThrow() {
    return getResultOrThrow(() -> new ResponseFailedException(this));
  }

  public boolean hasErrors() {
    return !this.getValidationErrors().isEmpty() || !this.getPermissionErrors().isEmpty() || !this.getOtherErrors().isEmpty();
  }

  public boolean hasNoErrors() {
    return this.getValidationErrors().isEmpty() && this.getPermissionErrors().isEmpty() && this.getOtherErrors().isEmpty();
  }

  public <U> Response<U> merge(Response<U> response) {
    response.getErrors(ErrorType.VALIDATION).addAll(this.getErrors(ErrorType.VALIDATION));
    response.getErrors(ErrorType.PERMISSION).addAll(this.getErrors(ErrorType.PERMISSION));
    response.getErrors(ErrorType.OTHER).addAll(this.getErrors(ErrorType.OTHER));
    return response;
  }

  public <U> Response<U> merge(Supplier<Response<U>> responseSupplier) {
    final var response = responseSupplier.get();
    response.getErrors(ErrorType.VALIDATION).addAll(this.getErrors(ErrorType.VALIDATION));
    response.getErrors(ErrorType.PERMISSION).addAll(this.getErrors(ErrorType.PERMISSION));
    response.getErrors(ErrorType.OTHER).addAll(this.getErrors(ErrorType.OTHER));
    return response;
  }

  public <U> Response<U> mergeOnCondition(boolean condition, Supplier<Response<U>> supplier) {
    return condition ? this.merge(supplier.get()) :
            new Response<U>().mergeErrors(this);
  }

  public <U> Response<U> mapOnCondition(boolean condition, Function<Response<T>, Response<U>> function) {
    return condition && this.hasNoErrors() ? this.merge(function.apply(this)) :
            new Response<U>().mergeErrors(this);
  }

  public <U> Response<U> mapOnCondition(boolean condition, Supplier<Response<U>> supplier) {
    return condition && this.hasNoErrors() ? this.merge(supplier.get()) :
            new Response<U>().mergeErrors(this);
  }

  public <U> Response<U> mapResponseOnCondition(boolean condition, Function<T, Response<U>> function) {
    return condition && this.hasNoErrors() ? this.merge(function.apply(this.getResult())) :
            new Response<U>().mergeErrors(this);
  }

  public <U> Response<U> map(Supplier<Response<U>> supplier) {
    return mergeOnCondition(this.hasNoErrors(), supplier);
  }

  public <U> Response<U> map(Function<Response<T>, Response<U>> function) {
    return this.hasNoErrors() ? function.apply(this) : new Response<U>().mergeErrors(this);
  }

  public Response<T> or(Supplier<Response<T>> supplier) {
    return this.hasErrors() ? supplier.get() : this;
  }

  public Response<T> orElse(Response<T> response) {
    return this.or(() -> response);
  }

  public <U> Response<U> mapResult(Function<T, U> function) {
    if (this.hasNoErrors()) {
      return new Response<>(function.apply(this.getResult()));
    } else {
      return new Response<U>().mergeErrors(this);
    }
  }

  public <RESULT> Response<RESULT> withResult(RESULT result) {
    if (this.hasNoErrors()) {
      return new Response<>(result);
    } else {
      return new Response<RESULT>().mergeErrors(this);
    }
  }

  public <RESULT> Response<RESULT> withResult(Supplier<RESULT> result) {
    if (this.hasNoErrors()) {
      return new Response<>(result.get());
    } else {
      return new Response<RESULT>().mergeErrors(this);
    }
  }

  public Response<T> conditionalMapResult(boolean condition, UnaryOperator<T> function) {
    if (this.hasNoErrors()) {
      return condition ? new Response<>(function.apply(this.getResult())) : this;
    } else {
      return this;
    }
  }

  public Response<T> conditionalMapResultToResponse(Predicate<T> predicate, Supplier<Response<T>> supplier) {
    if (this.hasNoErrors()) {
      return predicate.test(this.getResult()) ? supplier.get() : this;
    } else {
      return this;
    }
  }

  public Response<T> conditionalMapResultToResponse(Predicate<T> predicate, Function<T, Response<T>> function) {
    if (this.hasNoErrors()) {
      return predicate.test(this.getResult()) ? function.apply(this.getResult()) : this;
    } else {
      return this;
    }
  }

  public Response<T> conditionalMapResultToResponse(boolean condition, Supplier<Response<T>> supplier) {
    if (this.hasNoErrors()) {
      return condition ? supplier.get() : this;
    } else {
      return this;
    }
  }

  public Response<T> conditionalMapResultToResponse(boolean condition, Function<T, Response<T>> function) {
    if (this.hasNoErrors()) {
      return condition ? function.apply(this.getResult()) : this;
    } else {
      return this;
    }
  }

  public <U> Response<U> mapResultToResponse(Function<T, Response<U>> function) {
    if (this.hasNoErrors()) {
      return function.apply(this.getResult());
    } else {
      return new Response<U>().mergeErrors(this);
    }
  }

  public Response<T> justDo(Supplier<Void> supplier) {
    supplier.get();
    return this;
  }

  public Response<T> justDoOnCondition(boolean condition, Supplier<Void> supplier) {
    if (condition) supplier.get();
    return this;
  }

  public Response<T> onSuccessDo(Consumer<Response<T>> consumer) {
    if (this.hasErrors()) {
      return this;
    } else {
      consumer.accept(this);
      return this;
    }
  }

  public Response<T> onSuccessDo(Supplier<Void> supplier) {
    if (this.hasErrors()) {
      return this;
    } else {
      supplier.get();
      return this;
    }
  }

  public Response<T> onSuccessDo(Runnable action) {
    if (this.hasErrors()) {
      return this;
    } else {
      action.run();
      return this;
    }
  }

  public Response<T> onSuccessDoWithResult(Consumer<T> consumer) {
    if (this.hasErrors()) {
      return this;
    } else {
      consumer.accept(this.getResult());
      return this;
    }
  }

  public Response<T> onSuccessDoOnCondition(Predicate<T> condition, Supplier<Void> supplier) {
    if (this.hasErrors()) {
      return this;
    } else {
      if (condition.test(this.getResult())) supplier.get();
      return this;
    }
  }

  public Response<T> onSuccessDoOnCondition(final Predicate<T> condition, final Runnable runnable) {
    if (this.hasErrors()) {
      return this;
    } else {
      if (condition.test(this.getResult())) runnable.run();
      return this;
    }
  }

  public Response<T> onSuccessDoOnCondition(final boolean condition, final Runnable action) {
    if (!this.hasErrors() && condition) {
      action.run();
    }
    return this;
  }

  public Response<T> onFailDo(final Runnable callBack) {
    if (this.hasErrors()) {
      callBack.run();
    }

    return this;
  }

  public Response<T> onFailDoWithResponse(Consumer<Response<T>> consumer) {
    if (this.hasErrors()) {
      consumer.accept(this);
      return this;
    } else {
      return this;
    }
  }

  public boolean isPresent() {
    return !this.hasErrors() && result != null;
  }

  public boolean isEmpty() {
    return !this.hasErrors() && result == null;
  }

  public T getResultIfPresentOrElseResult(Supplier<T> supplier) {
    return this.isPresent() ? this.getResult() : supplier.get();
  }

  public T getResultIfPresentOrElseResult(T result) {
    return this.isPresent() ? this.getResult() : result;
  }

  public <U> Response<U> ifPresentMapResultToResponseOr(Function<T, Response<U>> function, Supplier<Response<U>> supplier) {
    if (this.isPresent()) {
      return function.apply(this.getResult());
    } else {
      return supplier.get();
    }
  }

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

  public Response<T> addError(ErrorType type, String code, String message) {
    return this.addError(type, Error.of(code, message, type));
  }

  private Set<Error> getErrors(ErrorType errorType) {
    return this.errors.get(errorType);
  }

  public Collection<Error> getValidationErrors() {
    return Collections.unmodifiableSet(getErrors(ErrorType.VALIDATION));
  }

  public Collection<Error> getPermissionErrors() {
    return Collections.unmodifiableSet(getErrors(ErrorType.PERMISSION));
  }

  public Collection<Error> getOtherErrors() {
    return Collections.unmodifiableSet(getErrors(ErrorType.OTHER));
  }

  public Collection<Error> getAllErrors() {
    return Stream.concat(Stream.concat(getErrors(ErrorType.VALIDATION).stream(), getErrors(ErrorType.PERMISSION).stream()), getErrors(ErrorType.OTHER).stream()).collect(Collectors.toList());
  }


  public List<String> getMessages() {
    return Stream.concat(Stream.concat(getErrors(ErrorType.VALIDATION).stream(), getErrors(ErrorType.PERMISSION).stream()), getErrors(ErrorType.OTHER).stream())
            .map(Error::getMessage)
            .collect(Collectors.toList());
  }

  public String getMessagesCombined(String delimiter) {
    return String.join(delimiter, getMessages());
  }

  public static <T> Collector<Response<T>, Response<List<T>>, Response<List<T>>> toList() {
    return Collector.of(() -> new Response<>(new ArrayList<>()), getResultToResultListAccumulator(), getListCombiner(),
            Collector.Characteristics.IDENTITY_FINISH);
  }

  public static <T> Collector<Response<T>, Response<Set<T>>, Response<Set<T>>> toSet() {
    return Collector.of(() -> new Response<>(new HashSet<>()), getResultToResultSetAccumulator(), getSetCombiner(),
            Collector.Characteristics.UNORDERED);
  }

  public static <T> Collector<Response<List<T>>, Response<List<T>>, Response<List<T>>> mergeLists() {
    return Collector.of(() -> new Response<>(new ArrayList<>()), getResultListAccumulator(), getListCombiner(),
            Collector.Characteristics.IDENTITY_FINISH);
  }

  @Value(staticConstructor = "of")
  public static class Error {
    String code;
    String message;
    ErrorType type;
  }

  public enum ErrorType {
    VALIDATION,
    PERMISSION,
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
