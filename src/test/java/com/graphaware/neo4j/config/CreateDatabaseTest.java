package com.graphaware.neo4j.config;

import com.graphaware.neo4j.config.model.Database;
import com.graphaware.neo4j.config.model.Indexes;
import com.graphaware.neo4j.config.model.rbac.Constraints;
import com.graphaware.neo4j.config.service.CreateDatabaseService;
import org.junit.jupiter.api.Test;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.util.List;

public class CreateDatabaseTest extends Neo4jIntegrationBase {

    private Database database(String name, boolean dropIfExists) {
        return new Database(name, dropIfExists, false, new Indexes(List.of()), new Constraints(List.of()), List.of());
    }

    @Test
    public void create_database() {
        CreateDatabaseService createDatabaseService = new CreateDatabaseService(driver);
        Database database = database("test", true);
        neo4jAsserts.databaseNotExists(database.name());
        createDatabaseService.createDatabase(database);
        neo4jAsserts.databaseExists(database.name());
    }

    @Test
    public void drop_before_create() {
        CreateDatabaseService createDatabaseService = new CreateDatabaseService(driver);
        Database database = database("droptest", true);
        createDatabaseService.createDatabase(database);
        insertSomeData(database.name());
        neo4jAsserts.databaseNotEmpty(database.name());
        createDatabaseService.createDatabase(database);
        neo4jAsserts.databaseEmpty(database.name());
    }

    private void insertSomeData(String databaseName) {
        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run("UNWIND range(1, 10) AS i CREATE (n:Node {id: i, data: timestamp()})");
        }
    }
}
