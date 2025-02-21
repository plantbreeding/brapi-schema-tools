package org.brapi.schematools.cli;

import org.brapi.schematools.analyse.authorization.oauth.SingleSignOn;
import picocli.CommandLine;

import java.io.PrintStream;

/**
 * Login Command
 */
@CommandLine.Command(
        name = "login", mixinStandardHelpOptions = true,
        description = "Login to  of Single Single On to get access token"
)
public class LoginSubCommand implements Runnable {

  private final PrintStream out = System.out;
  private final PrintStream err = System.err;
  @CommandLine.Option(names = {"-u", "--username"}, description = "The username. If not provided the current system username is used.")
  private String username = System.getProperty("user.name");

  @CommandLine.Option(names = {"-p", "--password"}, interactive = true, arity = "0..1", description = "The password for the supplied username. Providing the option without a value make the application as for a value.")
  private String password;

  @CommandLine.Option(names = {"-c", "--client"}, required = true, description = "The client id for authentication")
  private String clientId;

  @Override
  public void run() {
    SingleSignOn.builder().clientId(clientId).username(username).build().login(password)
            .onSuccessDo(() -> out.println("Logged in!"))
            .onFailDoWithResponse(response -> err.printf("Login failed due to %s%n", response.getMessagesCombined(", ")));

  }
}