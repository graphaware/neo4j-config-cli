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

import com.graphaware.neo4j.config.model.ConstraintType;
import com.graphaware.neo4j.config.model.UniqueConstraint;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUniqueConstraint {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUniqueConstraint.class);

    private final Driver driver;
    private final UniqueConstraint uniqueConstraint;

    public CreateUniqueConstraint(Driver driver, UniqueConstraint uniqueConstraint) {
        this.driver = driver;
        this.uniqueConstraint = uniqueConstraint;
    }

    public void createUniqueConstraintOnDatabase(String databaseName) {
        String constraintName = uniqueConstraint.name() != null
                ? uniqueConstraint.name()
                : String.format("unique_%s_%s", uniqueConstraint.label(), uniqueConstraint.property()).toLowerCase();
        String q = String.format(
                "CREATE CONSTRAINT %s IF NOT EXISTS FOR (n:`%s`) REQUIRE n.`%s` %s",
                constraintName,
                uniqueConstraint.label(),
                uniqueConstraint.property(),
                toConstraintTypeQueryString(uniqueConstraint.type())
        );

        LOG.debug("Query : {}", q);
        LOG.info("Creating unique constraint {}", constraintName);
        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }

    private String toConstraintTypeQueryString(ConstraintType constraintType) {
        return constraintType.name().toUpperCase().replace("_", " ");
    }
}
