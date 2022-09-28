package com.graphaware.neo4j.config.model.rbac;

import java.util.List;

public record Privilege(String graph, boolean access, List<Rule> rules) {

    public Privilege {
        rules = List.copyOf(rules);
    }
}
