package org.brapi.schematools.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

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
    boolean response ;
    @JsonProperty("interface")
    boolean interfaceClass ;
    List<String> controlledVocabularyProperties ;
    List<String> subQueryProperties ;
    List<String> updatableProperties ;
    List<String> writableProperties ;
    List<String> noSingularizeProperties ;
    /**
     * Properties on this class that represent action endpoints (e.g. {@code POST /variantsets/extract})
     * rather than data fields. The property's type is the action request body schema; the response is
     * the owning type's standard single-entity response. Action properties are excluded from the data
     * schema and from list/search/table query parameters. Currently only consumed by the OpenAPI
     * generator; other generators must filter them out using
     * {@link BrAPIMetadata#isActionProperty(String)}.
     */
    List<String> actionProperties ;
    String discriminatorPropertyName ;

    /**
     * Determines if the named property is declared as an action property on the owning class.
     *
     * @param propertyName the property name
     * @return {@code true} if the property is declared as an action property, {@code false} otherwise
     */
    public boolean isActionProperty(String propertyName) {
        return actionProperties != null && actionProperties.contains(propertyName);
    }
}
