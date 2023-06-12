package com.graphaware.neo4j.config.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.graphaware.neo4j.config.model.schema.Constraints;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Database(String name, boolean dropIfExists, boolean skipIfCreate, Indexes indexes, Constraints constraints, List<String> seeds, String seedFromUri, boolean composite, List<String> constituents) {

    public Database {
        seeds = List.copyOf(Optional.ofNullable(seeds).orElse(List.of()));
        constituents = List.copyOf(Optional.ofNullable(constituents).orElse(List.of()));
    }
}
