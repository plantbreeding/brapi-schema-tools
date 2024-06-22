# brapi-schema-tools
Tools used to manipulate, extend, convert, and modify the BrAPI Specification documents.

Only the Java based tools are available at the moment. 
These consist of a Command Line Interface (CLI) that can generate OpenAPI Specification or 
GraphQL Schema from the BrAPI JSON Schema. For more details on the JSON Schema please see below

https://github.com/plantbreeding/BrAPI

## Quick Start

1. Download the Command Line Interface (CLI) from the 
[Releases](https://github.com/plantbreeding/brapi-schema-tools/releases) page
2. Run the application on your terminal
   * In windows

    ```powershell
    BrAPITools
    ```

    * In Linux or macOS

    ```shell
    BrAPITools 
    ```

## BrAPI JSON Schema
The BrAPI JSON Schema is a new way to define the Data and Query model for BrAPI using JSON Schema.

The approach is experimental, but it is likely to be supported in a future release of BrAPI.
PLease go to the 'data-model-separation' branch of BrAPI to see the JSON Schema.
* https://github.com/plantbreeding/BrAPI/tree/data-model-separation/Specification/BrAPI-Schema

## For developers
For more control over the OpenAPI Specification or GraphQL Schema generation or to
contribute, please contact the developers by creating a 
[GitHub issue](https://github.com/plantbreeding/brapi-schema-tools/issues) or look directly at the
code 
* [Java Code base](java/README.md) 