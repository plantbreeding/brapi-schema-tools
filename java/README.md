# BrAPI Schema Tools - Java 

The Java BrAPI Schema Tools generates OpenAPI Specification or GraphQL Schema generation.
There is also a basic spring application that allow you to view the BrAPI Specification.

## Quick Start

1. Download the Command Line Interface (CLI) from the
   [Releases](https://github.com/plantbreeding/brapi-schema-tools/releases) page
2. Run the application on your terminal
    * In windows

    ```powershell
    brapi
    ```

    * In Linux or macOS

    ```shell
    brapi 
    ```

## For developers
For more control over the OpenAPI Specification or GraphQL Schema generation or to
contribute, please contact the developers by creating a
[GitHub issue](https://github.com/plantbreeding/brapi-schema-tools/issues) or look directly at the
code

The Java BrAPI Schema Tools consists of 4 modules:

* [application](application/README.md) - A spring application that allow you to view the generated specification
* buildSrc - Defines the plugins use by the gradle
* [cli](cli/README.md) - The command line tool that makes use of the core module
* [core](core/README.md) - The core schema tools for OpenAPI Specification or GraphQL Schema generation
