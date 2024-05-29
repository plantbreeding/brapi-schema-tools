# BrAPI Schema Tools - Core Classes

Core Java classes for parsing the BrAPI Schema

Use gradle to build, test and publish

## In windows

To make a clean build use

```powershell
./gradlew clean build
```

To make a clean build and test use

```powershell
./gradlew clean test
```

To make a clean build and publish locally use

```powershell
./gradlew clean publishToMavenLcoal
```

To make a clean build and release, you will need some secrets.
It is not recommend to this from here,
let the CI on GitHub handle this.

```powershell
./gradlew clean test publish
```

## In Linux or MacOS

To make a clean build use

```shell
./gradlew clean build
```

To make a clean build and test use

```shell
./gradlew clean test
```

To make a clean build and publish locally use

```shell
./gradlew clean publishToMavenLcoal
```

To make a clean build and release, you will need some secrets.
It is not recommend to this from here,
let the CI on GitHub handle this.

```shell
./gradlew clean test publish
```
