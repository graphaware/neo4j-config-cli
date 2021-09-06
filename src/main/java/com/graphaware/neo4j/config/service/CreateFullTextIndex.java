package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.FullTextIndex;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateFullTextIndex {

    private final Driver driver;
    private final FullTextIndex fullTextIndex;

    public CreateFullTextIndex(Driver driver, FullTextIndex fullTextIndex) {
        this.driver = driver;
        this.fullTextIndex = fullTextIndex;
    }

    public void createFullTextIndexOnDatabase(String databaseName) {
        String labels = StringUtils.join(fullTextIndex.getLabels(), "|");
        List<String> properties = fullTextIndex.getProperties()
                .stream().map(p -> String.format("n.%s", p))
                .collect(Collectors.toList());
        String propsString = StringUtils.join(properties, ",");
        String indexName = fullTextIndex.getName() != null ? fullTextIndex.getName() : "fulltext_" + StringUtils.join(Stream.concat(fullTextIndex.getLabels().stream(), fullTextIndex.getProperties().stream()).collect(Collectors.toList()), "_").toLowerCase();
        String q = String.format("CREATE FULLTEXT INDEX %s IF NOT EXISTS FOR (n:%s) ON EACH [%s]", indexName, labels, propsString);

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }
}
