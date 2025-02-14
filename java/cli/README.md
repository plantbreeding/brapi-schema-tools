# BrAPI Schema Tools - Command Line Interface (CLI)

A Command line interface for running BrAPI Schema Tools on your computer.

Depends on only the [core](../core/README.md) module.

## Compiling and installing the CLI

Use Gradle to create and install the CLI in one command

```
./gradlew cli:installDist \path\to\installation\directory
```

Alternatively you can create a Zip or Tar achieve of the CLI including runtime libraries and OS specific scripts.

For a Zip file

```
./gradlew cli:distZip
```

For a Tar file

```
./gradle cli:distTar
```

This will create the zip or tar archive in the cli/build/distributions directory

## Using the executable JAR

To create an Executable JAR use the Gradle command:

```
./gradlew cli:jar
```

This will create the executable JAR in the cli/build/libs directory

## Using Docker

You can create a Docker version of the application, which might help you overcome some dependency issues.

You will need [Docker](rhttps://www.docker.com/) installed for this to work

```shell
cd cli
docker build .
```

## Using Gradle (nice for development)

When you have access to the source code repository and while developing new features this is
the quickest way to run the Command Line Interface. Use the Gradle command:

```
./gradlew cli:run  --args="-h"
```

Remember to replace the '-h' arguments with your required options. The '-h' option will give you a list of the available
options.
