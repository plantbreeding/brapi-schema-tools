package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PutOptions {
    @JsonProperty("generate")
    boolean generating;
    String name;
    String descriptionFormat;
    @JsonProperty("generateFor")
    @Builder.Default
    Map<String, Boolean> generatingFor = new HashMap<>();
}
