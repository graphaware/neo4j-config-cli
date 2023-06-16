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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.neo4j.config.model.Database;
import com.graphaware.neo4j.config.model.rbac.Privilege;
import com.graphaware.neo4j.config.model.rbac.Role;
import com.graphaware.neo4j.config.model.rbac.Rule;
import com.graphaware.neo4j.config.model.rbac.RuleAccess;
import com.graphaware.neo4j.config.service.CreateDatabaseService;
import com.graphaware.neo4j.config.util.ConfigUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GraphDatabaseImport {

    @Value("${import.waiting.timeout}")
    private int importWaitingTimeout;

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Logger LOG = LoggerFactory.getLogger(GraphDatabaseImport.class);

    private final Driver driver;
    private final CreateDatabaseService createDatabaseService;

    public GraphDatabaseImport(Driver driver, CreateDatabaseService createDatabaseService) {
        this.driver = driver;
        this.createDatabaseService = createDatabaseService;
    }

    private SessionConfig sessionConfig() {
        return SessionConfig.forDatabase("system");
    }

    public void importFile(File file, Map<String, String> seeds) throws Exception {
        String s = new String(Files.readAllBytes(Paths.get(file.getPath())));
        Map<String, Object> config = MAPPER.readValue(s, new TypeReference<>() {});
        List<Database> compositeDatabases = new ArrayList<>();
        if (config.get("kind").equals("Database")) {
            LOG.info("deserializing file %s to Database object".formatted(file.getPath()));
            Database database = MAPPER.convertValue(config, Database.class);
            if (database.composite()) {
                compositeDatabases.add(database);
            } else {
                Map<String, String> remoteSeeds = getRemoteSeeds(database);
                seeds.putAll(remoteSeeds);
                importDatabase(database);
                if (!database.seeds().isEmpty()) {
                    for (String se : database.seeds()) {
                        importSeed(database.name(), se, seeds);
                    }
                }
            }
        }

        compositeDatabases.forEach(this::importDatabase);

        if (config.get("kind").equals("Role")) {
            importPrivileges(MAPPER.convertValue(config, Role.class));
        }
    }

    private void importPrivileges(Role role) {
        importRole(role);
    }

    private void importDatabase(Database database) {
        createDatabaseService.createDatabase(database);
    }

    private void importRole(Role role) {
        List<String> matchRead = List.of("match", "read");
        try (Session session = driver.session(sessionConfig())) {

            if (role.dropIfExists()) {
                session.run(String.format("DROP ROLE %s IF EXISTS", role.name()));
            }

            String q = String.format( "CREATE ROLE %s IF NOT EXISTS", role.name());
            session.run(q);

            for (Privilege privilege : role.privileges()) {

                if (privilege.access()) {
                    String aq = String.format("GRANT ACCESS ON DATABASE %s TO %s", privilege.graph(), role.name());
                    session.run(aq);
                }

                for (Rule rule : privilege.rules()) {
                    if (matchRead.contains(rule.action())) {
                        String properties = rule.resource().equals("all_properties")
                                ? "*"
                                : rule.resource();

                        String op = rule.access().equals(RuleAccess.GRANTED) ? "GRANT" : "DENY";
                        String target = rule.target().equals("node") ? "NODES" : "RELATIONSHIPS";
                        String action = rule.action().toUpperCase();

                        String oq = String.format("%s %s {%s} ON GRAPH %s %s %s TO %s",
                                op,
                                action,
                                properties,
                                privilege.graph(),
                                target,
                                rule.labels(),
                                role.name()
                                );
                        session.run(oq);
                    } else {
                        String op = rule.access().equals(RuleAccess.GRANTED) ? "GRANT" : "DENY";
                        String target = rule.access().name().equals("node") ? "NODES" : "RELATIONSHIPS";
                        String action = rule.action().toUpperCase();

                        String oq = String.format("%s %s ON GRAPH %s %s %s TO %s",
                                op,
                                action,
                                privilege.graph(),
                                target,
                                rule.labels(),
                                role.name()
                        );
                        session.run(oq);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }
    }

    private void importSeed(String dbname, String seed, Map<String, String> seeds) {
        if (!seeds.containsKey(seed)) {
            LOG.error("Seed {} for database {} is not present", seed, dbname);
            return;
        }
        List<String> lines = Arrays.asList(seeds.get(seed).split(";"));
        try (Session session = driver.session(SessionConfig.forDatabase(dbname))) {
            lines
                    .stream()
                    .filter(l -> !l.trim().equals(""))
                    .forEach(session::run);
        }
    }

    private void importSeed(String dbname, Map<String, String> seeds) {
        for (String s : seeds.values()) {
            List<String> lines = Arrays.asList(s.split(";"));
            try (Session session = driver.session(SessionConfig.forDatabase(dbname))) {
                lines
                        .stream()
                        .filter(l -> !l.trim().equals(""))
                        .forEach(session::run);
            }
        }
    }

    public void waitUntilStarted() throws Exception {
        LOG.info("Detecting neo4j server availability");
        long begin = System.currentTimeMillis();
        boolean available = false;
        do {
            try (Session session = driver.session(sessionConfig())) {
                session.run("CALL db.ping()");
                available = true;
            } catch (Exception e) {
                LOG.error("Neo4j server not yet available, waiting for 2 seconds...");
                Thread.sleep(2000);
            }
        } while (((System.currentTimeMillis() - begin) < importWaitingTimeout) && !available);
    }

    private Map<String, String> getRemoteSeeds(Database database) {
        Map<String, String> seeds = new HashMap<>();
        database.seeds().forEach(s -> {
            if (s.startsWith("http")) {
                try {
                    seeds.put(s, ConfigUtils.URLToString(s));
                } catch (Exception e) {
                    LOG.error(e.getMessage());
                }
            }
        });

        return seeds;
    }

    private static boolean isRemoteSeed(String s) {
        return s.startsWith("http") || s.startsWith("file://");
    }

    public void seedDatabase(String database, Map<String, String> seeds) {
        importSeed(database, seeds);
    }
}
