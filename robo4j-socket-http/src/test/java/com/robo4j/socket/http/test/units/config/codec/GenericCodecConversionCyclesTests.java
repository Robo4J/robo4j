/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class GenericCodecConversionCyclesTests {

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
		System.out.println("JSON: " + json);
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
		System.out.println("JSON: " + json);
	}

	@Test
	void genericClassCycleFromObjectToJsonToObjectTest() {

		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, "some messge", true);

		String json = fieldTypesMessageCodec.encode(obj1);
		System.out.println("json: " + json);
		NSBTypesTestMessage createdObj = fieldTypesMessageCodec.decode(json);

		assertEquals(obj1, createdObj);
		System.out.println("Result: " + createdObj);
	}

	@Test
	void genericClassCycleFromObjectToJsonToObjectWithNulTest() {
		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, null, true);

		String json = fieldTypesMessageCodec.encode(obj1);
		NSBTypesTestMessage createdObj = fieldTypesMessageCodec.decode(json);

		assertEquals(obj1, createdObj);
		System.out.println("json: " + json);
		System.out.println("Result: " + createdObj);
	}

	@Test
	void genericClassCycleFromObjectToJsonToObjectWithArrayTest() {
		String desiredObjectToJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}}";
		NSBWithSimpleCollectionsTypesMessage obj1 = new NSBWithSimpleCollectionsTypesMessage();
		obj1.setNumber(42);
		obj1.setMessage("no message");
		obj1.setActive(false);
		obj1.setArray(new String[] { "one", "two" });
		obj1.setList(Arrays.asList("text1", "text2"));
		Map<String, String> mapStrings = new HashMap<>();
		mapStrings.put("key1", "value1");
		mapStrings.put("key2", "value2");
		mapStrings.put("key3", "value3");
		obj1.setMap(Collections.singletonMap("key", "value"));

		String json = collectionsTypesMessageCodec.encode(obj1);
		System.out.println("JSON:" + json);
		NSBWithSimpleCollectionsTypesMessage createdObj = collectionsTypesMessageCodec.decode(json);

		assertEquals(desiredObjectToJson, json);
		assertEquals("no message", createdObj.getMessage());
		assertTrue(!createdObj.getActive());
		System.out.println("createdObj: " + createdObj);
		System.out.println("json: " + json);

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

		Map<String, TestPerson> personMap = new LinkedHashMap<>();
		personMap.put("person1", testPerson1);
		personMap.put("person2", testPerson2);

		NSBWithSimpleCollectionsTypesMessage obj1 = new NSBWithSimpleCollectionsTypesMessage();
		obj1.setNumber(42);
		obj1.setMessage("no message");
		obj1.setActive(false);
		obj1.setArray(new String[] { "one", "two" });
		obj1.setList(Arrays.asList("text1", "text2"));
		obj1.setMap(Collections.singletonMap("key", "value"));
		obj1.setPersons(Arrays.asList(testPerson1, testPerson2));
		obj1.setPersonMap(personMap);

		String json = collectionsTypesMessageCodec.encode(obj1);
		System.out.println("JSON: " + json);
	}

	@Test
	void collectionNSBWithSimpleCollectionsTypesMessageNestedObject() {
		String json = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],"
				+ "\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, "
				+ "\"persons\":[{\"name\":\"name1\",\"value\":22, \"child\":{\"name\":\"name11\",\"value\":0, "
				+ "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}], "
				+ "\"personMap\":{\"key1\":\"value1\",\"key2,\":\"value2\"}}";

		System.out.println("JSON: " + json);
		NSBWithSimpleCollectionsTypesMessage createdObj = collectionsTypesMessageCodec.decode(json);

		List<String> expectedList = Arrays.asList("text1", "text2");

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

		Map<String, String> expectedMap = Collections.singletonMap("key", "value");

		assertEquals(Integer.valueOf(42), createdObj.getNumber());
		assertTrue(!createdObj.getActive());
		assertArrayEquals(new String[] { "one", "two" }, createdObj.getArray());
		assertEquals(expectedMap, createdObj.getMap());
		assertEquals(expectedList.size(), createdObj.getList().size());
		assertTrue(createdObj.getList().containsAll(expectedList));
		assertEquals("no message", createdObj.getMessage());
		assertEquals(testPerson1, createdObj.getPersons().get(0));
		assertEquals(testPerson2, createdObj.getPersons().get(1));
		assertEquals(testPerson11, createdObj.getPersons().get(0).getChild());

		System.out.println("createdObj: " + createdObj);
	}

}
