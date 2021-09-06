package com.graphaware.neo4j.config.model.rbac;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data @AllArgsConstructor @NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Role {

    private String name;
    private boolean dropIfExists;
    private List<Privilege> privileges;
}
