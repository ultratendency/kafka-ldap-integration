# User Guide for `kafka-ldap-integration`

This user guide covers the following:
1. How to enable authentication (`SASL_PLAINTEXT`) and authorization for [Apache Kafka](https://kafka.apache.org).
1. Code documentation outlining LDAP bind (authentication verification) and LDAP group membership (authorization) in the Kafka context.
1. Use of Apache Directory Studio, LDAP server up and running in a few minutes

Following these instructions will take approximately 30 minutes. At the end, you should have a working Kafka environment locally.

Two important observations:
* Never use `SASL_PLAINTEXT` in production systems. TLS configuration is an independent activity beyond this context. Just substitute `SASL_PLAINTEXT` with `SASL_SSL` when activating TLS.
* This text is a minimalistic "recipe", please read more on relevant sites for enlightenment.

## Table of Contents

* [Kafka Broker Setup](kafka.md)
* [LDAP Server Setup](ldap.md)
* [Test Drive Kafka LDAP Integration](test.md)
