# BrAPI Schema Tools - Spring Application

The module contains a deployable Spring application. However, its main use is to view the BrAPI Specification locally

You need to generate the OpenAPI specification and GraphQL schema and place these in the
[/src/main/resources/openapi/schema.graphqls] and /src/main/resources/openapi/brapi_openapi.json
files respectively. See the [CLI module](../cli/README.md) for details.

To run the application use the following commands

In windows

```powershell
./gradlew bootRun
```

In Linux or MacOS

```shell
./gradle bootRun
```

