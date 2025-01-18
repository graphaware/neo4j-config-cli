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

package com.graphaware.neo4j.config.model.schema;

public enum PropertyType {

    BOOLEAN("BOOLEAN"),
    STRING("STRING"),
    INTEGER("INTEGER"),
    FLOAT("FLOAT"),
    DATE("DATE"),
    LOCAL_TIME("LOCAL TIME"),
    ZONED_TIME("ZONED TIME"),
    LOCAL_DATETIME("LOCAL DATETIME"),
    ZONED_DATETIME("ZONED DATETIME"),
    DURATION("DURATION"),
    POINT("POINT"),
    LIST_OF_BOOLEAN("LIST<BOOLEAN NOT NULL>"),
    LIST_OF_STRING("LIST<STRING NOT NULL>"),
    LIST_OF_INTEGER("LIST<INTEGER NOT NULL>"),
    LIST_OF_FLOAT("LIST<FLOAT NOT NULL>"),
    LIST_OF_DATETIME("LIST<DATETIME NOT NULL>"),
    LIST_OF_LOCAL_TIME("LIST<LOCAL TIME NOT NULL>"),
    LIST_OF_ZONED_TIME("LIST<ZONED TIME NOT NULL>"),
    LIST_OF_LOCAL_DATETIME("LIST<LOCAL DATETIME NOT NULL>"),
    LIST_OF_DATE("LIST<DATE NOT NULL>"),
    LIST_OF_DURATION("LIST<DURATION NOT NULL>"),
    LIST_OF_POINT("LIST<POINT NOT NULL>"),
    ;

    private final String value;

    PropertyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // Optional: Override toString() to return the string value
    @Override
    public String toString() {
        return value;
    }
}
