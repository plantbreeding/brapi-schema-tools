package org.brapi.schematools.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * The BrAPI metadata associated with a {@link BrAPIClass}.
 */
@Builder(toBuilder = true)
@Value
public class BrAPIMetadata {
    boolean primaryModel ;
    boolean request ;
    boolean parameters ;
    @JsonProperty("interface")
    boolean interfaceClass ;
    List<String> controlledVocabularyProperties ;
    List<String> subQueryProperties ;
    List<String> updatableProperties ;
    List<String> writableProperties ;
}
