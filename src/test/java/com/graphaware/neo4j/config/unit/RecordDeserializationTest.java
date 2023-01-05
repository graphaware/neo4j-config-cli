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

package com.graphaware.neo4j.config.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graphaware.neo4j.config.model.Database;
import org.junit.jupiter.api.Test;

public class RecordDeserializationTest {

    @Test
    void test_mapping_json_with_empty_values_to_record() throws Exception {
        String json = """
                {
                  "kind": "Database",
                  "name": "world.cup",
                  "dropIfExists": "false",
                  "seedFromUri": "https://downloads.graphaware.com/neo4j-db-seeds/world-cup-2022-neo4j.backup",
                  "indexes": {
                    "nodes": []
                  }
                }""";

        var om = new ObjectMapper();
        var db = om.readValue(json, Database.class);
        System.out.println(db);
    }
}
