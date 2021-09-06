package com.graphaware.neo4j.config;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class Neo4jAsserts {

    private final Driver driver;

    public Neo4jAsserts(Driver driver) {
        this.driver = driver;
    }

    public void databaseExists(String databaseName) {
        assertThat(listDatabaseNames()).asList().contains(databaseName);
    }

    public void databaseNotExists(String databaseName) {
        assertThat(listDatabaseNames()).asList().doesNotContain(databaseName);
    }

    public void databaseEmpty(String databaseName) {
        assertThat(countNodes(databaseName)).isEqualTo(0);
    }

    public void databaseNotEmpty(String databaseName) {
        assertThat(countNodes(databaseName)).isGreaterThan(0);
    }

    public void indexExistForName(String name) {
        assertThat(listIndexNames()).asList().contains(name);
    }

    public void indexDoesNotExistForName(String name) {
        assertThat(listIndexNames()).asList().doesNotContain(name);
    }

    private long countNodes(String databaseName) {
        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            return session.run("MATCH (n) RETURN count(n) AS c").single().get("c").asLong();
        }
    }

    private List<String> listDatabaseNames() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            return session.run("SHOW DATABASES").list().stream().map(record -> record.get("name").asString()).collect(Collectors.toList());
        }
    }

    private List<String> listIndexNames() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            return session.run("SHOW INDEXES").list().stream().map(record -> record.get("name").asString()).collect(Collectors.toList());
        }
    }
}
