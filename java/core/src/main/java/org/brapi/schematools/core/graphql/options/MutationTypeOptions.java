package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.options.Options;
import org.brapi.schematools.core.validiation.Validation;

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

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(MutationTypeOptions overrideOptions) {
        if (overrideOptions.name != null) {
            setName(overrideOptions.name); ;
        }

        if (overrideOptions.createMutation != null) {
            createMutation.override(overrideOptions.createMutation) ;
        }

        if (overrideOptions.updateMutation != null) {
            updateMutation.override(overrideOptions.updateMutation) ;
        }

        if (overrideOptions.deleteMutation != null) {
            deleteMutation.override(overrideOptions.deleteMutation) ;
        }
    }
}
