package org.brapi.schematools.cli;

import picocli.CommandLine;

/**
 * Main class for the BrAPI application
 */
@CommandLine.Command(
    name = "brapi",
    description = "Command line tools for the BrAPI JSON schema, see the sub-commands for details",
    version = "0.0.2",
    footer = "Copyright (c) 2024 The Breeding API",
    subcommands = {
        ValidateSubCommand.class,
        GenerateSubCommand.class
    },
    mixinStandardHelpOptions = true
)
public class BrAPICommand {

    /**
     * Main method for application
     * @param args arguments for application
     */
    public static void main(String[] args) {
        new CommandLine(new BrAPICommand()).execute(args);
    }
}