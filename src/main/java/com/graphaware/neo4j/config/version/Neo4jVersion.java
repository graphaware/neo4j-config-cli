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

package com.graphaware.neo4j.config.version;

public enum Neo4jVersion {
    UNDEFINED,
    /**
     * Constant for Neo4j 4.4
     */
    V4_4,
    /**
     * Constant for Neo4j 5
     */
    V5;

    Neo4jVersion() {
    }

    public static Neo4jVersion of(String version) {
        if (version == null) {
            return UNDEFINED;
        } else if (version.startsWith("4.4")) {
            return V4_4;
        } else if (version.startsWith("5.")) {
            return V5;
        }

        throw new IllegalArgumentException(String.format("Unsupported Neo4j version %s", version));
    }
}