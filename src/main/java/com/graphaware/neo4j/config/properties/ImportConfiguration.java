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

package com.graphaware.neo4j.config.properties;

public class ImportConfiguration {

    private final String importPath;
    private final boolean seedOnly;
    private final String seedUrl;
    private final String seedDb;

    public ImportConfiguration(String importPath, boolean seedOnly, String seedUrl, String seedDb) {
        this.importPath = importPath;
        this.seedOnly = seedOnly;
        this.seedUrl = seedUrl;
        this.seedDb = seedDb;
    }

    public String getImportPath() {
        return importPath;
    }

    public boolean isSeedOnly() {
        return seedOnly;
    }

    public String getSeedUrl() {
        return seedUrl;
    }

    public String getSeedDb() {
        return seedDb;
    }
}
