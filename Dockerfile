### Build Java Application image ###
FROM maven:3.6.3-jdk-11 as builder

WORKDIR /tmp/poc

COPY ear-module /tmp/poc/ear-module
COPY ejb-module /tmp/poc/ejb-module
COPY pom.xml .

RUN mvn clean install

### Run Java Application image ###
FROM jboss/keycloak:12.0.1
MAINTAINER  Dmitry Efimov <dmitry.a.efimov@gmail.com>

ARG app_version=0.1

# Internal Keycloak DB params
ENV DB_VENDOR=postgres

# External DB params for custom user provider
ENV EXTDB_ADDR=postgresdb
ENV EXTDB_PORT=5432
ENV EXTDB_DATABASE=poc
ENV EXTDB_USER=user_adm
ENV EXTDB_PASSWORD=passwd
ENV EXTDB_JDBC_PARAMS=""

COPY --from=builder /tmp/poc/ear-module/target/poc-extdb-storage-provider-bundle-${app_version}.ear /opt/jboss/keycloak/standalone/deployments/

COPY config/pocds.cli /opt/jboss/startup-scripts/pocds.cli
COPY config/realm_poc.json /tmp/realm_poc.json

ENV KEYCLOAK_IMPORT=/tmp/realm_poc.json