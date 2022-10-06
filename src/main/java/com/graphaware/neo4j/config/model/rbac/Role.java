package com.graphaware.neo4j.config.model.rbac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Role(String name, boolean dropIfExists, List<Privilege> privileges) {

    public Role {
        privileges = List.copyOf(privileges);
    }
}
