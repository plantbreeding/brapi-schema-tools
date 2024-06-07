package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListQueryOptions {
    @JsonProperty("generate")
    boolean generating;
    String descriptionFormat;
    String inputName;
    String inputNameFormat;
    String inputTypeNameFormat;

    String responseTypeNameFormat;
    String dataFieldName;

    boolean pagedDefault;
    @Builder.Default
    Map<String, Boolean> paged = new HashMap<>();
    @Builder.Default
    Map<String, Boolean> input = new HashMap<>();
    @JsonProperty("generateFor")
    @Builder.Default
    Map<String, Boolean> generatingFor = new HashMap<>();

    String pagingInputName;
    String pageInputTypeName;
    String pageTypeName;
    String pageFieldName;

    @JsonIgnore
    public boolean isGeneratingFor(String name) {
        return generatingFor.getOrDefault(name, generating) ;
    }
}
