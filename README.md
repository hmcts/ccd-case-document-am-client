# ccd-case-document-am-client

[![API v1](https://img.shields.io/badge/API%20Docs-v1-e140ad.svg)](https://hmcts.github.io/reform-api-docs/swagger.html?url=https://hmcts.github.io/reform-api-docs/specs/document-management-store-app.json)
[![Build Status](https://travis-ci.com/hmcts/ccd-case-document-am-client.svg?branch=master)](https://travis-ci.com/hmcts/ccd-case-document-am-client)
[![Download](https://api.bintray.com/packages/hmcts/hmcts-maven/ccd-case-document-am-client/images/download.svg) ](https://bintray.com/hmcts/hmcts-maven/ccd-case-document-am-client/_latestVersion)

This is a client library for interacting with the ccd-case-document-am-api application. The two main responsibilities are:
 - upload the case document
 - download the case document
 - update the case document
 - delete the case document

The API Documentation provided at the top of this README point to the Swagger documentation for the Case Document API.
## Getting started

### Prerequisites

- [JDK 8](https://www.oracle.com/java)

## Usage

Just include the library as your dependency and you will be ready to use the client class. Health check for case-document-am-api is provided as well.

Components provided by this library will get automatically configured in a Spring context if `ccd_case_ddocument_am_api.url` configuration property is defined and does not equal `false`.

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

The project uses [Gradle](https://gradle.org) as a build tool but you don't have install it locally since there is a
`./gradlew` wrapper script.

To build project please execute the following command:

```bash
    ./gradlew build
```

## Developing

### Coding style tests

To run all checks (including unit tests) please execute the following command:

```bash
    ./gradlew check
```

## Functional Tests

The functional tests rely on CCD, document-store and Idam and need to be configured with appropriate user roles and events.
Before running the functional test on a local environment run the following:


## Versioning

We use [SemVer](http://semver.org/) for versioning.
For the versions available, see the tags on this repository.

To release a new version add a tag with the version number and push this up to the origin repository. This will then
build and publish the release to maven.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE.md) file for details.
