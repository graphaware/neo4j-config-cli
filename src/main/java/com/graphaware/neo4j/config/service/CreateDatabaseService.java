package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.Database;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CreateDatabaseService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDatabaseService.class);

    private final Driver driver;

    public CreateDatabaseService(Driver driver) {
        this.driver = driver;
    }

    public void createDatabase(Database database) {
        createDatabase(database.name(), database.dropIfExists(), database.skipIfCreate(), database.seedFromUri());
        createIndexes(database);
        createConstraints(database);
    }

    public void createDatabase(String name, boolean dropIfExists, boolean skipCreate, String fromUri) {
        if (skipCreate) {
            LOG.info("Skipping creation of database {}", name);
            return;
        }

        if (dropIfExists) {
            dropDatabaseIfExists(name);
        }

        String query = String.format("CREATE DATABASE %s IF NOT EXISTS", name);

        if (fromUri != null && isNeo4j5()) {
            LOG.info("seedFromUri detected, will seed database from {}", fromUri);
            query = String.format("%s OPTIONS { existingData: \"use\", seedUri: \"%s\"}", query, fromUri);
        }

        LOG.info("Creating database {} ", name);
        LOG.debug("Query : {}", query);
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run(query);
        }
        waitDatabaseIsOnline(name);
    }

    private boolean isNeo4j5() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            var version = session.run("CALL dbms.components() YIELD versions RETURN versions[0] AS version").single().get("version").asString();

            return version.startsWith("5");
        }
    }

    private void createIndexes(Database database) {
        if (null != database.indexes()) {
            database.indexes().fulltext().forEach(fullTextIndex -> {
                new CreateFullTextIndex(driver, fullTextIndex).createFullTextIndexOnDatabase(database.name());
            });
        }
    }

    private void createConstraints(Database database) {
        if (null != database.constraints()) {
            database.constraints().uniqueConstraints().forEach(uniqueConstraint -> {
                new CreateUniqueConstraint(driver, uniqueConstraint).createUniqueConstraintOnDatabase(database.name());
            });
        }
    }

    private void dropDatabaseIfExists(String name) {
        String query = String.format("DROP DATABASE %s IF EXISTS", name);
        LOG.info("Dropping database {}", name);
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run(query);
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
                        && records.stream().anyMatch(r -> "leader".equals(r.asMap().get("role")));
                inTimeWindow = System.currentTimeMillis() < (startTime + 10_000);
            } while (!online && inTimeWindow);
        }
    }
}
