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
package com.robo4j.socket.http.test.units.config.codec;

import com.robo4j.socket.http.test.codec.NSBTypesTestMessageCodec;
import com.robo4j.socket.http.test.codec.NSBWithSimpleCollectionsTypesMessageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class GenericCodecConversionCyclesTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericCodecConversionCyclesTests.class);
    private NSBTypesTestMessageCodec fieldTypesMessageCodec;
    private NSBWithSimpleCollectionsTypesMessageCodec collectionsTypesMessageCodec;

    @BeforeEach
    void setUp() {
        fieldTypesMessageCodec = new NSBTypesTestMessageCodec();
        collectionsTypesMessageCodec = new NSBWithSimpleCollectionsTypesMessageCodec();
    }

    @Test
    void genericClassCycleFromObjectToJsonWithNullExtractionTest() {
        int numberValue = 22;
        boolean isActive = true;
        String desiredJson = "{\"number\":" + numberValue + ",\"active\":" + isActive + "}";

        NSBTypesTestMessage obj1 = new NSBTypesTestMessage(numberValue, null, isActive);

        String json = fieldTypesMessageCodec.encode(obj1);
        assertEquals(desiredJson, json);
        printJson(json);
    }

    @Test
    void genericClassCycleFromObjectToJsonExtractionTest() {
        int numberValue = 22;
        boolean isActive = true;
        String message = "some messge";
        String desiredJson = "{\"number\":" + numberValue + ",\"message\":\"" + message + "\",\"active\":" + isActive
                + "}";
        NSBTypesTestMessage obj1 = new NSBTypesTestMessage(numberValue, message, isActive);

        String json = fieldTypesMessageCodec.encode(obj1);
        assertEquals(desiredJson, json);
        printJson(json);
    }

    @Test
    void genericClassCycleFromObjectToJsonToObjectTest() {

        NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, "some messge", true);

        String json = fieldTypesMessageCodec.encode(obj1);
        NSBTypesTestMessage createdObj = fieldTypesMessageCodec.decode(json);

        printJson(json);
        LOGGER.info("Result: {}", createdObj);
        assertEquals(obj1, createdObj);
    }

    @Test
    void genericClassCycleFromObjectToJsonToObjectWithNulTest() {
        var obj1 = new NSBTypesTestMessage(22, null, true);
        var json = fieldTypesMessageCodec.encode(obj1);
        var createdObj = fieldTypesMessageCodec.decode(json);

        printJson(json);
        printObject(createdObj);

        assertEquals(obj1, createdObj);
    }

    @Test
    void genericClassCycleFromObjectToJsonToObjectWithArrayTest() {
        String desiredObjectToJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}}";
        NSBWithSimpleCollectionsTypesMessage obj1 = new NSBWithSimpleCollectionsTypesMessage();
        obj1.setNumber(42);
        obj1.setMessage("no message");
        obj1.setActive(false);
        obj1.setArray(new String[]{"one", "two"});
        obj1.setList(Arrays.asList("text1", "text2"));
        obj1.setMap(Collections.singletonMap("key", "value"));

        String json = collectionsTypesMessageCodec.encode(obj1);
        var createdObj = collectionsTypesMessageCodec.decode(json);

        printJson(json);
        printObject(createdObj);

        assertEquals(desiredObjectToJson, json);
        assertEquals("no message", createdObj.getMessage());
        assertFalse(createdObj.getActive());

    }

    @Test
    void testJson() {
        TestPerson testPerson2 = new TestPerson();
        testPerson2.setName("name2");
        testPerson2.setValue(5);

        TestPerson testPerson111 = new TestPerson();
        testPerson111.setName("name111");
        testPerson111.setValue(42);

        TestPerson testPerson11 = new TestPerson();
        testPerson11.setName("name11");
        testPerson11.setValue(0);
        testPerson11.setChild(testPerson111);

        TestPerson testPerson1 = new TestPerson();
        testPerson1.setName("name1");
        testPerson1.setValue(22);
        testPerson1.setChild(testPerson11);

        var personMap = new LinkedHashMap<String, TestPerson>();
        personMap.put("person1", testPerson1);
        personMap.put("person2", testPerson2);

        var obj1 = new NSBWithSimpleCollectionsTypesMessage();
        obj1.setNumber(42);
        obj1.setMessage("no message");
        obj1.setActive(false);
        obj1.setArray(new String[]{"one", "two"});
        obj1.setList(Arrays.asList("text1", "text2"));
        obj1.setMap(Collections.singletonMap("key", "value"));
        obj1.setPersons(Arrays.asList(testPerson1, testPerson2));
        obj1.setPersonMap(personMap);
        String json = collectionsTypesMessageCodec.encode(obj1);

        printJson(json);
    }

    @Test
    void collectionNSBWithSimpleCollectionsTypesMessageNestedObject() {
        String json = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],"
                + "\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, "
                + "\"persons\":[{\"name\":\"name1\",\"value\":22, \"child\":{\"name\":\"name11\",\"value\":0, "
                + "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}], "
                + "\"personMap\":{\"key1\":\"value1\",\"key2,\":\"value2\"}}";

        var createdObj = collectionsTypesMessageCodec.decode(json);

        var expectedList = Arrays.asList("text1", "text2");

        TestPerson testPerson2 = new TestPerson();
        testPerson2.setName("name2");
        testPerson2.setValue(5);

        TestPerson testPerson111 = new TestPerson();
        testPerson111.setName("name111");
        testPerson111.setValue(42);

        TestPerson testPerson11 = new TestPerson();
        testPerson11.setName("name11");
        testPerson11.setValue(0);
        testPerson11.setChild(testPerson111);

        TestPerson testPerson1 = new TestPerson();
        testPerson1.setName("name1");
        testPerson1.setValue(22);
        testPerson1.setChild(testPerson11);

        var expectedMap = Collections.singletonMap("key", "value");

        printJson(json);
        printObject(createdObj);

        assertEquals(Integer.valueOf(42), createdObj.getNumber());
        assertFalse(createdObj.getActive());
        assertArrayEquals(new String[]{"one", "two"}, createdObj.getArray());
        assertEquals(expectedMap, createdObj.getMap());
        assertEquals(expectedList.size(), createdObj.getList().size());
        assertTrue(createdObj.getList().containsAll(expectedList));
        assertEquals("no message", createdObj.getMessage());
        assertEquals(testPerson1, createdObj.getPersons().get(0));
        assertEquals(testPerson2, createdObj.getPersons().get(1));
        assertEquals(testPerson11, createdObj.getPersons().get(0).getChild());
    }

    static void printJson(String json) {
        LOGGER.info("JSON: {}", json);
    }

    private static void printObject(Object obj) {
        LOGGER.info("Object: {}", obj);
    }
}
