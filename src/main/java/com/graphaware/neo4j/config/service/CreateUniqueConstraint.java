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

import com.graphaware.neo4j.config.model.rbac.UniqueConstraint;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

@Slf4j
public class CreateUniqueConstraint {

    private final Driver driver;
    private final UniqueConstraint uniqueConstraint;

    public CreateUniqueConstraint(Driver driver, UniqueConstraint uniqueConstraint) {
        this.driver = driver;
        this.uniqueConstraint = uniqueConstraint;
    }

    public void createUniqueConstraintOnDatabase(String databaseName) {
        String constraintName = uniqueConstraint.getName() != null
                ? uniqueConstraint.getName()
                : String.format("unique_%s_%s", uniqueConstraint.getLabel(), uniqueConstraint.getProperty()).toLowerCase();
        String q = String.format(
                "CREATE CONSTRAINT %s IF NOT EXISTS ON (n:`%s`) ASSERT n.`%s` IS UNIQUE",
                constraintName,
                uniqueConstraint.getLabel(),
                uniqueConstraint.getProperty()
        );

        log.debug("Query : {}", q);
        log.info("Creating unique constraint {}", constraintName);
        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }
}
