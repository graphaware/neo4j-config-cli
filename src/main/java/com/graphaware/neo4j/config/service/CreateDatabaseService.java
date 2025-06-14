package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.Database;
import com.graphaware.neo4j.config.model.schema.ConstraintType;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

import static org.neo4j.driver.Values.ofString;

@Service
public class CreateDatabaseService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDatabaseService.class);

    private final Driver driver;

    public CreateDatabaseService(Driver driver) {
        this.driver = driver;
    }

    public void createDatabase(Database database) {
        if (database.composite()) {
            createCompositeDatabase(database);
        } else {
            createDatabase(database.name(), database.dropIfExists(), database.skipIfCreate(), database.seedFromUri());
            createIndexes(database);
            createConstraints(database);
        }
    }

    private void createCompositeDatabase(Database database) {
        if (database.skipIfCreate()) {
            LOG.info("Skipping creation of composite database {}", database.name());
            return;
        }

        LOG.info("Creating composite database {}", database.name());
        String query = "CREATE COMPOSITE DATABASE `%s` IF NOT EXISTS ".formatted(database.name());
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run(query);

            for (String db : database.constituents()) {
                LOG.info("Creating alias {} for composite database {}", db, database.name());
                String aliasQuery = "CREATE ALIAS `%s`.`%s` IF NOT EXISTS FOR DATABASE %s".formatted(database.name(), db, db);
                session.run(aliasQuery);
            }
        }
    }

    public void createDatabase(String name, boolean dropIfExists, boolean skipCreate, String fromUri) {
        if (skipCreate) {
            LOG.info("Skipping creation of database {}", name);
            return;
        }

        if (dropIfExists) {
            dropDatabaseIfExists(name);
        }

        String query = "CREATE DATABASE `%s` IF NOT EXISTS ".formatted(name);

        if (fromUri != null && isNeo4j5OrAbove()) {
            LOG.info("seedFromUri detected, will seed database from {}", fromUri);
            query = "%s OPTIONS { existingData: \"use\", seedURI: \"%s\"} ".formatted(query, fromUri);
        }
        query = "%s WAIT".formatted(query);

        LOG.info("Creating database {} ", name);
        LOG.debug("Query : {}", query);
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            var result = session.run(query).list().get(0);
            System.out.println(result.asMap());
            var success = result.get("success").asBoolean();
            var message = result.get("message").asString();

            if (!success) {
                LOG.error("Creating database %s with seedURI failed with the following message : %s".formatted(name, message));
                LOG.info("Dropping database %s after failed seedURI".formatted(name));
                return;
            }
        }
        waitDatabaseIsOnline(name);
    }

    private boolean isNeo4j5OrAbove() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            var result = session.run("CALL dbms.components()");
            var dbmsComponents = result.list().stream().map(r -> r.as(DbmsComponent.class)).toList();
            var version = dbmsComponents.stream().filter(c -> c.name().equals("Neo4j Kernel")).findFirst().get().versions().get(0);

            return version.startsWith("5") || version.startsWith("20");
        }
    }

    private void createIndexes(Database database) {
        if (null != database.indexes()) {
            database.indexes().nodes().forEach(fullTextIndex -> {
                new CreateNodeIndex(driver, fullTextIndex).createIndex(database.name());
            });
            database.indexes().relationships().forEach(relationshipIndex -> {
                new CreateRelationshipIndex(driver, relationshipIndex).createIndex(database.name());
            });
        }
    }

    private void createConstraints(Database database) {
        if (null != database.constraints()) {
            database.constraints().nodes()
                    .stream().filter(c -> {
                        return !c.type().equals(ConstraintType.PROPERTY_TYPE) || isNeo4j5OrAbove();
                    })
                    .forEach(constraint -> {
                new CreateNodeConstraint(driver, constraint).createConstraintOnDatabase(database.name());
            });

            if (isNeo4j5OrAbove()) {
                database.constraints().relationships().forEach(relationshipConstraint -> {
                    new CreateRelationshipConstraint(driver, relationshipConstraint).createSchemaConstraintForRelationship(database.name());
                });
            }
        }
    }

    private void dropDatabaseIfExists(String name) {
        dropDatabaseAliases(name);
        String query = String.format("DROP DATABASE %s IF EXISTS", name);
        LOG.info("Dropping database {}", name);
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run(query);
        }
    }

    private void dropDatabaseAliases(String name) {
        Set<String> aliases = new HashSet<>();
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run("SHOW DATABASES YIELD name, aliases RETURN *").forEachRemaining(record -> {
                if (record.get("name").asString().equals(name)) {
                    aliases.addAll(record.get("aliases").asList(ofString()));
                }
            });

            aliases.forEach(alias -> {
                LOG.info("Dropping database alias {}", alias);
                session.run("DROP ALIAS $alias IF EXISTS FOR DATABASE", Map.of("alias", alias));
            });
        }
    }

    private void waitDatabaseIsOnline(String databaseName) {
        long startTime = System.currentTimeMillis();
        boolean online;
        boolean inTimeWindow;
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            do {
                List<Record> records = session.run(String.format("SHOW DATABASE `%s`", databaseName)).list();
                online = records.size() > 0
                        && records.stream().allMatch(r -> "online".equals(r.asMap().get("currentStatus")))
                        && records.stream().anyMatch(r -> "leader".equals(r.asMap().get("role")) || "primary".equals(r.asMap().get("role")));
                inTimeWindow = System.currentTimeMillis() < (startTime + 10_000);
            } while (!online && inTimeWindow);
        }
    }
}
