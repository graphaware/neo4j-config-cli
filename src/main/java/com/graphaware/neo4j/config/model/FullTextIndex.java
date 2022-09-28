package com.graphaware.neo4j.config.model;

import java.util.List;

public record FullTextIndex(String name, List<String> labels, List<String> properties) {

    public FullTextIndex {
        labels = List.copyOf(labels);
        properties = List.copyOf(properties);
    }
}
