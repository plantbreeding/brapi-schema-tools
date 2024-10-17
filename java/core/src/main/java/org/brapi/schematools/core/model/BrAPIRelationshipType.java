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
    /**
     * A relationship that denotes a one-to-one relationship
     */
    ONE_TO_ONE ("one-to-one"),
    /**
     * A relationship that denotes a one-to-many relationship
     */
    ONE_TO_MANY ("one-to-many"),
    /**
     * A relationship that denotes a many-to-one relationship
     */
    MANY_TO_ONE ("many-to-one"),
    /**
     * A relationship that denotes a many-to-many relationship
     */
    MANY_TO_MANY ("many-to-many") ;

    final String label ;

    /**
     * Find the Relationship Type by its name or label, case-insensitive.
     * @param nameOrLabel the name or label to search for
     * @return Relationship Type that matches the provided name or label
     */
    public static Response<BrAPIRelationshipType> fromNameOrLabel(final String nameOrLabel) {
        return findByNameOrLabel(nameOrLabel)
            .map(Response::success)
            .orElseGet(() -> fail(Response.ErrorType.VALIDATION, String.format("No BrAPIRelationshipType found for name or label [%s]",nameOrLabel)));
    }

    /**
     * Find the Relationship Types by names or labels, case-insensitive.
     * @param nameOrLabels a list of names or labels to search for
     * @return Relationship Types that matches the provided names or labels
     */
    public static Response<List<BrAPIRelationshipType>> fromNameOrLabels(List<String> nameOrLabels) {
        return nameOrLabels.stream().map(BrAPIRelationshipType::fromNameOrLabel).collect(Response.toList()) ;
    }

    /**
     * Find the Relationship Type by its name or label, case-insensitive.
     * @param nameOrLabel the name or label to search for
     * @return An optional containing Relationship Type that matches the provided name or label, or an empty optional
     */
    public static Optional<BrAPIRelationshipType> findByNameOrLabel(final String nameOrLabel) {
        return Stream.of(values())
            .filter(type -> type.name().equalsIgnoreCase(nameOrLabel) || type.getLabel().equalsIgnoreCase(nameOrLabel))
            .findAny();
    }
}
