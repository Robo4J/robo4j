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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonReaderTests {

	private static final String jsonBooleanValues = "{\"number\":22,\"message\":\"no message\",\"active\":true,\"passive\": false, \"bool1\":false,\"bool2\" :true}";
	private static final String jsonBasicValues = "{ \"number\"\n :  42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : 0.42}";
	private static final String jsonBasicMinusValues = "{ \"number\"\n :  -42, \"message\" \t: \"no message\", \"active\" : false , \"floatNumber\" : -0.42}";
	private static final String jsonBasicValuesWithNullValues = "{ \"number\"\n :  42, \"message\" \t: \"no message\","
			+ " \"active\" : false , \"floatNumber\" : 0.42, \"empty1\":null, \"empty2\" : null }";
	private static final String jsonBasicValueWithStringArray = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, "
			+ "\"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}";
	private static final String jsonBasicValueWithStringAndIntegerArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, "
			+ "\"active\" : false, \"message\" \t: \"no message\", \"arrayOne\":[\"one\", \"two\"], \"arrayTwo\" : [1, 2 ,3 ]}";
	private static final String jsonBasicValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, "
			+ "\"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], "
			+ "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}], \"active\" : false}";

	private static final String jsonBasicValueAndObjectValueWithStringAndIntegerAndObjectArrays = "{ \"floatNumber\" : 0.42, \"number\"\n :  42, "
			+ "\"arrayOne\":[\"one\", \"two\"], \"message\" \t: \"no message\", \"arrayTwo\" : [1, 2 ,3 ], "
			+ "\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}], \"active\" : false,"
			+ "\"child\" :{\"name\":\"name11\",\"value\":1} }";

	private static final String jsonArrayIntegerStringObject = "{\"arrayString\" : [\"one\", \"two\",  \"three\"], \"arrayInteger\":[1,2,3]}";

	private static final String jsonNestedObjects = "{\"value\" : { \"floatNumber\" : 0.42, \"object1\" : {\"name\" : \"some\"}}}";
	private static final String jsonNestedObjectAndArrayObject = "{\"arrayThree\" : [{\"name\":\"name1\",\"value\": 22}, {\"name\":\"name2\",\"value\": 42}]}";
	private static final String jsonNestedObjectWithBasicValueWithStringArray = "{\"value\" : { \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, "
			+ "\"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}}";

	private static final String jsonNestedObject2BasicValueWithStringArray = "{\"name\" : \"nestedName1\", \"object1\" : {\"value\" : "
			+ "{ \"floatNumber\" : 0.42, \"number\"\n :  42, \"active\" : false, \"message\" \t: \"no message\", \"arrayOne\":[\"one\",\"two\"]}}}";

	@Test
	void basicBooleanValuesTest() {

		long start = System.nanoTime();
		JsonReader parser = new JsonReader(jsonBooleanValues);
		JsonDocument document = parser.read();
		TimeUtils.printTimeDiffNano("basicBooleanValues robo4j", start);

		System.out.println("DOC: " + document);

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(22, document.getKey("number"));
		assertEquals("no message", document.getKey("message"));
		assertEquals(true, document.getKey("active"));
		assertEquals(false, document.getKey("passive"));
		assertEquals(false, document.getKey("bool1"));
		assertEquals(true, document.getKey("bool2"));

	}

	@Test
	public void basicAndNullValues() {
		JsonReader parser = new JsonReader(jsonBasicValuesWithNullValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		System.out.println("DOC: " + document);
		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals("no message", map.get("message"));
		assertEquals(false, map.get("active"));
		assertEquals(0.42, map.get("floatNumber"));
		assertNull(map.get("empty1"));
		assertNull(map.get("empty2"));
	}

	@Test
	void basicArrayIntegerStringTest() {
		JsonReader parser = new JsonReader(jsonArrayIntegerStringObject);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		List<Object> arrayString = ((JsonDocument) map.get("arrayString")).getArray();
		List<Object> arrayInteger = ((JsonDocument) map.get("arrayInteger")).getArray();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertTrue((Arrays.asList("one", "two", "three").containsAll(arrayString)));
		assertTrue((Arrays.asList(1, 2, 3).containsAll(arrayInteger)));
		System.out.println("DOC: " + document);
	}

	@Test
	void basicNestedObjectWithStringArray() {

		JsonDocument nestedJsonDocument2 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument2.put("name", "some");

		JsonDocument nestedJsonDocument1 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument1.put("floatNumber", 0.42);
		nestedJsonDocument1.put("object1", nestedJsonDocument2);

		JsonReader parser = new JsonReader(jsonNestedObjects);
		JsonDocument document = parser.read();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(nestedJsonDocument1, document.getKey("value"));

		System.out.println("document: " + document);

	}

	@Test
	void basicNestedObjectValuesAndObjectArray() {

		JsonDocument nestedJsonDocument21 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument21.put("name", "name1");
		nestedJsonDocument21.put("value", 22);

		JsonDocument nestedJsonDocument22 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument22.put("name", "name2");
		nestedJsonDocument22.put("value", 42);

		JsonDocument nestedJsonDocument2 = new JsonDocument(JsonDocument.Type.ARRAY);
		nestedJsonDocument2.add(nestedJsonDocument21);
		nestedJsonDocument2.add(nestedJsonDocument22);

		JsonDocument nestedJsonDocument1 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument1.put("arrayThree", nestedJsonDocument2);

		JsonReader parser = new JsonReader(jsonNestedObjectAndArrayObject);
		JsonDocument document = parser.read();

		JsonDocument arrayJsonDocument = (JsonDocument) document.getKey("arrayThree");

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(JsonDocument.Type.ARRAY, arrayJsonDocument.getType());
		assertEquals(nestedJsonDocument2, document.getKey("arrayThree"));
		System.out.println("document: " + document);
	}

	@Test
	void basicNestedObjectWithBasicValueWithStringArray() {

		JsonDocument nestedJsonDocument2 = new JsonDocument(JsonDocument.Type.ARRAY);
		nestedJsonDocument2.add("one");
		nestedJsonDocument2.add("two");

		JsonDocument nestedJsonDocument1 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument1.put("floatNumber", 0.42);
		nestedJsonDocument1.put("number", 42);
		nestedJsonDocument1.put("active", false);
		nestedJsonDocument1.put("message", "no message");
		nestedJsonDocument1.put("arrayOne", nestedJsonDocument2);

		JsonReader parser = new JsonReader(jsonNestedObjectWithBasicValueWithStringArray);
		JsonDocument document = parser.read();

		JsonDocument nestedArray = ((JsonDocument) ((JsonDocument) document.getKey("value")).getKey("arrayOne"));

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(nestedJsonDocument1, document.getKey("value"));
		assertEquals(JsonDocument.Type.ARRAY, nestedArray.getType());
		System.out.println("document: " + document);

	}

	@Test
	void basicNestedObject2WithBasicValueWithStringArray() {

		JsonDocument nestedJsonDocument4 = new JsonDocument(JsonDocument.Type.ARRAY);
		nestedJsonDocument4.add("one");
		nestedJsonDocument4.add("two");

		JsonDocument nestedJsonDocument3 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument3.put("floatNumber", 0.42);
		nestedJsonDocument3.put("number", 42);
		nestedJsonDocument3.put("active", false);
		nestedJsonDocument3.put("message", "no message");
		nestedJsonDocument3.put("arrayOne", nestedJsonDocument4);

		JsonDocument nestedJsonDocument2 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument2.put("value", nestedJsonDocument3);

		JsonDocument nestedJsonDocument1 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument1.put("name", "nestedName1");
		nestedJsonDocument1.put("object1", nestedJsonDocument2);

		JsonReader parser = new JsonReader(jsonNestedObject2BasicValueWithStringArray);
		JsonDocument document = parser.read();

		JsonDocument expectedNestedObject2 = (JsonDocument) document.getKey("object1");

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals("nestedName1", document.getKey("name"));
		assertEquals(expectedNestedObject2, nestedJsonDocument2);
		assertEquals(JsonDocument.Type.OBJECT, expectedNestedObject2.getType());
		System.out.println("document: " + document);
	}

	@Test
	void basicValuesJsonParse() {
		JsonReader parser = new JsonReader(jsonBasicValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals("no message", map.get("message"));
		assertEquals(false, map.get("active"));
		assertEquals(0.42, map.get("floatNumber"));
	}

	@Test
	void basicValuesMinusJsonParse() {
		JsonReader parser = new JsonReader(jsonBasicMinusValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(-42, map.get("number"));
		assertEquals("no message", map.get("message"));
		assertEquals(false, map.get("active"));
		assertEquals(-0.42, map.get("floatNumber"));
	}

	@Test
	void jsonBasicValuesAndStringArrayTest() {
		JsonReader parser = new JsonReader(jsonBasicValueWithStringArray);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals(false, map.get("active"));
		assertEquals("no message", map.get("message"));
		assertEquals(0.42, map.get("floatNumber"));
		List<Object> resultArray = ((JsonDocument) map.get("arrayOne")).getArray();
		assertTrue(Arrays.asList("one", "two").containsAll(resultArray));

		System.out.println("document: " + document);
	}

	@Test
	void jsonBasicValuesAndStringAndIntegerArraysTest() {
		JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerArrays);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals(false, map.get("active"));
		assertEquals("no message", map.get("message"));
		assertEquals(0.42, map.get("floatNumber"));
		assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
		assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));

		System.out.println("document: " + document);
	}

	@Test
	void jsonBasicValuesAndStringAndIntegerAndObjectArraysTest() {

		JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerAndObjectArrays);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
		JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals(false, map.get("active"));
		assertEquals("no message", map.get("message"));
		assertEquals(0.42, map.get("floatNumber"));
		assertEquals(2, resultObjectArray.size());
		assertEquals("name1", arrayObj1.getMap().get("name"));
		assertEquals(22, arrayObj1.getMap().get("value"));
		assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));
		assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));

	}

	@Test
	void jsonBasicValuesAndObjectValueAndStringAndIntegerAndObjectArraysTest() {

		JsonReader parser = new JsonReader(jsonBasicValueAndObjectValueWithStringAndIntegerAndObjectArrays);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
		JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);
		JsonDocument childObj = (JsonDocument) map.get("child");

		assertEquals(JsonDocument.Type.OBJECT, document.getType());
		assertEquals(42, map.get("number"));
		assertEquals(false, map.get("active"));
		assertEquals("no message", map.get("message"));
		assertEquals(0.42, map.get("floatNumber"));
		assertEquals(2, resultObjectArray.size());
		assertEquals("name1", arrayObj1.getMap().get("name"));
		assertEquals(22, arrayObj1.getMap().get("value"));
		assertEquals("name11", childObj.getMap().get("name"));
		assertEquals(1, childObj.getMap().get("value"));
		assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));
		assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));

	}
}
