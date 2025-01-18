package com.graphaware.neo4j.config.unit;

import com.graphaware.neo4j.config.model.schema.PropertyType;
import org.junit.jupiter.api.Test;

public class PropertyTypesTest {

    @Test
    void test_lists() {
        var list_of_strings = PropertyType.LIST_OF_STRING;
        System.out.println(list_of_strings.getValue());

        var from_string = PropertyType.valueOf("LIST_OF_STRING");
        System.out.println(from_string);
    }
}
