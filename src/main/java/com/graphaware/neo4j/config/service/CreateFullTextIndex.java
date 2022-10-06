package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.FullTextIndex;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateFullTextIndex {

    private static final Logger LOG = LoggerFactory.getLogger(CreateFullTextIndex.class);

    private final Driver driver;
    private final FullTextIndex fullTextIndex;

    public CreateFullTextIndex(Driver driver, FullTextIndex fullTextIndex) {
        this.driver = driver;
        this.fullTextIndex = fullTextIndex;
    }

    public void createFullTextIndexOnDatabase(String databaseName) {
        String labels = StringUtils.join(fullTextIndex.labels(), "|");
        List<String> properties = fullTextIndex.properties()
                .stream().map(p -> String.format("n.%s", p))
                .collect(Collectors.toList());
        String propsString = StringUtils.join(properties, ",");
        String indexName = fullTextIndex.name() != null ? fullTextIndex.name() : "fulltext_" + StringUtils.join(Stream.concat(fullTextIndex.labels().stream(), fullTextIndex.properties().stream()).collect(Collectors.toList()), "_").toLowerCase();
        String q = String.format("CREATE FULLTEXT INDEX %s IF NOT EXISTS FOR (n:%s) ON EACH [%s]", indexName, labels, propsString);
        LOG.debug("Query : {}", q);
        LOG.info("Creating fulltext index {}", indexName);

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }
}
