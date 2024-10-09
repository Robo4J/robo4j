/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.socket.http.test.json;

import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

import static com.robo4j.socket.http.test.json.JsonReaderTests.printDocument;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonTest.class);

    private static final String jsonBasicValues = "{ \"number\"\n :  42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : 0.42}";
    private static final String jsonBasicValueWithStringArray = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, "
            + "\"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}";
    private static final String jsonBasicValueWithStringAndIntegerArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, "
            + "\"active\" : false, \"message\" \t: \"no message\", \"arrayOne\":[\"one\", \"two\"], \"arrayTwo\" : [1, 2 ,3 ]}";
    private static final String jsonBasicValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, "
            + "\"active\" : false,  \"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], "
            + "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]}";

    private static final String jsonBasicObjectArrays = "{\"number\"\n :  42,\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}],"
            + " \"active\" : false}";

    private static final String jsonBasicObjectArraysAndStringMap = "{\"number\"\n :  42,"
            + "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}],"
            + " \"active\" : false, \"simpleMap\": {\"one\":\"one1\",\"two\":\"two2\"}}";

    @Test
    void basicValuesJsonParse() {
        JsonReader parser = new JsonReader(jsonBasicValues);
        JsonDocument document = parser.read();
        Map<String, Object> map = document.getMap();
        assertEquals(42, map.get("number"));
        assertEquals("no message", map.get("message"));
        assertEquals(false, map.get("active"));
        assertEquals(0.42, map.get("floatNumber"));
    }

    @Test
    void jsonBasicValuesAndStringArrayTest() {
        var parser = new JsonReader(jsonBasicValueWithStringArray);
        var document = parser.read();
        var map = document.getMap();
        var resultArray = ((JsonDocument) map.get("arrayOne")).getArray();

        printDocument(document);

        assertEquals(42, map.get("number"));
        assertEquals(false, map.get("active"));
        assertEquals("no message", map.get("message"));
        assertEquals(0.42, map.get("floatNumber"));
        assertTrue(Arrays.asList("one", "two").containsAll(resultArray));

    }

    @Test
    void jsonBasicValuesAndStringAndIntegerArraysTest() {
        var parser = new JsonReader(jsonBasicValueWithStringAndIntegerArrays);
        var document = parser.read();
        var map = document.getMap();
        var resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        var resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();

        printDocument(document);

        assertEquals(42, map.get("number"));
        assertEquals(false, map.get("active"));
        assertEquals("no message", map.get("message"));
        assertEquals(0.42, map.get("floatNumber"));
        assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));
    }

    @Test
    void jsonBasicValuesAndStringAndIntegerAndObjectArraysTest() {
        var parser = new JsonReader(jsonBasicValueWithStringAndIntegerAndObjectArrays);
        var document = parser.read();
        var map = document.getMap();
        var resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
        var resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();

        printDocument(document);

        assertEquals(42, map.get("number"));
        assertEquals(false, map.get("active"));
        assertEquals("no message", map.get("message"));
        assertEquals(0.42, map.get("floatNumber"));
        assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
        assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));

    }

    @Test
    void jsonBasicObjectArraysTest() {
        var parser = new JsonReader(jsonBasicObjectArrays);
        var document = parser.read();
        var map = document.getMap();
        var resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();

        JsonDocument obj1 = (JsonDocument) resultObjectArray.get(0);
        JsonDocument obj2 = (JsonDocument) resultObjectArray.get(1);

        printDocument(document);

        // [{"name":"name1","value": 22}, {"name":"name2","value": 42}]
        assertEquals(42, map.get("number"));
        assertEquals(false, map.get("active"));
        assertEquals("name1", obj1.getMap().get("name").toString());
        assertEquals(22, (int) obj1.getMap().get("value"));
        assertEquals("name2", obj2.getMap().get("name").toString());
        assertEquals(42, (int) obj2.getMap().get("value"));
        assertEquals(2, resultObjectArray.size());
    }

    @Test
    void jsonBasicObjectArraysAndSimpleMapTest() {
        var parser = new JsonReader(jsonBasicObjectArraysAndStringMap);
        var document = parser.read();
        var map = document.getMap();
        var resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
        var simpleMap = ((JsonDocument) map.get("simpleMap")).getMap();

        JsonDocument obj1 = (JsonDocument) resultObjectArray.get(0);
        JsonDocument obj2 = (JsonDocument) resultObjectArray.get(1);

        printDocument(document);

        // [{"name":"name1","value": 22}, {"name":"name2","value": 42}]
        assertEquals(42, map.get("number"));
        assertEquals(false, map.get("active"));
        assertEquals("name1", obj1.getMap().get("name").toString());
        assertEquals(22, (int) obj1.getMap().get("value"));
        assertEquals("name2", obj2.getMap().get("name").toString());
        assertEquals(42, (int) obj2.getMap().get("value"));
        assertEquals(2, resultObjectArray.size());
        assertEquals("one1", simpleMap.get("one"));
        assertEquals("two2", simpleMap.get("two"));

    }

}
