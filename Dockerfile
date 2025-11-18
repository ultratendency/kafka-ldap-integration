FROM eclipse-temurin:24.0.2_12-jdk-alpine AS build

WORKDIR /kafka-plain-saslserver-2-ad

COPY . .

RUN ./gradlew build test &&\
    ./gradlew shadowJar -x test

FROM confluentinc/cp-kafka:8.1.0

COPY --from=build /kafka-plain-saslserver-2-ad/build/libs/kafka-plain-saslserver-2-ad-* \
                  /usr/share/java/kafka

ENV KAFKA_OPTS='-Djava.security.auth.login.config=/etc/kafka/kafka_server_jaas.conf'
ENV CLASSPATH="/etc/kafka"
