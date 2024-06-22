package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.brapi.schematools.core.model.BrAPIType;
import org.brapi.schematools.core.utils.StringUtils;

/**
 * Provides options for the generation of Ids
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdsOptions {
    @Getter(AccessLevel.PRIVATE)
    String nameFormat;
    @JsonProperty("useIDType")
    boolean usingIDType;

    public void validate() {
        assert nameFormat != null : "'nameFormat' option on Mutation Ids Options is null";
    }

    /**
     * Gets the name of the id for a specific primary model
     * @param name the name of the primary model
     * @return the name of the id for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull String name) {
        return String.format(nameFormat, StringUtils.toParameterCase(name)) ;
    }

    /**
     * Gets the name of id for a specific primary model
     * @param type the primary model
     * @return the name of the id for a specific primary model
     */
    @JsonIgnore
    public final String getNameFor(@NonNull BrAPIType type) {
        return getNameFor(type.getName());
    }
}
