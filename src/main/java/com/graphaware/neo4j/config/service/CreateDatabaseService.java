package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.Database;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CreateDatabaseService {

    private static final Logger LOG = LoggerFactory.getLogger(CreateDatabaseService.class);

    private final Driver driver;

    public CreateDatabaseService(Driver driver) {
        this.driver = driver;
    }

    public void createDatabase(Database database) {
        createDatabase(database.getName(), database.isDropIfExists());
        createIndexes(database);
    }

    public void createDatabase(String name, boolean dropIfExists) {
        if (dropIfExists) {
            dropDatabaseIfExists(name);
        }
        String query = String.format("CREATE DATABASE %s IF NOT EXISTS", name);
        LOG.info("Creating database {} ", name);
        LOG.debug("Query : {}", query);
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            session.run(query);
        }
    }

    private void createIndexes(Database database) {
        if (null != database.getIndexes()) {
            database.getIndexes().getFulltext().forEach(fullTextIndex -> {
                new CreateFullTextIndex(driver, fullTextIndex).createFullTextIndexOnDatabase(database.getName());
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
}
