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

package com.graphaware.neo4j.config.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class FileCollectionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(FileCollectionUtils.class);

    public static FilesToImport getFilesToImport(Collection<File> files) {
        var cypherSeeds = files
                .stream()
                .filter(f -> f.getName().endsWith(".cypher"))
                .collect(Collectors.toList());
        var configFiles = files
                .stream()
                .filter(f -> !f.getName().endsWith(".cypher"))
                .toList();

        return new FilesToImport(configFiles, cypherSeeds);
    }

    public static Map<String, String> buildSeeds(Collection<File> files) {
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
