package com.graphaware.neo4j.config.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.graphaware.neo4j.config.model.rbac.Constraints;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor @NoArgsConstructor @Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Database {

    private String name;

    private boolean dropIfExists;

    private Indexes indexes = new Indexes();

    private Constraints constraints = new Constraints();

    private List<String> seeds = new ArrayList<>();
}
