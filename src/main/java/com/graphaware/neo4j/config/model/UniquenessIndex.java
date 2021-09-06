package com.graphaware.neo4j.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor
public class UniquenessIndex {

    private String name;
    private String label;
    private String property;
}
