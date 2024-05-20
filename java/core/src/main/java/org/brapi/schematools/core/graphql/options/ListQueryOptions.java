package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

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
    Map<String, Boolean> paged;

    String pagingInputName;
    String pageInputTypeName;

    String pageTypeName;
    String pageFieldName;
}
