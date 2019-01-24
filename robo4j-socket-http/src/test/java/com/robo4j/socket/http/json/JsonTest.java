package com.robo4j.socket.http.json;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonTest {

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
		JsonReader parser = new JsonReader(jsonBasicValueWithStringArray);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();
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

		assertEquals(42, map.get("number"));
		assertEquals(false, map.get("active"));
		assertEquals("no message", map.get("message"));
		assertEquals(0.42, map.get("floatNumber"));
		assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
		assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));

	}

	@Test
	void jsonBasicObjectArraysTest() {

		JsonReader parser = new JsonReader(jsonBasicObjectArrays);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();

		JsonDocument obj1 = (JsonDocument) resultObjectArray.get(0);
		JsonDocument obj2 = (JsonDocument) resultObjectArray.get(1);

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

		JsonReader parser = new JsonReader(jsonBasicObjectArraysAndStringMap);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
		Map<String, Object> simpleMap = ((JsonDocument) map.get("simpleMap")).getMap();

		JsonDocument obj1 = (JsonDocument) resultObjectArray.get(0);
		JsonDocument obj2 = (JsonDocument) resultObjectArray.get(1);

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
