package com.graphaware.neo4j.config.model;

import java.util.List;

public record Indexes(List<FullTextIndex> fullTextIndices) {

    public Indexes {
        fullTextIndices = List.copyOf(fullTextIndices);
    }
}
