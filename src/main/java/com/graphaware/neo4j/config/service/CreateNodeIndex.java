package com.graphaware.neo4j.config.service;

import com.graphaware.neo4j.config.model.schema.IndexType;
import com.graphaware.neo4j.config.model.schema.NodeIndex;
import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CreateNodeIndex {

    private static final Logger LOG = LoggerFactory.getLogger(CreateNodeIndex.class);

    private final Driver driver;
    private final NodeIndex nodeIndex;

    public CreateNodeIndex(Driver driver, NodeIndex nodeIndex) {
        this.driver = driver;
        this.nodeIndex = nodeIndex;
    }

    public void createIndex(String databaseName) {
        if (nodeIndex.type().equals(IndexType.FULLTEXT)) {
            createFullTextIndexForNode(databaseName);
        } else {
            createSchemaIndexForNode(databaseName);
        }
    }

    private void createSchemaIndexForNode(String databaseName) {
        nodeIndex.labels().forEach(label -> {
            var name = nodeIndex.name() != null ? nodeIndex.name().replace("$label", label) : "node_idx_" + StringUtils.join(nodeIndex.properties(), "_").toLowerCase();
            var type = getTypeDependingOnDatabase();
            List<String> properties = nodeIndex.properties()
                    .stream().map(p -> String.format("n.`%s`", p))
                    .toList();
            String propsString = StringUtils.join(properties, ",");
            String query = String.format("CREATE %s INDEX %s IF NOT EXISTS FOR (n:`%s`) ON (%s)", type.name(), name, label, propsString);
            LOG.info("Creating node index {}", query);
            try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
                session.run(query);
            }
        });
    }

    private void createFullTextIndexForNode(String databaseName) {
        String labels = StringUtils.join(nodeIndex.labels(), "|");
        List<String> properties = nodeIndex.properties()
                .stream().map(p -> String.format("n.%s", p))
                .toList();
        String propsString = StringUtils.join(properties, ",");
        String indexName = nodeIndex.name() != null ? nodeIndex.name() : "fulltext_" + StringUtils.join(Stream.concat(nodeIndex.labels().stream(), nodeIndex.properties().stream()).collect(Collectors.toList()), "_").toLowerCase();
        String q = String.format("CREATE FULLTEXT INDEX %s IF NOT EXISTS FOR (n:%s) ON EACH [%s]", indexName, labels, propsString);
        LOG.info("Creating fulltext index {}", q);

        try (Session session = driver.session(SessionConfig.forDatabase(databaseName))) {
            session.run(q);
        }
    }

    private IndexType getTypeDependingOnDatabase() {
        if (nodeIndex.type() == null) {
            return isNeo4j5() ? IndexType.RANGE : IndexType.BTREE;
        }

        if (nodeIndex.type().equals(IndexType.BTREE) && isNeo4j5()) {
            return IndexType.RANGE;
        }

        return nodeIndex.type();
    }

    private boolean isNeo4j5() {
        try (Session session = driver.session(SessionConfig.forDatabase("system"))) {
            var version = session.run("CALL dbms.components() YIELD versions RETURN versions[0] AS version").single().get("version").asString();

            return version.startsWith("5");
        }
    }
}
