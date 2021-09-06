package com.graphaware.neo4j.config.model.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor
public class Privilege {

    private String graph;
    private boolean access;
    private List<Rule> rules;
}
