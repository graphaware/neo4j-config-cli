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
import com.graphaware.neo4j.config.model.schema.NodeConstraint;
import com.graphaware.neo4j.config.model.schema.PropertyType;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class CreateNodeConstraint {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNodeConstraint.class);

    private final Driver driver;
    private final NodeConstraint nodeConstraint;

    public CreateNodeConstraint(Driver driver, NodeConstraint nodeConstraint) {
        this.driver = driver;
        this.nodeConstraint = nodeConstraint;
    }

    public void createConstraintOnDatabase(String databaseName) {
        nodeConstraint.labels().forEach(label -> {
            List<String> properties = nodeConstraint.properties()
                    .stream().map(p -> String.format("n.`%s`", p))
                    .toList();
            String propsString = StringUtils.join(properties, ",");
            String constraintName = nodeConstraint.name() != null
                    ? nodeConstraint.name().replace("$label", label)
                    : String.format("unique_%s_%s", label, StringUtils.join(nodeConstraint.properties(), "_")).toLowerCase();
            String query = String.format(
                    "CREATE CONSTRAINT %s IF NOT EXISTS FOR (n:`%s`) REQUIRE (%s) IS %s",
                    constraintName,
                    label,
                    propsString,
                    nodeConstraint.type().equals(ConstraintType.PROPERTY_TYPE)
                            ? propertyTypeToCypher(nodeConstraint.propertyType())
                            : nodeConstraint.type().getValue()
            );

            LOG.info("Creating unique constraint {}", query);
            try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
                session.run(query);
            }
        });
    }

    private String propertyTypeToCypher(PropertyType p) {
        return ":: %s".formatted(p.getValue().replace("_", " "));
    }
}
