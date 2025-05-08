# ccd-case-document-am-client

This is a client library for interacting with the ccd-case-document-am-api application. The two main responsibilities are:
 - upload the case document
 - download the case document
 - update the case document
 - delete the case document
 - patch the case document

## Getting started

### Prerequisites

- [JDK 17](https://www.oracle.com/java)

## Usage

This library is hosted on Azure DevOps Artifacts and can be used in your project by adding the following to your `build.gradle` file:

```gradle
repositories {
  maven {
    url = uri('https://pkgs.dev.azure.com/hmcts/Artifacts/_packaging/hmcts-lib/maven/v1')
  }
}
dependencies {
  implementation 'com.github.hmcts:fortify-client:LATEST_TAG'
}
```

This library also includes a health check for case-document-am-api is provided as well.

Components provided by this library will get automatically configured in a Spring context if `ccd_case_document_am_api.url` configuration property is defined and does not equal `false`.

## Configurable Health Check

To enable/disable the Health Check endpoint made available by this client, you need to set the following property in your `application.properties` or `application.yaml` Spring property file:
```
management.health.case-document-am-api.enabled=false
```
By setting the value to false, you are disabling the Health Check endpoint exposed by the client.

To enable the endpoint, simply set the value to true:
```
management.health.case-document-am-api.enabled=true
```

## Building

The project uses [Gradle](https://gradle.org) as a build tool. However, you don't have to install it locally since there is a
`./gradlew` wrapper script.

To build this project, please run the following command:

```bash
    ./gradlew build
```

## Developing

### Coding style tests

To run all checks (including unit tests) please execute the following command:

```bash
    ./gradlew check
```

## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

To release a new version add a tag with the version number and push this up to the origin repository. This will then
build and publish the release to maven.

## License

This project is licensed under the MIT License—see the [LICENSE](LICENSE.md) file for details.
