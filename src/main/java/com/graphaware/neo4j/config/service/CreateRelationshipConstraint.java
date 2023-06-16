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

import com.graphaware.neo4j.config.model.schema.ConstraintType;
import com.graphaware.neo4j.config.model.schema.PropertyType;
import com.graphaware.neo4j.config.model.schema.RelationshipConstraint;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CreateRelationshipConstraint {

    private final Logger LOG = LoggerFactory.getLogger(CreateRelationshipConstraint.class);

    private final Driver driver;

    private final RelationshipConstraint relationshipConstraint;

    public CreateRelationshipConstraint(Driver driver, RelationshipConstraint relationshipConstraint) {
        this.driver = driver;
        this.relationshipConstraint = relationshipConstraint;
    }

    public void createSchemaConstraintForRelationship(String databaseName) {
        relationshipConstraint.relationshipTypes().forEach(relType -> {
            var name = relationshipConstraint.name() != null ? relationshipConstraint.name().replace("$relationshipType", relType) : "rel_constraint_" + StringUtils.join(relationshipConstraint.properties(), "_").toLowerCase();
            var type = relationshipConstraint.type();
            List<String> properties = relationshipConstraint.properties()
                    .stream().map(p -> String.format("r.`%s`", p))
                    .toList();
            String propsString = StringUtils.join(properties, ",");
            String query = "CREATE CONSTRAINT %s IF NOT EXISTS FOR ()-[r:`%s`]-() REQUIRE (%s) IS %s".formatted(
                    name,
                    relType,
                    propsString,
                    type.equals(ConstraintType.PROPERTY_TYPE)
                            ? propertyTypeToCypher(relationshipConstraint.propertyType())
                            : type.name().replace("_", " "));
            LOG.info("Creating relationship constraint {}", query);
            try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
                session.run(query);
            }
        });
    }

    private String propertyTypeToCypher(PropertyType p) {
        return ":: %s".formatted(p.name().replace("_", " "));
    }
}
