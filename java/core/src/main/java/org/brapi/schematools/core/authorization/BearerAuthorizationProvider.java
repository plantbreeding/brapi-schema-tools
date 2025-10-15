package org.brapi.schematools.core.authorization;

import lombok.Builder;
import lombok.Value;
import org.brapi.schematools.core.response.Response;

import java.util.Base64;

/**
 * The Bearer Authorisation
 */
@Value
@Builder
public class BearerAuthorizationProvider implements AuthorizationProvider {
    String token ;

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public Response<String> getAuthorization() {
        if (token == null) {
            return Response.fail(Response.ErrorType.VALIDATION, "Token was not provided!") ;
        }

        return Response.success("Bearer " + token);
    }
}
