package org.brapi.schematools.core.authorization.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Encapsulates all the information returned by the OAuth server
 */
@Getter
@EqualsAndHashCode
public class OpenIDToken {

    @JsonProperty("access_token")
    String accessToken;
    @JsonProperty("expires_in")
    int expiresIn;
    @JsonProperty("refresh_expires_in")
    int refreshExpiresIn;
    @JsonProperty("refresh_token")
    String refresh_token;
    @JsonProperty("token_type")
    String token_type;
    @JsonProperty("not-before-policy")
    int notBeforePolicy;
    @JsonProperty("session_state")
    String session_state;
    @JsonProperty("scope")
    String scope;
}
