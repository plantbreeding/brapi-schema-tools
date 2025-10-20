package org.brapi.schematools.cli;

import org.brapi.schematools.core.authorization.oauth.SingleSignOn;
import picocli.CommandLine;

import java.io.PrintStream;

/**
 * Login Command
 */
@CommandLine.Command(
    name = "loginWithPassword", mixinStandardHelpOptions = true,
    description = "Login using 'Single Sign On (SSO)' to get access token"
)
public class LoginSubCommand implements Runnable {

    private final PrintStream out = System.out;
    private final PrintStream err = System.err;

    @CommandLine.Option(names = {"-a", "--oauth"}, description = "The URL of the OAuth access token if used")
    private String oauthURL;
    @CommandLine.Option(names = {"-u", "--username"}, description = "The username for authentication if required. If not provided the current system username is used.")
    private String username = System.getProperty("user.name");
    @CommandLine.Option(names = {"-p", "--password"}, interactive = true, arity = "0..1", description = "The password for the supplied username. Will fail if not logged in and the password is not provided. Providing the option without a value make the application as for a value.")
    private String password;
    @CommandLine.Option(names = {"-c", "--clientId"}, description = "The client id for authentication if required.")
    private String clientId;
    @CommandLine.Option(names = {"-s", "--clientSecret"}, description = "The client secret for authentication if required.")
    private String clientSecret;
    @CommandLine.Option(names = {"-n", "--name"}, description = "The name of the SSO which is used as a sub-directory to store the token. If not provided the host URL is used.")
    private String name ;

    @Override
    public void run() {
        if (password != null && clientSecret != null) {
            SingleSignOn.builder().name(name).url(oauthURL).username(username).clientId(clientId).build().loginWithPasswordAndClientId(password, clientSecret)
                .onSuccessDo(() -> out.println("Logged in!"))
                .onFailDoWithResponse(response -> err.printf("Login failed due to %s%n", response.getMessagesCombined(", ")));
        } else if (password != null) {
            SingleSignOn.builder().name(name).url(oauthURL).username(username).build().loginWithPassword(password)
                .onSuccessDo(() -> out.println("Logged in!"))
                .onFailDoWithResponse(response -> err.printf("Login failed due to %s%n", response.getMessagesCombined(", ")));
        } else if (clientSecret != null) {
            SingleSignOn.builder().name(name).url(oauthURL).clientId(clientId).build().loginWithClientId(clientSecret)
                .onSuccessDo(() -> out.println("Logged in!"))
                .onFailDoWithResponse(response -> err.printf("Login failed due to %s%n", response.getMessagesCombined(", ")));
        } else {
            err.printf(String.format("Not logged please provide password using option '-p' for user '%s' or client secret for client '%s'", username, clientId));
        }

    }
}