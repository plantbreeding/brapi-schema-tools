package org.brapi.schematools.analyse.authorization;

import lombok.Builder;
import lombok.Value;
import org.brapi.schematools.core.response.Response;

import java.util.Base64;

/**
 * The Basic Authorisation
 */
@Value
@Builder
public class BasicAuthorizationProvider implements AuthorizationProvider {
    String username ;
    String password ;

    @Override
    public Response<String> getAuthorization() {
        if (username == null) {
            return Response.fail(Response.ErrorType.VALIDATION, "Username was not provided!") ;
        }

        if (password == null) {
            return Response.fail(Response.ErrorType.VALIDATION, "Password was not provided!") ;
        }

        String valueToEncode = username + ":" + password;
        return Response.success("Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes()));
    }
}
