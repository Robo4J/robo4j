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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonReaderArrayTests {

	private static final String jsonArrayStringOnly = "[\"one\", \"two\" , \"three\"]";
	private static final String jsonArrayIntegerOnly = "[ 1,2,3 ,4, 5 , 6,  7]";
	private static final String jsonArrayBooleanOnly = "[ true, false, false, false, true]";
	private static final String jsonArrayNumberOnly = "[ 0.2, 0.1, 0.3]";
	private static final String jsonArrayObjectOnly = "[{\"name\":\"name1\",\"value\":42}, {\"name\":\"name2\",\"value\":22}, "
			+ "{\"name\":\"name3\",\"value\":8}]";
	private static final String jsonArrayOfArraysStringsOnly = "[[\"one\" ,\"two\", \"three\"] , [\"one\",\"two\"],[\"one\"]]";
	private static final String jsonArrayOfArraysIntegersOnly = "[[ 1 , 2, 3] , [1,2],[1]]";
	private static final String jsonArrayOfArraysBooleansOnly = "[[ true, false, true] , [true, false],[true]]";
	private static final String jsonArrayOfArraysObjectsOnly = "[[ {\"name\":\"name11\",\"value\":11}, "
			+ "{\"name\":\"name12\",\"value\":12}, {\"name\":\"name13\",\"value\":13}] , [{\"name\":\"name21\",\"value\":21}, "
			+ "{\"name\":\"name22\",\"value\":22}],[{\"name\":\"name31\",\"value\":31}]]";

	private static final String jsonArrayOfArraysObjectsWithObjectWithArray = "[[ {\"name\":\"name11\",\"value\":11}, "
			+ "{\"name\":\"name12\",\"value\":12}, {\"name\":\"name13\",\"value\":13}] , [{\"name\":\"name21\",\"value\":21}, "
			+ "{\"name\":\"name22\",\"value\":22}],[{\"name\":\"name31\",\"value\":31, \"array\":[31,32,33], "
			+ "\"child\":{\"name\":\"name311\", \"array\":[31,32,33]}}]]";

	@Test
	void basicArrayStringOnlyTest() {
		JsonReader parser = new JsonReader(jsonArrayStringOnly);
		JsonDocument document = parser.read();

		System.out.println("DOC: " + document);
		List<String> testArray = Arrays.asList("one", "two", "three");
		assertEquals(JsonDocument.Type.ARRAY, document.getType());
		assertEquals(testArray.size(), document.getArray().size());
		assertTrue(document.getArray().containsAll(testArray));
	}

	@Test
	void basicArrayBooleanOnlyTest() {
		JsonReader parser = new JsonReader(jsonArrayBooleanOnly);
		JsonDocument document = parser.read();

		List<Boolean> testArray = Arrays.asList(true, false, false, false, true);
		assertEquals(JsonDocument.Type.ARRAY, document.getType());
		assertEquals(testArray.size(), document.getArray().size());
		assertTrue(document.getArray().containsAll(testArray));
	}

	@Test
	void basicArrayIntegerOnlyTest() {
		JsonReader parser = new JsonReader(jsonArrayIntegerOnly);
		JsonDocument document = parser.read();

		System.out.println("DOC: " + document);
		List<Integer> testArray = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
		assertEquals(JsonDocument.Type.ARRAY, document.getType());
		assertEquals(testArray.size(), document.getArray().size());
		assertTrue(document.getArray().containsAll(testArray));
	}

	@Test
	void basicArrayNumberOnlyTest() {
		JsonReader parser = new JsonReader(jsonArrayNumberOnly);
		JsonDocument document = parser.read();

		List<Double> testArray = Arrays.asList(0.2, 0.1, 0.3);
		assertEquals(JsonDocument.Type.ARRAY, document.getType());
		assertEquals(testArray.size(), document.getArray().size());
		assertTrue(document.getArray().containsAll(testArray));
	}

	@Test
	void basicArrayObjectOnlyTest() {

		JsonDocument jsonDocumentChild1 = new JsonDocument(JsonDocument.Type.OBJECT);
		jsonDocumentChild1.put("name", "name1");
		jsonDocumentChild1.put("value", 42);

		JsonDocument jsonDocumentChild2 = new JsonDocument(JsonDocument.Type.OBJECT);
		jsonDocumentChild2.put("name", "name2");
		jsonDocumentChild2.put("value", 22);

		JsonDocument jsonDocumentChild3 = new JsonDocument(JsonDocument.Type.OBJECT);
		jsonDocumentChild3.put("name", "name3");
		jsonDocumentChild3.put("value", 8);

		List<JsonDocument> expectedArrayResult = Arrays.asList(jsonDocumentChild1, jsonDocumentChild2,
				jsonDocumentChild3);

		JsonReader parser = new JsonReader(jsonArrayObjectOnly);
		JsonDocument document = parser.read();

		System.out.println("DOC: " + document);
		assertEquals(JsonDocument.Type.ARRAY, document.getType());
		assertEquals(expectedArrayResult.size(), document.getArray().size());
		assertTrue(document.getArray().containsAll(expectedArrayResult));
	}

	@Test
	void basicArrayOfArrayStringOnlyTest() {

		List<Object> stringArray1 = Arrays.asList("one", "two", "three");
		List<Object> stringArray2 = Arrays.asList("one", "two");
		List<Object> stringArray3 = Collections.singletonList("one");
		JsonReader parser = new JsonReader(jsonArrayOfArraysStringsOnly);
		JsonDocument document = parser.read();

		assertTrue(compareArrays(stringArray1, document, 0));
		assertTrue(compareArrays(stringArray2, document, 1));
		assertTrue(compareArrays(stringArray3, document, 2));
		System.out.println("DOC: " + document);
	}

	@Test
	void basicArrayOfArraysIntegersOnlyTest() {
		List<Object> stringArray1 = Arrays.asList(1, 2, 3);
		List<Object> stringArray2 = Arrays.asList(1, 2);
		List<Object> stringArray3 = Collections.singletonList(1);

		JsonReader parser = new JsonReader(jsonArrayOfArraysIntegersOnly);
		JsonDocument document = parser.read();

		assertTrue(compareArrays(stringArray1, document, 0));
		assertTrue(compareArrays(stringArray2, document, 1));
		assertTrue(compareArrays(stringArray3, document, 2));
		System.out.println("DOC: " + document);
	}

	@Test
	void basicArrayOfArraysBooleanOnlyTest() {
		List<Object> stringArray1 = Arrays.asList(true, false, true);
		List<Object> stringArray2 = Arrays.asList(true, false);
		List<Object> stringArray3 = Collections.singletonList(true);

		JsonReader parser = new JsonReader(jsonArrayOfArraysBooleansOnly);
		JsonDocument document = parser.read();

		assertTrue(compareArrays(stringArray1, document, 0));
		assertTrue(compareArrays(stringArray2, document, 1));
		assertTrue(compareArrays(stringArray3, document, 2));
		System.out.println("DOC: " + document);
	}

	@Test
	void basicArrayOfArraysObjectsOnlyTest() {
		List<Object> array1 = Arrays.asList(testObjectJsonDocument(11), testObjectJsonDocument(12),
				testObjectJsonDocument(13));
		List<Object> array2 = Arrays.asList(testObjectJsonDocument(21), testObjectJsonDocument(22));
		List<Object> array3 = Collections.singletonList(testObjectJsonDocument(31));

		JsonReader parser = new JsonReader(jsonArrayOfArraysObjectsOnly);
		JsonDocument document = parser.read();

		assertTrue(compareArrays(array1, document, 0));
		assertTrue(compareArrays(array2, document, 1));
		assertTrue(compareArrays(array3, document, 2));

		System.out.println("DOC: " + document);
	}

	@Test
	void basicArrayOfArraysObjectsWithObjectWithArrayTest() {
		List<Object> array1 = Arrays.asList(testObjectJsonDocument(11), testObjectJsonDocument(12),
				testObjectJsonDocument(13));
		List<Object> array2 = Arrays.asList(testObjectJsonDocument(21), testObjectJsonDocument(22));

		JsonDocument lastJsonDocument = testObjectJsonDocument(31);
		JsonDocument lastJsonDocumentArray = new JsonDocument(JsonDocument.Type.ARRAY);
		lastJsonDocumentArray.add(31);
		lastJsonDocumentArray.add(32);
		lastJsonDocumentArray.add(33);
		JsonDocument lastJsonDocumentObject = new JsonDocument(JsonDocument.Type.OBJECT);
		lastJsonDocumentObject.put("name", "name311");
		lastJsonDocumentObject.put("array", lastJsonDocumentArray);
		lastJsonDocument.put("array", lastJsonDocumentArray);
		lastJsonDocument.put("child", lastJsonDocumentObject);
		List<Object> array3 = Collections.singletonList(lastJsonDocument);

		JsonReader parser = new JsonReader(jsonArrayOfArraysObjectsWithObjectWithArray);
		long start = System.nanoTime();
		JsonDocument document = parser.read();
		TimeUtils.printTimeDiffNano("robo4j:", start);

		assertTrue(compareArrays(array1, document, 0));
		assertTrue(compareArrays(array2, document, 1));
		assertTrue(compareArrays(array3, document, 2));

		System.out.println("DOC: " + document);
	}

	private boolean compareArrays(List<Object> sourceArray, JsonDocument arrayDocument, int index) {
		JsonDocument desiredArrayDocument = (JsonDocument) arrayDocument.getArray().get(index);
		assertEquals(JsonDocument.Type.ARRAY, desiredArrayDocument.getType());
		return sourceArray.containsAll(desiredArrayDocument.getArray());
	}

	private JsonDocument testObjectJsonDocument(Integer value) {
		JsonDocument result = new JsonDocument(JsonDocument.Type.OBJECT);
		result.put("name", "name" + value);
		result.put("value", value);
		return result;
	}
}
