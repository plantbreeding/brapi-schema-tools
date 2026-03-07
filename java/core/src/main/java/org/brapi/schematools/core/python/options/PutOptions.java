package org.brapi.schematools.core.python.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.brapi.schematools.core.model.BrAPIType;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of PUT (update) methods.
 */
@Getter(AccessLevel.PRIVATE)
@Setter
public class PutOptions extends AbstractPythonGeneratorSubOptions {

    private Boolean multiple;
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.PRIVATE)
    private Map<String, Boolean> multipleFor = new HashMap<>();

    /**
     * Overrides the values in this Options Object from the provided Options Object if they are non-null.
     *
     * @param overrideOptions the options which will be used to override this Options Object
     */
    public void override(PutOptions overrideOptions) {
        super.override(overrideOptions);

        if (overrideOptions.multiple != null) {
            setMultiple(overrideOptions.multiple);
        }

        if (overrideOptions.multipleFor != null) {
            multipleFor.putAll(overrideOptions.multipleFor);
        }
    }

    /**
     * Determines if the PUT method accepts multiple entities for the named model.
     *
     * @param name the name of the primary model
     * @return {@code true} if the PUT method accepts multiple entities, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull String name) {
        return multipleFor.getOrDefault(name, multiple);
    }

    /**
     * Determines if the PUT method accepts multiple entities for the given model.
     *
     * @param type the primary model
     * @return {@code true} if the PUT method accepts multiple entities, {@code false} otherwise
     */
    @JsonIgnore
    public final boolean isMultipleFor(@NonNull BrAPIType type) {
        return isMultipleFor(type.getName());
    }
}
