package org.brapi.schematools.cli;

import org.brapi.schematools.analyse.authorization.oauth.SingleSignOn;
import picocli.CommandLine;

import java.io.PrintStream;

/**
 * Logout Command
 */
@CommandLine.Command(
        name = "logout", mixinStandardHelpOptions = true,
        description = "Logout of Single Single On"
)
public class LogoutSubCommand implements Runnable {

  private final PrintStream out = System.out;
  private final PrintStream err = System.err;
  @CommandLine.Option(names = {"-u", "--username"}, description = "The username. If not provided the current system username is used.")
  private String username = System.getProperty("user.name");

  @CommandLine.Option(names = {"-c", "--client"}, required = true, description = "The client id for authentication")
  private String clientId;

  @Override
  public void run() {
    SingleSignOn.builder().clientId(clientId).username(username).build().logout()
            .onSuccessDo(() -> out.println("Logged out!"))
            .onFailDoWithResponse(response -> err.printf("Logged failed due to %s%n", response.getMessagesCombined(", ")));

  }
}