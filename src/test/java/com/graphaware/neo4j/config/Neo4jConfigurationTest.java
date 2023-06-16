/*
 * Copyright (C) 2013 - 2021 GraphAware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.graphaware.neo4j.config;

import com.graphaware.neo4j.config.properties.ImportConfiguration;
import com.graphaware.neo4j.config.service.CreateDatabaseService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.testcontainers.containers.Neo4jContainer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.neo4j.driver.Values.ofString;

public class Neo4jConfigurationTest extends MultipleNeo4jVersionsTest {

    @ParameterizedTest
    @MethodSource("neo4jVersions")
    void loading_config(String version) throws Exception {
        var configPath = new File("src/test/resources/config").getAbsolutePath();
        ResourceLoader resourceLoader = new FileSystemResourceLoader();
        ImportProvider importProvider = new ImportProvider(resourceLoader);
        var config = new ImportConfiguration(configPath, false, null, "neo4j");
        Neo4jContainer<?> neo4j = getNeo4j(version);
        Driver neo4jDriver = getDriver(neo4j);

        var service = new CreateDatabaseService(neo4jDriver);
        var importer = new GraphDatabaseImport(neo4jDriver, service);

        var runner = new Neo4jConfigRunner(importProvider, importer, config);
        runner.applyConfiguration();

        try (Driver driver = getDriver(neo4j)) {
            try (Session session = driver.session(SessionConfig.forDatabase("movies"))) {
                // assert database exist
                session.run("CALL db.ping()").consume();

                assertThat(session.run("MATCH (n:Movie) RETURN count(n) AS c").single().get("c").asLong()).isGreaterThan(0);
            }

            try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
                var roles = session.run("SHOW ROLES YIELD role RETURN collect(role) AS roles").single().get("roles").asList(ofString());
                assertThat(roles).asList().contains("ut1");
            }

            try (Session session = driver.session(SessionConfig.forDatabase("world.cup"))) {
                session.run("CALL db.ping()").consume();

                if (version.startsWith("5")) {
                    var nodesCount = session.run("MATCH (n) RETURN count(n) AS c").single().get("c").asLong();

                    assertThat(nodesCount).isGreaterThan(0);
                }
            }

            if (version.startsWith("5")) {
                try (Session session = driver.session(SessionConfig.forDatabase("relationship.constraints"))) {
                    final Map<String, String> constraints = new HashMap<>();
                    final Map<String, String> propertyTypeConstraints = new HashMap<>();
                    session.run("SHOW CONSTRAINTS").list().forEach(record -> {
                                constraints.put(record.get("name").asString(), record.get("type").asString());
                                propertyTypeConstraints.put(record.get("name").asString(), record.get("propertyType").asString());
                            });

                    Assertions.assertEquals("RELATIONSHIP_UNIQUENESS", constraints.get("rel_uniq_RELTYPE_3_id"));
                    Assertions.assertEquals("RELATIONSHIP_KEY", constraints.get("rel_rk_RELTYPE_4_id"));
                    Assertions.assertEquals("ZONED DATETIME", propertyTypeConstraints.get("rel_ptc_since_RELTYPE_100"));
                }

                try (Session session = driver.session(SessionConfig.forDatabase("movies"))) {
                    final Map<String, String> propertyTypeConstraints = new HashMap<>();
                    session.run("SHOW CONSTRAINTS").list().forEach(record -> {
                        propertyTypeConstraints.put(record.get("name").asString(), record.get("propertyType").asString());
                    });
                    Assertions.assertEquals("STRING", propertyTypeConstraints.get("unique_person_name"));
                }
            }

            try (Session session = driver.session(SessionConfig.forDatabase("movies"))) {
                var constraints = session.run("SHOW CONSTRAINTS").list().stream().map(record -> record.get("name").asString()).toList();
                Assertions.assertTrue(constraints.contains("cr_person_id_unique"));
            }
        }

    }

}
