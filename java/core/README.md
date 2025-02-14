# BrAPI Schema Tools - Core Classes

Core Java classes for parsing the BrAPI Schema

Use gradle to build, test and publish

To make a clean build use

```
./gradlew clean build
```

To make a clean build and test use

```
./gradlew clean test
```

To make a clean build and publish locally use

```
./gradlew clean publishToMavenLcoal
```

To make a clean build and release, you will need some secrets.
It is not recommend to this from here,
let the CI on GitHub handle this.

```
./gradlew clean test publish
```
