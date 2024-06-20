package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * Provides options for the generation of the Mutation Type
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MutationTypeOptions {
    @JsonProperty("generate")
    boolean generating;
    String name;
    CreateMutationOptions createMutation;
    UpdateMutationOptions updateMutation;
    DeleteMutationOptions deleteMutation;
}
