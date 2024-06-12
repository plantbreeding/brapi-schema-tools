package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

/**
 * Provides options for the generation of Ids
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class IdsOptions {
    String nameFormat;

    @JsonProperty("generateFor")
    @Builder.Default
    Map<String, String> parameterFor = new HashMap<>();

    /**
     * Gets the id parameter name for a specific primary model. For example the id parameter (or field)
     * name of Study, would be 'studyDbiId' by default. Use {@link #parameterFor} to override this value.
     * @param name the name of the primary model
     * @return id parameter name for a specific primary model
     */
    @JsonIgnore
    public String getIDParameterFor(String name) {
        return parameterFor.getOrDefault(name, String.format(nameFormat, toParameterCase(name))) ;
    }
}
