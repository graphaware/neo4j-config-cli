package com.graphaware.neo4j.config.model;

import com.graphaware.neo4j.config.model.schema.NodeIndex;
import com.graphaware.neo4j.config.model.schema.RelationshipIndex;

import java.util.List;
import java.util.Optional;

public record Indexes(List<NodeIndex> nodes, List<RelationshipIndex> relationships) {

    public Indexes {
        nodes = List.copyOf(nodes);
        relationships = List.copyOf(Optional.ofNullable(relationships).orElse(List.of()));
    }
}
