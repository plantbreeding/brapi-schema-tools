package org.brapi.schematools.core.graphql.options;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides options for the generation of Update Mutations
 */
@Getter
@Setter(AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class UpdateMutationOptions {
    @JsonProperty("generate")
    boolean generating;
    String nameFormat;
    String descriptionFormat;
    @JsonProperty("generateFor")
    @Builder.Default
    Map<String, Boolean> generatingFor = new HashMap<>();
    boolean multiple;

    /**
     * Determines if the Update Mutation is generated for a specific primary model
     * @param name the name of the primary model
     * @return <code>true</code> if the Update Mutation is generated for a specific primary model, <code>false</code> otherwise
     */
    @JsonIgnore
    public boolean isGeneratingFor(String name) {
        return generatingFor.getOrDefault(name, generating) ;
    }

    /**
     * Gets the name for the Mutation for a specific primary model
     * @param name the name of the primary model
     * @return the name for the Mutation for a specific primary model
     */
    public String getMutationNameFor(String name) {
        return String.format(nameFormat, name) ;
    }
}
