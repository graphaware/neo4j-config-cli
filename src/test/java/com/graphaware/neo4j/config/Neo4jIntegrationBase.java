package com.graphaware.neo4j.config;

import org.junit.jupiter.api.BeforeEach;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class Neo4jIntegrationBase {

    protected Driver driver;

    protected Neo4jAsserts neo4jAsserts;

    @BeforeEach
    public void setUp() {
        driver = driver();
        neo4jAsserts = new Neo4jAsserts(driver);
    }

    @Container
    protected static Neo4jContainer neo4jContainer = (Neo4jContainer) new Neo4jContainer("neo4j:4.3.3-enterprise")
            .withAdminPassword("password")
            .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes");

    private Driver driver() {
        return GraphDatabase.driver(neo4jContainer.getBoltUrl(), AuthTokens.basic("neo4j", "password"));
    }
}
