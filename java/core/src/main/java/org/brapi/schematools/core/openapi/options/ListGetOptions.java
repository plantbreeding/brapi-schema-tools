package org.brapi.schematools.core.openapi.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of List Get Endpoints
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ListGetOptions {
    @JsonProperty("generate")
    boolean generating;
    String summaryFormat;
    String descriptionFormat;
    boolean pagedDefault;
    @Builder.Default
    Map<String, Boolean> paged = new HashMap<>();
    @JsonProperty("generateFor")
    @Builder.Default
    Map<String, Boolean> generatingFor = new HashMap<>();

    /**
     * Determines if the List Get Endpoint is generated for a specific primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the List Get Endpoint is generated for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingFor(String name) {
        return generatingFor.getOrDefault(name, generating) ;
    }
}
