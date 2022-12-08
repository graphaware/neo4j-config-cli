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
import com.graphaware.neo4j.config.util.ConfigUtils;
import com.graphaware.neo4j.config.util.FileCollectionUtils;
import com.graphaware.neo4j.config.util.FilesToImport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collection;
import java.util.Map;

@Component
public class Neo4jConfigRunner implements CommandLineRunner, ExitCodeGenerator {



    private static final int EXIT_CODE = 0;
    private static final Logger LOG = LoggerFactory.getLogger(Neo4jConfigRunner.class);

    private final ImportConfiguration configuration;

    private final ImportProvider importProvider;
    private final GraphDatabaseImport graphDatabaseImport;

    public Neo4jConfigRunner(ImportProvider importProvider, GraphDatabaseImport graphDatabaseImport, ImportConfiguration configuration) {
        this.configuration = configuration;
        this.importProvider = importProvider;
        this.graphDatabaseImport = graphDatabaseImport;
    }

    @Override
    public void run(String... args) throws Exception {
        applyConfiguration();
    }

    public void applyConfiguration() throws Exception {
        LOG.info("Starting command line application");
        if (configuration.isSeedOnly()) {
            LOG.info("seed-only mode detected, loading data from {}", configuration.getSeedUrl());
            seedOnly();
        } else {
            Collection<File> importFiles = importProvider.getImportFiles(configuration.getImportPath());
            FilesToImport filesToImport = FileCollectionUtils.getFilesToImport(importFiles);
            Map<String, String> seeds = FileCollectionUtils.buildSeeds(filesToImport.getCypherSeeds());
            graphDatabaseImport.waitUntilStarted();
            filesToImport.getConfigurationFiles()
                    .forEach(f -> {
                        LOG.info("Will import from file {}", f.getPath());
                        try {
                            graphDatabaseImport.importFile(f, seeds);
                        } catch (Exception e) {
                            LOG.error(e.getMessage());
                        }
                    });
        }
        LOG.info("Ended command line application");
    }

    private void seedOnly() throws Exception {
        Map<String, String> seeds;
        if (null != configuration.getSeedUrl()) {
            seeds = Map.of(configuration.getSeedUrl(), ConfigUtils.URLToString(configuration.getSeedUrl()));
        } else {
            seeds = getSeedsFromConfigDirectory();
        }

        graphDatabaseImport.seedDatabase(configuration.getSeedDb(), seeds);

    }

    private Map<String, String> getSeedsFromConfigDirectory() throws Exception {
        Collection<File> fileCollection = importProvider.getImportFiles(configuration.getImportPath());
        Collection<File> cypherSeeds = fileCollection
                .stream()
                .filter(f -> f.getName().endsWith(".cypher"))
                .toList();
         return FileCollectionUtils.buildSeeds(fileCollection);
    }

    @Override
    public int getExitCode() {
        return EXIT_CODE;
    }
}
