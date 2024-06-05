package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

import static org.brapi.schematools.core.utils.StringUtils.toParameterCase;

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

    @JsonIgnore
    public String getIDParameterFor(String name) {
        return parameterFor.getOrDefault(name, String.format(nameFormat, toParameterCase(name))) ;
    }
}
