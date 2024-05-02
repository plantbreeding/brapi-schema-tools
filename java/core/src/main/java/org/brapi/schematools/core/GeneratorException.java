package org.brapi.schematools.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class GeneratorException extends RuntimeException {

  private List<Error> errors ;
  public GeneratorException(Collection<Error> errors) {
    errors = new ArrayList<>(errors) ;
  }
}
