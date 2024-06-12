package org.brapi.schematools.core.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.schematools.core.response.Response;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * The type of relationship between types
 */
@Getter
@AllArgsConstructor
public enum BrAPIRelationshipType {
    ONE_TO_ONE ("one-to-one"),
    ONE_TO_MANY ("one-to-many"),
    MANY_TO_ONE ("many-to-one"),
    MANY_TO_MANY ("many-to-many") ;

    final String label ;

    public static Response<BrAPIRelationshipType> fromNameOrLabel(final String nameOrLabel) {
        return findByNameOrLabel(nameOrLabel)
            .map(Response::success)
            .orElseGet(() -> fail(Response.ErrorType.VALIDATION, String.format("No BrAPIRelationshipType found for name or label [%s]",nameOrLabel)));
    }

    public static Response<List<BrAPIRelationshipType>> fromNameOrLabels(List<String> types) {
        return types.stream().map(BrAPIRelationshipType::fromNameOrLabel).collect(Response.toList()) ;
    }

    public static Optional<BrAPIRelationshipType> findByNameOrLabel(final String nameOrLabel) {
        return Stream.of(values())
            .filter(type -> type.name().equalsIgnoreCase(nameOrLabel) || type.getLabel().equalsIgnoreCase(nameOrLabel))
            .findAny();
    }
}
