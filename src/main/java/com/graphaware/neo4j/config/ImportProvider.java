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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ImportProvider {

    private final ResourceLoader resourceLoader;

    public ImportProvider(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    public Collection<File> getImportFiles(String path) throws Exception {

        if (path.startsWith("http")) {
            path = transformFromUrl(path);
        }

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

    private String transformFromUrl(String url) throws Exception {
        URL u = new URL(url);
        String tempDir = System.getProperty("java.io.tmpdir") + "/" + "neo4jconfig";
        String path = tempDir + "/" + System.currentTimeMillis() + ".json";

        File f = new File(path);
        log.info("copying file from {} copied to {}", url, path);
        FileUtils.copyURLToFile(u, f);

        return tempDir;
    }

}
