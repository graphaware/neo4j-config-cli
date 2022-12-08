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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.testcontainers.containers.Neo4jContainer;

import java.io.File;

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
        }

    }

}
