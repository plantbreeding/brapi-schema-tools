package org.brapi.schematools.core.graphql.options;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.brapi.schematools.core.valdiation.Validation;

/**
 * Provides options for the generation of Delete Mutations
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DeleteMutationOptions extends AbstractGraphQLOptions {
    private Boolean multiple;

    public Validation validate() {
        return super.validate()
            .assertNotNull(multiple, "'multiple' option on %s is null", this.getClass().getSimpleName()) ;
    }

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(DeleteMutationOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.multiple != null) {
            setMultiple(overrideOptions.multiple);
        }
    }

    /**
     * Determines if the mutation accepts multiple object or just one
     * @return <code>true</code> if the mutation accepts multiple object, <code>false </code> is the mutation accepts just one object
     */
    public boolean isMultiple() {
        return multiple != null && multiple ;
    }
}
