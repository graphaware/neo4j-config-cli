package com.graphaware.neo4j.config.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor @NoArgsConstructor
public class Indexes {

    private List<FullTextIndex> fulltext = new ArrayList<>();
}
