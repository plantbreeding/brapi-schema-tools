# BrAPI Schema Tools - Java 

The Java BrAPI Schema Tools generates OpenAPI Specification or GraphQL Schema generation.
There is also a basic spring application that allows you to view the BrAPI Specification.

The Java BrAPI Schema Tools consists of 4 modules:

* [application](application/README.md) - A spring application that allow you to view the generated specification
* buildSrc - Defines the plugins use by the gradle
* [cli](cli/README.md) - The command line tool that makes use of the core module
* [core](core/README.md) - The core schema tools for OpenAPI Specification or GraphQL Schema generation

## Quick Start

1. Download the Command Line Interface (CLI) from the
   [Releases](https://github.com/plantbreeding/brapi-schema-tools/releases) page
2. Run the application on your terminal

    ```
    brapi
    ```

## For developers
For more control over the OpenAPI Specification or GraphQL Schema generation or to
contribute, please contact the developers by creating a
[GitHub issue](https://github.com/plantbreeding/brapi-schema-tools/issues) or look directly at the
code

### New Features

1. Create an issue from the Issues, describing the new feature
2. Create a branch from develop from that new issue
3. Make changes on the branch, add tests etc. Change the version in the [gradle.properties](gradle.properties) file. Make sure the version is a -SNAPSHOT version and is increased by at least a minor version from the current version in the develop branch
4. Create a Pull Request (PR) to merge into develop branch, ask for a review from another develop
5. On approval of PR merge the PR, this will automatically create a SNAPSHOT release that another can use.

### Releases

1. Create a branch from develop for the release. 
2. Update the version the version in the [gradle.properties](gradle.properties) file by removing the '-SNAPSHOT'.
3. Create a PR to merge into the main branch. 
4. Get approval for the PR and merge
5. This will automatically create the release.