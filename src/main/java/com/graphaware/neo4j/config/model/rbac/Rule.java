package com.graphaware.neo4j.config.model.rbac;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class Rule {

    private String target;
    private String labels;
    private String action;
    private String resource;
    private RuleAccess access;
}
