package com.graphaware.neo4j.config.model.rbac;

public record Rule(String target, String labels, String action, String resource, RuleAccess access) {
}
