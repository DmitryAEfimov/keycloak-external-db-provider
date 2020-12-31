# A custom Keycloak User Storage Provider

This example demonstrates how to deploy custom Keycloak User storage provider as an `.ear`. 
This allows to use custom dependencies that are not part of the keycloak module space.  

The storage provider is implemented in the `ejb-module` project.

## External DB Prepare
Custom User Provider uses [user_model](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/example/db_schema.png)
This project uses single Postgres DB server both for internal and external databases. You can configure each DB server separatelly
1. Set max_prepared_transactions in postgresql.conf to a value greather then zero. See [postgresql.conf](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/config/postgresql.conf) for example
2. Run database. 
   
When use docker container you should define a network. For more information see [Official Docker Postgres](https://hub.docker.com/_/postgres) and [Oficial Docker Keycloak](https://hub.docker.com/r/jboss/keycloak)
  
    docker run
    -p 5432:5432
    -v <path_to_config>:/etc/postgresql:ro
    --env POSTGRES_PASSWORD=passwd
    --env POSTGRES_USER=user_adm
    --env POSTGRES_DB=poc
    --name postgresdb
    -t
    -i
    --net keycloak-network
    postgres:13.1-alpine -c config_file=/etc/postgresql/postgresql.conf

3. Execute script [test data](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/example/sql/external_db.sql) to fill test data.

## Keycloak Prepare
1. Configure XA DataSource for External DataBase. Optionally configure other WildFly server modules. See [cli batch script](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/config/pocds.cli)
2. Prepare internal database configuration. See previous paragraph's steps 1 and 2 
3. Execute script [keycloak db](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/example/sql/keycloak_db.sql) to prepare keycloak internal DB.
4. Start server using [Dockerfile](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/Dockerfile)

### Dockerfile environments
* EXTDB_ADDR. External database IP/hostname. Default is `EXTDB_ADDR=postgresdb`
* EXTDB_PORT. External database port. Default is `EXTDB_PORT=5432`
* EXTDB_DATABASE. External database name. No defaults
* EXTDB_USER. External database user. No defaults
* EXTDB_PASSWORD. External database user. No defaults
* EXTDB_JDBC_PARAMS. External database jdbc config parameters. Default is `EXTDB_JDBC_PARAMS=""`
* DB_VENDOR. Keycloak internal database vendor. Default is `postgres`
* KEYCLOAK_IMPORT. Import keycloak realm to work with. Defaults is `KEYCLOAK_IMPORT=/tmp/realm.json`. See example [realm.json](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/config/realm_poc.json)   
* See [Oficial Keycloak Docker](https://hub.docker.com/r/jboss/keycloak) to configure other Keycloak environments

## Deployment
    docker build -t keycloak:0.1 .
    && docker run
    -p 8080:8080
    --env KEYCLOAK_PASSWORD=<keycloak_pass>
    --env KEYCLOAK_USER=admin
    --env EXTDB_ADDR=postgresdb
    --env EXTDB_DATABASE=<ext_db_dbname>
    --env EXTDB_USER=<ext_db_user>
    --env EXTDB_PASSWORD=<ext_db_pass>
    --name keycloak
    -t
    -i
    --net keycloak-network
    keycloak:0.1

## Configure Admin Console
1. Login to keycloak with KEYCLOAK_USER and KEYCLOAK_PASSWORD.
2. Switch to imported realm (see name in [realm.json](https://github.com/DmitryAEfimov/keycloak-external-db-provider/blob/master/config/realm_poc.json))
3. Switch to User Federation tab and select `PocExternalPGFederation` provider. Optionally change `Cache Policy` and other provider parameters. Save changes.
4. Switch to Users tab. Lookup users from external DB.