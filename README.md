# kafka-ldap-integration 

[![Gradle build](https://github.com/ultratendency/kafka-ldap-integration/actions/workflows/gradle.yml/badge.svg)](https://github.com/ultratendency/kafka-ldap-integration/actions/workflows/gradle.yml)

Enhancing kafka 2.x with
- customized SimpleLDAPAuthentication using LDAPS simple bind for authentication
- customized SimpleACLAuthorizer using LDAPS compare-matched for group membership verification

Thus, moving authentication from user and passwords in JAAS context file on kafka brokers to LDAP server

By defining Read/Write allowance with LDAP groups, authorization is moved from 
Zookeeper Access Control Lists to group membership verification.

Binding and group membership information is cached (limited lifetime after write),
giving minor performance penalty and reduced LDAPS traffic.

## Tools
- Kotlin
- Gradle build tool
- Spek test framework

## Components

1. Unboundid LDAP SDK for LDAPS interaction
2. Caffeine Cache
3. YAML Configuration for LDAP baseDN for users, groups and more. See `src/test/resources/ldapconfig.yaml` for details

**N.B.** that the directory hosting yaml configuration file must be in CLASSPATH.

## Quickstart

A tutorial with configuration examples is available in the [User Guide](docs/index.md).

## Build 

```
./gradlew clean build shadowJar
```

#### Build without unit tests

```
./gradlew clean build shadowJar -x test
```

The result is `build/libs/kafka-ldap-integration-<version>.jar`, which contains the authentication and authorization classes, along with all of their dependencies.

**N.B.** This jar must be added to the classpath for the Kafka broker. The easist way to do that is to copy the jar into the directory `$KAFKA_HOME/libs`.

### Contact us
#### Code/project related questions can be sent to 
* Joe Schmetzer, `joe.schmetzer@instaclustr.com `
* Zeke Dean, `zeke.dean@instaclustr.com`

#### Credit to original authors

Maintainers of the GitHub project [kafka-plain-saslserver-2-ad](https://github.com/navikt/kafka-plain-saslserver-2-ad):
* Torstein Nesby, `torstein.nesby@nav.no`
* Trong Huu Nguyen, `trong.huu.nguyen@nav.no`
