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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Neo4jConfigRunner implements CommandLineRunner, ExitCodeGenerator {

    @Value("${import.path}")
    private String importPath;

    private static final int EXIT_CODE = 0;
    private static final Logger LOG = LoggerFactory.getLogger(Neo4jConfigRunner.class);

    private final ImportProvider importProvider;
    private final GraphDatabaseImport graphDatabaseImport;

    public Neo4jConfigRunner(ImportProvider importProvider, GraphDatabaseImport graphDatabaseImport) {
        this.importProvider = importProvider;
        this.graphDatabaseImport = graphDatabaseImport;
    }

    @Override
    public void run(String... args) throws Exception {
        LOG.info("Starting command line application");
        Collection<File> fileCollection = importProvider.getImportFiles(importPath);
        Collection<File> cypherSeeds = fileCollection
                .stream()
                .filter(f -> f.getName().endsWith(".cypher"))
                .collect(Collectors.toList());
        Map<String, String> seeds = buildSeeds(cypherSeeds);
        graphDatabaseImport.waitUntilStarted();
        fileCollection
                .stream()
                .filter(f -> !f.getName().endsWith(".cypher"))
                .forEach(f -> {
            LOG.info("Will import from file {}", f.getPath());
            try {
                graphDatabaseImport.importFile(f, seeds);
            } catch (Exception e) {
                LOG.error(e.getMessage());
                e.printStackTrace();
            }
        });
        Thread.sleep(1000);
        LOG.info("Ended command line application");
    }

    @Override
    public int getExitCode() {
        return EXIT_CODE;
    }

    private Map<String, String> buildSeeds(Collection<File> files) {
        Map<String, String> seeds = new HashMap<>();
        for (File file : files) {
            try {
                String s = new String(Files.readAllBytes(Paths.get(file.getPath())));
                seeds.put(Paths.get(file.getPath()).getFileName().toString(), s);
            } catch (Exception e) {
                LOG.error("Could not read cypher seed {}", file.getPath());
            }
        }

        return seeds;
    }
}
