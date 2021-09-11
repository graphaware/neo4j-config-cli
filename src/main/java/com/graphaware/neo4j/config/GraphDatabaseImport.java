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
import java.util.Arrays;
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
        if (config.get("kind").equals("Database")) {
            Database database = MAPPER.convertValue(config, Database.class);
            importDatabase(database);
            if (!database.getSeeds().isEmpty()) {
                for (String se : database.getSeeds()) {
                    importSeed(database.getName(), se, seeds);
                }
            }
        }

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

            if (role.isDropIfExists()) {
                session.run(String.format("DROP ROLE %s IF EXISTS", role.getName()));
            }

            String q = String.format( "CREATE ROLE %s IF NOT EXISTS", role.getName());
            session.run(q);

            for (Privilege privilege : role.getPrivileges()) {

                if (privilege.isAccess()) {
                    String aq = String.format("GRANT ACCESS ON DATABASE %s TO %s", privilege.getGraph(), role.getName());
                    session.run(aq);
                }

                for (Rule rule : privilege.getRules()) {
                    if (matchRead.contains(rule.getAction())) {
                        String properties = rule.getResource().equals("all_properties")
                                ? "*"
                                : rule.getResource();

                        String op = rule.getAccess().equals(RuleAccess.GRANTED) ? "GRANT" : "DENY";
                        String target = rule.getTarget().equals("node") ? "NODES" : "RELATIONSHIPS";
                        String action = rule.getAction().toUpperCase();

                        String oq = String.format("%s %s {%s} ON GRAPH %s %s %s TO %s",
                                op,
                                action,
                                properties,
                                privilege.getGraph(),
                                target,
                                rule.getLabels(),
                                role.getName()
                                );
                        session.run(oq);
                    } else {
                        String op = rule.getAccess().equals(RuleAccess.GRANTED) ? "GRANT" : "DENY";
                        String target = rule.getTarget().equals("node") ? "NODES" : "RELATIONSHIPS";
                        String action = rule.getAction().toUpperCase();

                        String oq = String.format("%s %s ON GRAPH %s %s %s TO %s",
                                op,
                                action,
                                privilege.getGraph(),
                                target,
                                rule.getLabels(),
                                role.getName()
                        );
                        session.run(oq);
                    }
                }
            }
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
}
