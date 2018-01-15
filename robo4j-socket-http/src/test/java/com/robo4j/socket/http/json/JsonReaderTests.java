package com.robo4j.socket.http.json;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonReaderTests {

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
	public void basicBooleanValuesTest() {

		long start = System.nanoTime();
		JsonReader parser = new JsonReader(jsonBooleanValues);
		JsonDocument document = parser.read();
		TimeUtils.printTimeDiffNano("basicBooleanValues robo4j", start);

		System.out.println("DOC: " + document);

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(document.getKey("number").equals(22));
		Assert.assertTrue(document.getKey("message").equals("no message"));
		Assert.assertTrue(document.getKey("active").equals(true));
		Assert.assertTrue(document.getKey("passive").equals(false));
		Assert.assertTrue(document.getKey("bool1").equals(false));
		Assert.assertTrue(document.getKey("bool2").equals(true));

	}

	@Test
	public void basicAndNullValues() {
		JsonReader parser = new JsonReader(jsonBasicValuesWithNullValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		System.out.println("DOC: " + document);
		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
		Assert.assertTrue(map.get("empty1") == null);
		Assert.assertTrue(map.get("empty2") == null);
	}

	@Test
	public void basicArrayIntegerStringTest() {
		JsonReader parser = new JsonReader(jsonArrayIntegerStringObject);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		List<Object> arrayString = ((JsonDocument) map.get("arrayString")).getArray();
		List<Object> arrayInteger = ((JsonDocument) map.get("arrayInteger")).getArray();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue((Arrays.asList("one", "two", "three").containsAll(arrayString)));
		Assert.assertTrue((Arrays.asList(1, 2, 3).containsAll(arrayInteger)));
		System.out.println("DOC: " + document);
	}

	@Test
	public void basicNestedObjectWithStringArray() {

		JsonDocument nestedJsonDocument2 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument2.put("name", "some");

		JsonDocument nestedJsonDocument1 = new JsonDocument(JsonDocument.Type.OBJECT);
		nestedJsonDocument1.put("floatNumber", 0.42);
		nestedJsonDocument1.put("object1", nestedJsonDocument2);

		JsonReader parser = new JsonReader(jsonNestedObjects);
		JsonDocument document = parser.read();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(document.getKey("value").equals(nestedJsonDocument1));

		System.out.println("document: " + document);

	}

	@Test
	public void basicNestedObjectValuesAndObjectArray() {

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

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(arrayJsonDocument.getType().equals(JsonDocument.Type.ARRAY));
		Assert.assertTrue(document.getKey("arrayThree").equals(nestedJsonDocument2));
		System.out.println("document: " + document);
	}

	@Test
	public void basicNestedObjectWithBasicValueWithStringArray() {

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

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(document.getKey("value").equals(nestedJsonDocument1));
		Assert.assertTrue(nestedArray.getType().equals(JsonDocument.Type.ARRAY));
		System.out.println("document: " + document);

	}

	@Test
	public void basicNestedObject2WithBasicValueWithStringArray() {

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

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(document.getKey("name").equals("nestedName1"));
		Assert.assertTrue(expectedNestedObject2.equals(nestedJsonDocument2));
		Assert.assertTrue(expectedNestedObject2.getType().equals(JsonDocument.Type.OBJECT));
		System.out.println("document: " + document);
	}

	@Test
	public void basicValuesJsonParse() {
		JsonReader parser = new JsonReader(jsonBasicValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
	}

	@Test
	public void basicValuesMinusJsonParse() {
		JsonReader parser = new JsonReader(jsonBasicMinusValues);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(-42));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("floatNumber").equals(-0.42));
	}

	@Test
	public void jsonBasicValuesAndStringArrayTest() {
		JsonReader parser = new JsonReader(jsonBasicValueWithStringArray);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
		List<Object> resultArray = ((JsonDocument) map.get("arrayOne")).getArray();
		Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultArray));

		System.out.println("document: " + document);
	}

	@Test
	public void jsonBasicValuesAndStringAndIntegerArraysTest() {
		JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerArrays);
		JsonDocument document = parser.read();
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
		Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
		Assert.assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));

		System.out.println("document: " + document);
	}

	@Test
	public void jsonBasicValuesAndStringAndIntegerAndObjectArraysTest() {

		JsonReader parser = new JsonReader(jsonBasicValueWithStringAndIntegerAndObjectArrays);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
		JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
		Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
		Assert.assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));
		Assert.assertTrue(resultObjectArray.size() == 2);
		Assert.assertTrue(arrayObj1.getMap().get("name").equals("name1"));
		Assert.assertTrue(arrayObj1.getMap().get("value").equals(22));

	}

	@Test
	public void jsonBasicValuesAndObjectValueAndStringAndIntegerAndObjectArraysTest() {

		JsonReader parser = new JsonReader(jsonBasicValueAndObjectValueWithStringAndIntegerAndObjectArrays);
		JsonDocument document = parser.read();
		System.out.println("document: " + document);
		Map<String, Object> map = document.getMap();
		List<Object> resultStringArray = ((JsonDocument) map.get("arrayOne")).getArray();
		List<Object> resultIntegerArray = ((JsonDocument) map.get("arrayTwo")).getArray();
		List<Object> resultObjectArray = ((JsonDocument) map.get("arrayThree")).getArray();
		JsonDocument arrayObj1 = (JsonDocument) resultObjectArray.get(0);
		JsonDocument childObj = (JsonDocument) map.get("child");

		Assert.assertTrue(document.getType().equals(JsonDocument.Type.OBJECT));
		Assert.assertTrue(map.get("number").equals(42));
		Assert.assertTrue(map.get("active").equals(false));
		Assert.assertTrue(map.get("message").equals("no message"));
		Assert.assertTrue(map.get("floatNumber").equals(0.42));
		Assert.assertTrue(Arrays.asList("one", "two").containsAll(resultStringArray));
		Assert.assertTrue(Arrays.asList(1, 2, 3).containsAll(resultIntegerArray));
		Assert.assertTrue(resultObjectArray.size() == 2);
		Assert.assertTrue(arrayObj1.getMap().get("name").equals("name1"));
		Assert.assertTrue(arrayObj1.getMap().get("value").equals(22));
		Assert.assertTrue(childObj.getMap().get("name").equals("name11"));
		Assert.assertTrue(childObj.getMap().get("value").equals(1));

	}
}
