package org.brapi.schematools.cli;

import picocli.CommandLine;

/**
 * Main class for the BrAPI application
 */
@CommandLine.Command(
    name = "brapi",
    description = "Command line tools for the BrAPI JSON schema, see the sub-commands for details",
    version = "0.25.0",
    footer = "Copyright (c) 2024 The Breeding API",
    subcommands = {
        ValidateSubCommand.class,
        GenerateSubCommand.class,
        LoginSubCommand.class,
        LogoutSubCommand.class,
        AnalyseSubCommand.class,
        MarkdownSubCommand.class,
        CompareSubCommand.class
    },
    mixinStandardHelpOptions = true
)
public class BrAPICommand {

    /**
     * Main method for application
     * @param args arguments for application
     * @throws Exception if the command is not valid.
     */
    public static void main(String... args) throws Exception {
        System.exit(new CommandLine(new BrAPICommand()).execute(args));
    }
}