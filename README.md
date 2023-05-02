[![GitHub license](https://img.shields.io/github/license/graphaware/neo4j-config-cli)](https://github.com/graphaware/neo4j-config-cli/blob/main/LICENSE.txt)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/graphaware/neo4j-config-cli?logo=github&sort=semver)](https://github.com/graphaware/neo4j-config-cli/releases/latest)
[![Docker Pulls](https://img.shields.io/docker/pulls/graphaware/neo4j-config-cli?logo=docker)](https://hub.docker.com/r/graphaware/neo4j-config-cli)

# neo4j-config-cli

`neo4j-config-cli` is a [Neo4j](https://neo4j.com) utility to ensure the desired configuration state of a Neo4j database 
based on a json file definition.

Docker : https://hub.docker.com/repository/docker/graphaware/neo4j-config-cli

The following `docker-compose.yml` : 

```yaml
version: '3'
services:
  neo4j:
    image: neo4j:5.6.0-enterprise
    ports:
      - "7474:7474"
      - "7687:7687"
    environment:
      - NEO4J_ACCEPT_LICENSE_AGREEMENT=yes
      - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD:-password}
  neo4j-config-cli:
    image: graphaware/neo4j-config-cli:2.2.0
    environment:
      - NEO4J_USER=neo4j
      - NEO4J_PASSWORD=password
      - NEO4J_URI=bolt://neo4j:7687
      - IMPORT_PATH=/config
    volumes:
      - "./config:/config"
```

with the following json definition in `config/simple-db-create.json`

```json
{
    "kind": "Database",
    "name": "movies",
    "dropIfExists": true
}
```

will wait Neo4j is started then drop the `movies` database and recreate it, once done the `neo4j-config-cli` container will stop : 

```text
$ docker-compose up
Creating network "neo4j-config-demo_default" with the default driver
Creating neo4j-config-demo_neo4j_1 ... done
Creating neo4j-config-demo_neo4j-config-cli_1 ... done
Attaching to neo4j-config-demo_neo4j-config-cli_1, neo4j-config-demo_neo4j_1
neo4j_1             | Selecting JVM - Version:11.0.12, Name:OpenJDK 64-Bit Server VM, Vendor:Oracle Corporation
neo4j-config-cli_1  |
neo4j-config-cli_1  |   .   ____          _            __ _ _
neo4j-config-cli_1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
neo4j-config-cli_1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
neo4j-config-cli_1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
neo4j-config-cli_1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
neo4j-config-cli_1  |  =========|_|==============|___/=/_/_/_/
neo4j-config-cli_1  |  :: Spring Boot ::                (v2.5.4)
neo4j-config-cli_1  |
neo4j-config-cli_1  | 2021-09-07 08:02:40.342  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : Starting Neo4jConfigCliApplication v1.1.0-SNAPSHOT using Java 11.0.11 on 4daf472cb393 with PID 1 (/opt/app.jar started by appuser in /)
neo4j-config-cli_1  | 2021-09-07 08:02:40.344  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : No active profile set, falling back to default profiles: default
neo4j-config-cli_1  | 2021-09-07 08:02:40.975  INFO 1 --- [main] org.neo4j.driver.Driver                  : Direct driver instance 841262455 created for server address neo4j:7687
neo4j-config-cli_1  | 2021-09-07 08:02:41.160  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : Started Neo4jConfigCliApplication in 1.29 seconds (JVM running for 1.677)
neo4j-config-cli_1  | 2021-09-07 08:02:41.162  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Starting command line application
neo4j-config-cli_1  | 2021-09-07 08:02:41.168  INFO 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Detecting neo4j server availability
neo4j-config-cli_1  | 2021-09-07 08:02:41.300 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not yet available, waiting for 2 seconds...
neo4j_1             | Changed password for user 'neo4j'.
neo4j-config-cli_1  | 2021-09-07 08:02:43.309 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not yet available, waiting for 2 seconds...
neo4j_1             | 2021-09-07 08:02:44.228+0000 INFO  Starting...
neo4j-config-cli_1  | 2021-09-07 08:02:45.313 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not yet available, waiting for 2 seconds...
neo4j_1             | 2021-09-07 08:02:45.959+0000 INFO  ======== Neo4j 4.3.3 ========
neo4j-config-cli_1  | 2021-09-07 08:02:47.303 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not yet available, waiting for 2 seconds...
neo4j_1             | 2021-09-07 08:02:47.731+0000 INFO  Sending metrics to CSV file at /var/lib/neo4j/metrics
neo4j_1             | 2021-09-07 08:02:48.012+0000 INFO  Bolt enabled on 0.0.0.0:7687.
neo4j_1             | 2021-09-07 08:02:48.917+0000 INFO  Remote interface available at http://localhost:7474/
neo4j_1             | 2021-09-07 08:02:48.919+0000 INFO  Started.
neo4j-config-cli_1  | 2021-09-07 08:02:49.694  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Will import from file /config/simple-db-create.json
neo4j-config-cli_1  | 2021-09-07 08:02:49.781  INFO 1 --- [main] c.g.n.c.service.CreateDatabaseService    : Dropping database movies
neo4j-config-cli_1  | 2021-09-07 08:02:50.048  INFO 1 --- [main] c.g.n.c.service.CreateDatabaseService    : Creating database movies
neo4j-config-cli_1  | 2021-09-07 08:02:51.648  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Ended command line application
neo4j-config-cli_1  | 2021-09-07 08:02:51.654  INFO 1 --- [main] org.neo4j.driver.Driver                  : Closing driver instance 841262455
neo4j-config-cli_1  | 2021-09-07 08:02:51.657  INFO 1 --- [main] org.neo4j.driver.ConnectionPool          : Closing connection pool towards neo4j:7687
neo4j-config-demo_neo4j-config-cli_1 exited with code 0
```

## Config files

Two `kind` of config files are supported today : 

- `Database` kind
- `Role` kind

### Database Config File

**Structure**

```json
{
  "kind": "Database",
  "name": "movies",
  "dropIfExists": "true",
  "indexes": {
    "nodes" : [
      {
        "labels": ["Person"],
        "properties": ["name"],
        "name": "Person",
        "type": "FULLTEXT"
      }
    ],
  },
  "constraints": {
    "nodes": [
      {
        "label": "Person",
        "property": "name",
        "type": "UNIQUE"
      }
    ]
  },
  "seeds": ["movies.cypher"]
}
```

### Role Config File

**Structure**

```json
{
  "kind": "Role",
  "name": "ut1",
  "dropIfExists": true,
  "privileges": [
    {
      "graph": "*",
      "access": true,
      "rules": [
        {
          "target": "node",
          "labels": "*",
          "action": "match",
          "resource": "all_properties",
          "access": "GRANTED"
        },
        {
          "target": "node",
          "labels": "Person",
          "action": "read",
          "resource": "born",
          "access": "DENIED"
        }
      ]
    }
  ]
}
```

---

## Neo4j Aura

The command `CREATE DATABASE` is not available on Neo4j aura, you thus have to skip the step where this tool will 
execute that command by specifying `skipCreate = true` : 

```json
{
  "kind": "Database",
  "name": "neo4j",
  "skipCreate": true
}
```

A full working example with Aura is available [here](./examples/aura) after inserting your Neo4j Aura credentials in 
the docker-compose file.

---

## Importing from remote files

You can let your config files on the web, for eg as a `Github Gist` : https://gist.github.com/ikwattro/f99c1ed085673065fcb4e850526ccd49

You will need to specify the raw versions of it, for eg :

```yaml
version: '3.7'
services:
  neo4j:
    image: neo4j:4.4.15-enterprise
    ports:
      - "7474:7474"
      - "7687:7687"
    environment:
      - NEO4J_ACCEPT_LICENSE_AGREEMENT=yes
      - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD:-password}
  neo4j-config-cli:
    image: graphaware/neo4j-config-cli:1.2.0
    environment:
      - NEO4J_PASSWORD=password
      - NEO4J_URI=bolt://neo4j:7687
      - IMPORT_PATH=https://gist.githubusercontent.com/ikwattro/f99c1ed085673065fcb4e850526ccd49/raw/35f077b71e3f7a9fd95b0288cf6d622eea3d6501/db-demo.json
```

## Seeding only an existing database

```shell
docker run --rm -it \
    -e NEO4J_URI=neo4j+s://18894d85.databases.neo4j.io \
    -e NEO4J_PASSWORD=tT3h3ieK4sw-MATaEaimFFHY9YqkgYNo9WNYcEBqMZ4 \
    -e seed-only=true \
    -e seed-url=https://bit.ly/2XnJzFn \
    graphaware/neo4j-config-cli:1.3.0
```

## Seeding a database from a Neo4j 5 backup ( seedFromUri )

This feature requires that your Neo4j configuration allows seeding from uri's

```yaml
- NEO4J_dbms_databases_seed__from__uri__providers=URLConnectionSeedProvider
```

```json
{
  "kind": "Database",
  "name": "world.cup",
  "dropIfExists": "false",
  "seedFromUri": "https://downloads.graphaware.com/neo4j-db-seeds/world-cup-2022-neo4j.backup"
}
```

## Licence

Refer to the `LICENSE.txt` file shipped in this repository

## Support

This repository is supported on best-effort basis.





