# Test Drive Kafka LDAP Integration

This is part of the [User Guide for `kafka-ldap-integration`](index.md).

## Prerequisites

This guide assumes that you have already:
* Completed the [Kafka Broker Setup](kafka.md)
* Completed the [LDAP Server Setup](ldap.md)


## Start the Kafka Broker

Zookeeper needs to be started first. From a terminal window or command prompt, run the following commands:

```shell script
cd $KAFKA_HOME
./bin/zookeeper-server-start.sh config/zookeeper.properties
```

Once Zookeeper is up and running, you can start the Kafka broker from a new terminal window:

```shell script
cd $KAFKA_HOME
export CLASSPATH=$KAFKA_HOME/config # ldapconfig.yaml must be classpath
./bin/kafka-server-start.sh config/server.properties
```

Check the log messages in the terminal window. If everything is set up and configured correctly, there should be no error messages about authentication.

## Set up a Test Topic with Permissions

Start a new terminal window, and run the following commands:
```shell script
cd $KAFKA_HOME

# Create a test topic
./bin/kafka-topics.sh --create \
  --zookeeper localhost:2181 \
  --replication-factor 1 \
  --partitions 1 \
  --topic test

# Define producer access, all users in LDAP group ktprodTest
./bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --topic test \
  --allow-principal User:ktprodTest \
  --producer

# Define consumer access, all users in LDAP group ktconsTest
./bin/kafka-acls.sh --authorizer-properties zookeeper.connect=localhost:2181 \
  --add --topic test \
  --allow-principal User:ktconsTest \
  --consumer  \
  --group *
```

## Start the Consumer

Create a new file called `consumer.properites` in `$KAFKA_HOME/config`:

```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
    username="srvkafkaconsumer" \
    password="consumer";

# consumer group id
group.id=test-srvkafkaconsumer-grp
client.id=srvkafkaconsumer

# list of brokers used for bootstrapping knowledge about the rest of the cluster
# format: host1:port1,host2:port2 ...
bootstrap.servers=localhost:9092
```

From the `$KAFKA_HOME` directory, run the command:

```shell script
./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 \
  --consumer.config ./config/consumer.properties \
  --topic test
```

If configured correctly, you should see no errors in the terminal window. The console consumer will wait for messages to appear in the `test` topic, and print them as they are received.

## Start the Producer

Create a new file called `producer.properites` in `$KAFKA_HOME/config`:

```properties
security.protocol=SASL_PLAINTEXT
sasl.mechanism=PLAIN
sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required \
    username="srvkafkaproducer3" \
    password="producer3";

# list of brokers used for bootstrapping knowledge about the rest of the cluster
# format: host1:port1,host2:port2 ...
bootstrap.servers=localhost:9092

# specify the compression codec for all data generated: none, gzip, snappy, lz4
compression.type=none
```

From the `$KAFKA_HOME` directory, run the command:

```shell script
./bin/kafka-console-producer.sh --broker-list localhost:9092 \
  --producer.config ./config/producer.properties \
  --topic test 
```

If configured correctly, you should see no errors in the terminal window. The console producer lets you type text into the terminal window. When you press `<return>`, the text will be sent to the broker, and you should see it appear in the terminal of console consumer. 

This concludes the test!