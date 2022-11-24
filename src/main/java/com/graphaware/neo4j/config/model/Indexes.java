package com.graphaware.neo4j.config.model;

import java.util.List;

public record Indexes(List<FullTextIndex> fulltext, List<UniqueConstraint> uniqueness) {

    public Indexes {
        fulltext = List.copyOf(fulltext);
        uniqueness = List.copyOf(uniqueness);
    }
}
