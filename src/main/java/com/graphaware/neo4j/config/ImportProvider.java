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

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class ImportProvider {

    private final ResourceLoader resourceLoader;

    public ImportProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Collection<File> getImportFiles(String path) throws Exception {
        if (!ResourceUtils.isUrl(path)) {
            path = "file:" + path;
        }

        Resource resource = resourceLoader.getResource(path);
        File f = resource.getFile();
        Collection<File> importResources = new ArrayList<>();
        if (f.isDirectory()) {
            importResources.addAll(handleDirectory(resource));
        }

        return importResources;
    }

    private Collection<File> handleDirectory(Resource resource) throws Exception {
        File file = resource.getFile();
        File[] files = file.listFiles();

        return Optional.ofNullable(files).map(f -> Arrays.stream(f).filter(File::isFile).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

}
