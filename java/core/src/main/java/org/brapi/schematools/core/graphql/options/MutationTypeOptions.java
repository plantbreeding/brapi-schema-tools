package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.valdiation.Validation;

/**
 * Provides options for the generation of the Mutation Type
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MutationTypeOptions implements Options {
    private String name;
    @Setter(AccessLevel.PRIVATE)
    private CreateMutationOptions createMutation;
    @Setter(AccessLevel.PRIVATE)
    private UpdateMutationOptions updateMutation;
    @Setter(AccessLevel.PRIVATE)
    private DeleteMutationOptions deleteMutation;

    public Validation validate() {
        return Validation.valid()
            .assertNotNull(name, "Name option on Mutation Type Options is null")
            .assertNotNull(createMutation, "Create Mutation Options are null")
            .assertNotNull(updateMutation, "Update Mutation Options are null")
            .assertNotNull(deleteMutation, "Delete Mutation Options are null")
            .merge(createMutation)
            .merge(updateMutation)
            .merge(deleteMutation) ;
    }
}
