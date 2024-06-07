package org.brapi.schematools.cli;

import picocli.CommandLine;

@CommandLine.Command(
    name = "brapi",
    subcommands = {
        GenerateSubCommand.class
    },
    version = "1.0",
    mixinStandardHelpOptions = true
)
public class BrAPICommand {
    public static void main(String[] args) {
        new CommandLine(new BrAPICommand()).execute(args);
    }
}