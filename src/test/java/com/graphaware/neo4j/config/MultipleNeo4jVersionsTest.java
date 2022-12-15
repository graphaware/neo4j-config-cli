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

import com.graphaware.neo4j.config.version.Neo4jVersion;
import org.junit.jupiter.api.TestInstance;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.stream.Stream;

@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class MultipleNeo4jVersionsTest {

    protected static String HEAP_SIZE = "256M";

    public static Stream<String> neo4jVersions() {
        return Stream.of("4.4.14", "5.2.0");
    }

    protected static String heapSizeSetting(Neo4jVersion version) {
        return version.equals(Neo4jVersion.V4_4)
                ? "NEO4J_dbms_memory_heap_max__size"
                : "NEO4J_server_memory_heap_max__size"
                ;
    }

    protected Neo4jContainer<?> getNeo4j(String version) {
        var imageName = String.format("neo4j:%s-enterprise", version);
        Neo4jVersion neo4jVersion = Neo4jVersion.of(version);
        Neo4jContainer<?> container = new Neo4jContainer<>(imageName)
                .withoutAuthentication()
                .withEnv("NEO4J_ACCEPT_LICENSE_AGREEMENT", "yes")
                .withEnv(heapSizeSetting(neo4jVersion), HEAP_SIZE)
                .withReuse(true);
        if (version.startsWith("5")) {
            container.withEnv("NEO4J_dbms_databases_seed__from__uri__providers", "URLConnectionSeedProvider");
        }
        container.start();

        return container;
    }

    protected Driver getDriver(Neo4jContainer<?> neo4j) {
        return GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.none());
    }
}