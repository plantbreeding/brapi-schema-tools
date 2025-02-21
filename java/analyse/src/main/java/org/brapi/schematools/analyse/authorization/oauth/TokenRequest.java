package org.brapi.schematools.analyse.authorization.oauth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * The request object used for OAuth
 */
@Value
@Builder
public class TokenRequest {
    @JsonProperty("grant_type")
    String grantType;
    @JsonProperty("client_id")
    String clientId;
    @JsonProperty("client_secret")
    String clientSecret;
    @JsonProperty("username")
    String username;
    @JsonProperty("password")
    String password;
    @JsonProperty("refresh_token")
    String refreshToken;

    /**
     * Get the parameters for the request
     * @return the parameters for the request
     */
    public String params() {

        String params = "grant_type=" + grantType ;

        if (clientId != null) {
            params = params + "&client_id=" + clientId;
        }

        if (clientSecret != null) {
            params = params + "&client_secret=" + clientSecret;
        }

        if (refreshToken != null) {
            params = params + "&refresh_token=" + refreshToken;
        } else {
            params = params +
                "&username=" +
                username +
                "&password=" +
                password;
        }

        return params;
    }
}
