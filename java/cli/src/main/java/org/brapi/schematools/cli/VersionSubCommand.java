package org.brapi.schematools.cli;

import org.brapi.schematools.core.utils.Version;
import picocli.CommandLine;

@CommandLine.Command(
    name = "version", mixinStandardHelpOptions = true,
    description = "Returns the version of the tools"
)
public class VersionSubCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Version: " + Version.getVersion());
    }
}
