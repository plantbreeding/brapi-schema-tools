package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Provides options for the generation of New Mutations
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CreateMutationOptions extends AbstractGraphQLOptions {
    private Boolean multiple;

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(CreateMutationOptions overrideOptions) {
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