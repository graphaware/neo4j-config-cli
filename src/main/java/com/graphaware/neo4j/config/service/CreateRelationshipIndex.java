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

package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.schema.IndexType;
import com.graphaware.neo4j.config.model.schema.RelationshipIndex;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateRelationshipIndex {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNodeIndex.class);

    private final Driver driver;
    private final RelationshipIndex relationshipIndex;

    public CreateRelationshipIndex(Driver driver, RelationshipIndex relationshipIndex) {
        this.driver = driver;
        this.relationshipIndex = relationshipIndex;
    }

    public void createIndex(String databaseName) {
        if (relationshipIndex.type() != null && relationshipIndex.type().equals(IndexType.FULLTEXT)) {
            createFullTextRelationshipIndex(databaseName);
        } else {
            createSchemaIndexForRelationship(databaseName);
        }
    }

    private void createSchemaIndexForRelationship(String databaseName) {
        relationshipIndex.relationshipTypes().forEach(relType -> {
            var name = relationshipIndex.name() != null ? relationshipIndex.name().replace("$relationshipType", relType) : "rel_idx_" + StringUtils.join(relationshipIndex.properties(), "_").toLowerCase();
            var type = getTypeDependingOnDatabase();
            List<String> properties = relationshipIndex.properties()
                    .stream().map(p -> String.format("r.`%s`", p))
                    .toList();
            String propsString = StringUtils.join(properties, ",");
            String query = String.format("CREATE %s INDEX %s IF NOT EXISTS FOR ()-[r:`%s`]-() ON (%s)", type.name(), name, relType, propsString);
            LOG.info("Creating relationship index {}", query);
            try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
                session.run(query);
            }
        });
    }

    private void createFullTextRelationshipIndex(String databaseName) {
        String labels = StringUtils.join(relationshipIndex.relationshipTypes(), "|");
        List<String> properties = relationshipIndex.properties()
                .stream().map(p -> String.format("r.%s", p))
                .toList();
        String propsString = StringUtils.join(properties, ",");
        String indexName = relationshipIndex.name() != null ? relationshipIndex.name() : "fulltext_" + StringUtils.join(Stream.concat(relationshipIndex.relationshipTypes().stream(), relationshipIndex.properties().stream()).collect(Collectors.toList()), "_").toLowerCase();
        String q = String.format("CREATE FULLTEXT INDEX %s IF NOT EXISTS FOR ()-[r:%s]-() ON EACH [%s]", indexName, labels, propsString);
        LOG.info("Creating fulltext index {}", q);

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }

    private IndexType getTypeDependingOnDatabase() {
        if (relationshipIndex.type() == null) {
            return isNeo4j5() ? IndexType.RANGE : IndexType.BTREE;
        }

        if (relationshipIndex.type().equals(IndexType.BTREE) && isNeo4j5()) {
            return IndexType.RANGE;
        }

        return relationshipIndex.type();
    }

    private boolean isNeo4j5() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            var version = session.run("CALL dbms.components() YIELD versions RETURN versions[0] AS version").single().get("version").asString();

            return version.startsWith("5");
        }
    }
}
