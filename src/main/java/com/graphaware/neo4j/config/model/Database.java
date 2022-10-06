package com.graphaware.neo4j.config.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.graphaware.neo4j.config.model.rbac.Constraints;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Database(String name, boolean dropIfExists, boolean skipIfCreate, Indexes indexes, Constraints constraints, List<String> seeds) {

    public Database {
        seeds = List.copyOf(seeds);
    }
}
