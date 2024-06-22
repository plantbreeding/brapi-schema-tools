package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides options for the generation of the Mutation Type
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MutationTypeOptions {
    private String name;
    @Setter(AccessLevel.PRIVATE)
    private CreateMutationOptions createMutation;
    @Setter(AccessLevel.PRIVATE)
    private UpdateMutationOptions updateMutation;
    @Setter(AccessLevel.PRIVATE)
    private DeleteMutationOptions deleteMutation;

    public void validate() {
        assert name != null : "Name option on Mutation Type Options is null";

        assert createMutation != null : "Create Mutation Options are null";
        assert updateMutation != null : "Update Mutation Options are null";
        assert deleteMutation != null : "Delete Mutation Options are null";

        createMutation.validate() ;
        updateMutation.validate() ;
        deleteMutation.validate() ;
    }
}
