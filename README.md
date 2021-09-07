[![GitHub license](https://img.shields.io/github/license/graphaware/neo4j-config-cli)](https://github.com/graphaware/neo4j-config-cli/blob/main/LICENSE.txt)
[![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/graphaware/neo4j-config-cli?logo=github&sort=semver)](https://github.com/graphaware/neo4j-config-cli/releases/latest)
[![Docker Pulls](https://img.shields.io/docker/pulls/graphaware/neo4j-config-cli?logo=docker)](https://hub.docker.com/r/graphaware/neo4j-config-cli)

# neo4j-config-cli

`neo4j-config-cli` is a [Neo4j](https://neo4j.com) utility to ensure the desired configuration state of a Neo4j database 
based on a json file definition.

Docker : https://hub.docker.com/repository/docker/graphaware/neo4j-config-cli

The following `docker-compose.yml` : 

```yaml
version: '3.7'
services:
  neo4j:
    image: neo4j:4.3.3-enterprise
    ports:
      - "7474:7474"
      - "7687:7687"
    environment:
      - NEO4J_ACCEPT_LICENSE_AGREEMENT=yes
      - NEO4J_AUTH=neo4j/${NEO4J_PASSWORD:-password}
  neo4j-config-cli:
    image: graphaware/neo4j-config-cli:1.0.0-SNAPSHOT
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
Creating neo4j-config-demo_neo4j-config-cli_1 ... done
Creating neo4j-config-demo_neo4j_1            ... done
Attaching to neo4j-config-demo_neo4j-config-cli_1, neo4j-config-demo_neo4j_1
neo4j-config-cli_1  |
neo4j-config-cli_1  |   .   ____          _            __ _ _
neo4j-config-cli_1  |  /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
neo4j-config-cli_1  | ( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
neo4j-config-cli_1  |  \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
neo4j-config-cli_1  |   '  |____| .__|_| |_|_| |_\__, | / / / /
neo4j-config-cli_1  |  =========|_|==============|___/=/_/_/_/
neo4j-config-cli_1  |  :: Spring Boot ::                (v2.5.4)
neo4j-config-cli_1  |
neo4j-config-cli_1  | 2021-09-06 20:52:59.968  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : Starting Neo4jConfigCliApplication v1.0.0-SNAPSHOT using Java 11.0.11 on 3b022cf6c2fc with PID 1 (/opt/app.jar started by appuser in /)
neo4j-config-cli_1  | 2021-09-06 20:52:59.970  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : No active profile set, falling back to default profiles: default
neo4j_1             | Selecting JVM - Version:11.0.12, Name:OpenJDK 64-Bit Server VM, Vendor:Oracle Corporation
neo4j-config-cli_1  | 2021-09-06 20:53:00.736  INFO 1 --- [main] org.neo4j.driver.Driver                  : Direct driver instance 775081157 created for server address neo4j:7687
neo4j-config-cli_1  | 2021-09-06 20:53:00.931  INFO 1 --- [main] c.g.n.config.Neo4jConfigCliApplication   : Started Neo4jConfigCliApplication in 1.42 seconds (JVM running for 1.816)
neo4j-config-cli_1  | 2021-09-06 20:53:00.934  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Starting command line application
neo4j-config-cli_1  | 2021-09-06 20:53:00.943  INFO 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Detecting neo4j server availability
neo4j-config-cli_1  | 2021-09-06 20:53:01.046 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not available, waiting for 5 seconds...
neo4j_1             | Changed password for user 'neo4j'.
neo4j_1             | 2021-09-06 20:53:04.130+0000 INFO  Starting...
neo4j_1             | 2021-09-06 20:53:05.862+0000 INFO  ======== Neo4j 4.3.3 ========
neo4j-config-cli_1  | 2021-09-06 20:53:06.056 ERROR 1 --- [main] c.g.neo4j.config.GraphDatabaseImport     : Neo4j server not available, waiting for 5 seconds...
neo4j_1             | 2021-09-06 20:53:07.747+0000 INFO  Sending metrics to CSV file at /var/lib/neo4j/metrics
neo4j_1             | 2021-09-06 20:53:07.777+0000 INFO  Bolt enabled on 0.0.0.0:7687.
neo4j_1             | 2021-09-06 20:53:08.755+0000 INFO  Remote interface available at http://localhost:7474/
neo4j_1             | 2021-09-06 20:53:08.757+0000 INFO  Started.
neo4j-config-cli_1  | 2021-09-06 20:53:12.855  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Will import from file /config/simple-db-create.json
neo4j-config-cli_1  | 2021-09-06 20:53:12.921  INFO 1 --- [main] c.g.n.c.service.CreateDatabaseService    : Creating database movies
neo4j-config-cli_1  | 2021-09-06 20:53:14.834  INFO 1 --- [main] c.g.neo4j.config.Neo4jConfigRunner       : Ended command line application
neo4j-config-cli_1  | 2021-09-06 20:53:14.837  INFO 1 --- [main] org.neo4j.driver.Driver                  : Closing driver instance 775081157
neo4j-config-cli_1  | 2021-09-06 20:53:14.839  INFO 1 --- [main] org.neo4j.driver.ConnectionPool          : Closing connection pool towards neo4j:7687
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
    "fulltext" : [
      {
        "labels": ["Person"],
        "properties": ["name"],
        "name": "Person"
      }
    ],
    "uniqueness": [
      {
        "label": "Person",
        "property": "name"
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

## Licence

Refer to the `LICENSE.txt` file shipped in this repository

## Support

This repository is supported on best-effort basis.





