package org.brapi.schematools.core.graphql.options;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.brapi.schematools.core.response.Response;

import java.util.Arrays;
import java.util.Optional;

import static org.brapi.schematools.core.response.Response.fail;

/**
 * Determines how a child property value is linked to the parent object
 */
@AllArgsConstructor
@Getter
public enum LinkType {

    /**
     * The json property value will be embedded in the parent object
     */
    EMBEDDED("embedded"),
    /**
     * The property value will be linked to the parent object via an ID, usually the DbId
     */
    ID("id"),
    /**
     * The property value will be exposed as a separate sub-query
     */
    SUB_QUERY("sub-query"),
    /**
     * The property value will be not be exposed in the parent object
     */
    NONE("none");

    private final String label ;

    /**
     * Find a LinkType by name or label
     * @param value the name or label of the LinkType
     * @return an Optional containing the LinkType that has the provided name or label or
     * an empty Optional if the name of label does not match any LinkType
     */
    public static Optional<LinkType> findByNameOrLabel(String value) {
        return Arrays.stream(LinkType.values())
            .filter(provider -> provider.name().equals(value) || provider.getLabel().equals(value))
            .findAny();
    }

    /**
     * Find a LinkType by name or label
     * @param value the name or label of the LinkType
     * @return a successful Response containing the LinkType that has the provided name or label or
     * a failed Response if the name of label does not match any LinkType
     */
    public static Response<LinkType> fromNameOrLabels(String value) {
        return findByNameOrLabel(value)
            .map(Response::success)
            .orElseGet(() -> fail(Response.ErrorType.VALIDATION, String.format("No LinkType found for value [%s]",value)));
    }
}
