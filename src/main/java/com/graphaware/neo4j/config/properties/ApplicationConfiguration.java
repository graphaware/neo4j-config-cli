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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Value("${import.path}")
    private String importPath;

    @Value("${seed-only}")
    private boolean seedOnly;

    @Value("${seed-url:#{null}}")
    private String seedUrl;

    @Value("${seed-db}")
    private String seedDb;

    @Bean
    public ImportConfiguration importConfiguration() {
        return new ImportConfiguration(importPath, seedOnly, seedUrl, seedDb);
    }
}
