# Kafka Broker Setup

This is part of the [User Guide for `kafka-ldap-integration`](index.md).

## Download Kafka

Download Kafka from the [Apache Kafka Downloads](http://kafka.apache.org/downloads) page. There are multiple versions of Kafka available. You should download the version that matches the `kafka_version` and `scala_version` properties in [`build.gradle`](../build.gradle).

Extract the downloaded archive into a directory of your choice. For the remainder of this example, the directory containing the Kafka distribution will be referred to as `$KAFKA_HOME`. It's a good idea to set this as an environment variable.

## Build the Jar and Add to Classpath

From a terminal window or command prompt, run the following commands:

```shell script
# Clone the repository locally
git clone git@github.com:instaclustr/kafka-ldap-integration.git

# Change into the directory
cd kafka-ldap-integration

# Build the jar
./gradlew build shadowJar

# Copy the jar into the Kafka distribution libs folder
cp build/libs/*.jar $KAFKA_HOME/libs
```

## Configure the Broker to Use `kafka-ldap-integration`

Open `$KAFKA_HOME/config/server.properties` in your favourite editor, and add the following lines to the bottom:

```properties
# Configure inter-broker communication to use plaintext (Use SSL/TLS in Prod!)
listeners=SASL_PLAINTEXT://localhost:9092
security.inter.broker.protocol=SASL_PLAINTEXT

# Configure brokers to exchange plain text username/password.
sasl.mechanism.inter.broker.protocol=PLAIN
sasl.enabled.mechanisms=PLAIN

# Configure the authentication to use LDAP (verify that client is actually who they say they are)
listener.name.sasl_plaintext.plain.sasl.server.callback.handler.class=\
  com.instaclustr.kafka.ldap.authentication.SimpleLDAPAuthentication

# Configure the authorization to use LDAP (verify that client is allowed to perform a specific action)
authorizer.class.name=com.instaclustr.kafka.ldap.authorization.SimpleLDAPAuthorizer

# Configure super users
super.users=User:srvkafkabroker
```

The JAAS configuration needs to be set up in an external config file. Open `$KAFKA_HOME/config/jaas.conf` in your editor, and copy in the following:

```
KafkaServer {
    org.apache.kafka.common.security.plain.PlainLoginModule required
    username="srvkafkabroker"
    password="broker";
};
``` 

Modify `$KAFKA_HOME/bin/kafka-server-start.sh`, and insert the following line at the end of the file, just before the exec:

```shell script
export KAFKA_OPTS=$"-Djava.security.auth.login.config=$base_dir/../config/jaas.conf"
```

There needs to be a separate file containing the connection and configuration information for the LDAP authentication and authorization. This file must be available in the root of the classpath and called `ldapconfig.yaml`

Open `$KAFKA_HOME/config/ldapconfig.yaml` in your editor, and copy in the following:

```yaml
#host of the LDAP server
host: localhost
#port of the LDAP server
port: 10636
# connectionTimout in milliseconds for LDAP 
connTimeout: 10000
# Placement of users in LDAP tree
usrBaseDN: ou=users,dc=security,dc=example,dc=com
# User attribute for DN completion
usrUid: uid
# Placement of groups in LDAP tree
grpBaseDN: ou=groups,dc=security,dc=example,dc=com
# Group attribute for DN completion
grpUid: cn
# Group membership attribute name
grpAttrName: uniqueMember
# Lifetime of user entry in cache after cache-write - IN MINUTES
usrCacheExpire: 6
# Lifetime of group entry in cache after cache-write - IN MINUTES
grpCacheExpire: 6
```

## Next Steps

With the above configuration, you won't be able to start the broker until the LDAP Directory Server is up and running, with the correct configuration. Instructions on how to do this can be found here:
* [LDAP Server Setup](ldap.md)
