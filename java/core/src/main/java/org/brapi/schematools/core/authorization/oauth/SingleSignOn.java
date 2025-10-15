package org.brapi.schematools.core.authorization.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.brapi.schematools.core.authorization.AuthorizationProvider;
import org.brapi.schematools.core.response.Response;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;

import static java.net.http.HttpClient.newHttpClient;

/**
 * Handles OAuth login and logout.
 */
@Getter
@Slf4j
public class SingleSignOn implements AuthorizationProvider {

    @NonNull
    private final String name;
    @NonNull
    private final String url;
    private final String clientId;
    private final String username;

    @Getter(AccessLevel.PRIVATE)
    private final URI requestUri;
    @Getter(AccessLevel.PRIVATE)
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Builder
    private SingleSignOn(String name, @NonNull final String url, String clientId, String username) {
        this.name = name != null ? name : URI.create(url).getHost();
        this.url = url;
        this.clientId = clientId;
        this.username = username != null ? username : System.getProperty("user.name");

        requestUri = URI.create(url);
    }

    @Override
    public boolean required() {
        return true;
    }

    @Override
    public Response<String> getAuthorization() {
        return getToken()
            .mapResult(openIDToken -> "Bearer " + openIDToken.getAccessToken());
    }

    /**
     * Login the current user and password
     *
     * @param password the current user's password
     * @return A response containing a valid token or failure explaining why login has failed.
     */
    public Response<OpenIDToken> loginWithPassword(String password) {

        log.debug("Attempting to login with password.");

        if (username == null) {
            return Response.fail(Response.ErrorType.PERMISSION, "Username not provided!");
        }

        if (password == null) {
            return Response.fail(Response.ErrorType.PERMISSION, "Password not provided!");
        }

        return requestToken(TokenRequest.builder()
            .grantType("password")
            .username(username)
            .password(password)
            .clientId(clientId)
            .build())
            .onSuccessDo(() -> log.debug("Logged in!"))
            .onFailDo(() -> log.debug("Log failed!"));
    }

    /**
     * Login with clientId
     *
     * @param clientSecret the clientSecret for the provided clientId
     * @return A response containing a valid token or failure explaining why login has failed.
     */
    public Response<OpenIDToken> loginWithClientId(String clientSecret) {

        log.debug("Attempting to login with client secret");

        if (clientId == null) {
            return Response.fail(Response.ErrorType.PERMISSION, "Client ID not provided!");
        }

        if (clientSecret == null) {
            return Response.fail(Response.ErrorType.PERMISSION, "Client secret not provided!");
        }

        return requestToken(TokenRequest.builder()
            .grantType("client_credentials")
            .clientId(clientId)
            .clientSecret(clientSecret)
            .build())
            .onSuccessDo(() -> log.debug("Logged in!"))
            .onFailDo(() -> log.debug("Log failed!"));
    }

    /**
     * Logout the current user
     *
     * @return An empty response or failure explaining why logout has failed.
     */
    public Response<Void> logout() {

        File tokenFile = getTokenFile();

        if (!tokenFile.exists()) {
            return Response.fail(Response.ErrorType.VALIDATION, "You were not logged in, so can not logout!");
        }

        if (tokenFile.delete()) {
            log.debug("Logged out!");
            return Response.success(null);
        } else {
            return Response.fail(Response.ErrorType.VALIDATION, String.format("Was not able to logout, try to delete the sso token at %s manually!", tokenFile.getAbsolutePath()));
        }
    }

    /**
     * Gets the current token if not expired or fetches a new one, or fails with a message explaining the reason why a
     * valid token can not be obtained.
     *
     * @return A response containing a valid token or failure.
     */
    public Response<OpenIDToken> getToken() {
        try {

            File tokenFile = getTokenFile();

            long lastModified = tokenFile.lastModified();

            OpenIDToken token = objectMapper.readValue(getTokenFile(), OpenIDToken.class);

            long currentTimeMillis = System.currentTimeMillis();

            if (currentTimeMillis > lastModified + (token.getExpiresIn() * 1000L)) {
                if (currentTimeMillis > lastModified + (token.getRefreshExpiresIn() * 1000L)) {
                    log.debug("Refresh token expired");
                    return Response.fail(Response.ErrorType.VALIDATION, "Token expired, please login first");
                } else {
                    log.debug("Token expired will try to refresh");
                    return requestToken(TokenRequest.builder()
                        .grantType("refresh_token")
                        .refreshToken(token.getRefresh_token())
                        .build());
                }
            }

            return Response.success(token);
        } catch (IOException e) {
            return Response.fail(Response.ErrorType.VALIDATION, "Not logged in, please login first");
        }
    }

    private File getTokenFile() {
        Path parentPath = Path.of(System.getProperty("user.home"), ".sso", name, clientId != null ? clientId : username);

        if (parentPath.toFile().mkdirs()) {
            log.debug("Created directory at {}", parentPath);
        } else {
            log.debug("Used existing directory at {}", parentPath);
        }

        return parentPath.resolve(String.format("%s.token", username)).toFile();
    }

    private Response<OpenIDToken> requestToken(TokenRequest tokenRequest) {
        try {
            HttpRequest request = HttpRequest.newBuilder(requestUri)
                .headers("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequest.params())).build();

            HttpResponse<String> response = newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                OpenIDToken token = objectMapper.readValue(response.body(), OpenIDToken.class);

                objectMapper.writeValue(getTokenFile(), token);

                return Response.success(token);
            } else {

                JsonNode tree = objectMapper.readTree(response.body());

                if (tree.has("error")) {
                    if (tree.has("error_description")) {
                        return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not login due to %s %s!", tree.get("error").asText(), tree.get("error_description").asText()));
                    }

                    return Response.fail(Response.ErrorType.VALIDATION, String.format("Can not login due to %s!", tree.get("error").asText()));
                }

                return Response.fail(Response.ErrorType.VALIDATION, "Can not login!");
            }

        } catch (IOException | InterruptedException e) {
            return Response.fail(Response.ErrorType.VALIDATION, e.getMessage());
        }
    }
}
