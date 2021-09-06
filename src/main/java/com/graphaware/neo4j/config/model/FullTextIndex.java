package com.graphaware.neo4j.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FullTextIndex {

    private String name;
    private List<String> labels;
    private List<String> properties;
}
