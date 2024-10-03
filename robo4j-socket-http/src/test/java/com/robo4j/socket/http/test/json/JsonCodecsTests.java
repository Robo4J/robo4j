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

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.test.codec.CameraMessage;
import com.robo4j.socket.http.test.codec.CameraMessageCodec;
import com.robo4j.socket.http.test.codec.HttpPathDTOCodec;
import com.robo4j.socket.http.test.codec.NSBETypesAndCollectionTestMessageCodec;
import com.robo4j.socket.http.test.codec.NSBETypesTestMessageCodec;
import com.robo4j.socket.http.test.codec.NSBWithSimpleCollectionsTypesMessageCodec;
import com.robo4j.socket.http.dto.HttpPathMethodDTO;
import com.robo4j.socket.http.test.units.config.codec.NSBETypesAndCollectionTestMessage;
import com.robo4j.socket.http.test.units.config.codec.NSBETypesTestMessage;
import com.robo4j.socket.http.test.units.config.codec.NSBWithSimpleCollectionsTypesMessage;
import com.robo4j.socket.http.test.units.config.codec.TestPerson;
import com.robo4j.socket.http.test.units.config.enums.TestCommandEnum;
import com.robo4j.util.StreamUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class JsonCodecsTests {

	private static String testJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,"
			+ "\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"},"
			+ "\"persons\":[{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
			+ "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}],"
			+ "\"personMap\":{\"person1\":{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
			+ "\"child\":{\"name\":\"name111\",\"value\":42}}},\"person2\":{\"name\":\"name2\",\"value\":5}}}";

	private NSBWithSimpleCollectionsTypesMessageCodec collectionsTypesMessageCodec;
	private NSBETypesTestMessageCodec enumTypesMessageCodec;
	private NSBETypesAndCollectionTestMessageCodec collectionEnumTypesMessageCodec;
	private CameraMessageCodec cameraMessageCodec;
	private HttpPathDTOCodec httpPathDTOCodec;

	@BeforeEach
	void setUp() {
		collectionsTypesMessageCodec = new NSBWithSimpleCollectionsTypesMessageCodec();
		enumTypesMessageCodec = new NSBETypesTestMessageCodec();
		collectionEnumTypesMessageCodec = new NSBETypesAndCollectionTestMessageCodec();
		cameraMessageCodec = new CameraMessageCodec();
		httpPathDTOCodec = new HttpPathDTOCodec();
	}

	@SuppressWarnings("unchecked")
	@Test
	void encodeServerPathDTOMessageNoFilterTest() {
		// String expectedJson = "{\"roboUnit\":\"roboUnit1\",\"method\":\"GET\"}";
		HttpPathMethodDTO message = new HttpPathMethodDTO();
		message.setRoboUnit("roboUnit1");
		message.setMethod(HttpMethod.GET);
		message.setCallbacks(Collections.EMPTY_LIST);

		String resultJson = httpPathDTOCodec.encode(message);
		HttpPathMethodDTO decodedMessage = httpPathDTOCodec.decode(resultJson);

		System.out.println("resultJson: " + resultJson);
		System.out.println("decodedMessage: " + decodedMessage);

		// Assert.assertTrue(expectedJson.equals(resultJson));
		assertEquals(message, decodedMessage);
	}

	@Test
	void encodeMessageWithEnumTypeTest() {
		String expectedJson = "{\"number\":42,\"message\":\"enum type 1\",\"active\":true,\"command\":\"MOVE\"}";
		NSBETypesTestMessage message = new NSBETypesTestMessage();
		message.setNumber(42);
		message.setMessage("enum type 1");
		message.setActive(true);
		message.setCommand(TestCommandEnum.MOVE);

		String resultJson = enumTypesMessageCodec.encode(message);
		NSBETypesTestMessage decodedMessage = enumTypesMessageCodec.decode(resultJson);

		System.out.println("resultJson: " + resultJson);
		System.out.println("decodedMessage: " + decodedMessage);
		assertNotNull(resultJson);
		assertEquals(expectedJson, resultJson);
		assertEquals(message, decodedMessage);
	}

	@Test
	void encodeMessageWithEnumCollectionTypeTest() {

		String expectedJson = "{\"number\":42,\"message\":\"enum type 1\",\"active\":true,\"command\":\"MOVE\",\"commands\":[\"MOVE\",\"STOP\",\"BACK\"]}";
		NSBETypesAndCollectionTestMessage message = new NSBETypesAndCollectionTestMessage();
		message.setNumber(42);
		message.setMessage("enum type 1");
		message.setActive(true);
		message.setCommand(TestCommandEnum.MOVE);
		message.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));

		String resultJson = collectionEnumTypesMessageCodec.encode(message);
		NSBETypesAndCollectionTestMessage decodeMessage = collectionEnumTypesMessageCodec.decode(expectedJson);

		System.out.println("resultJson: " + resultJson);
		System.out.println("decodeMessage: " + decodeMessage);
		assertNotNull(resultJson);
		assertEquals(expectedJson, resultJson);
		assertEquals(message, decodeMessage);
	}

	@Test
	void nestedObjectToJson() {

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

		long start = System.nanoTime();
		String json = collectionsTypesMessageCodec.encode(obj1);
		TimeUtils.printTimeDiffNano("decodeFromJson", start);
		System.out.println("JSON1: " + json);

		assertTrue(testJson.equals(json));

	}

	@Test
	void nestedJsonToObject() {

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

		long start = System.nanoTime();
		NSBWithSimpleCollectionsTypesMessage obj1 = collectionsTypesMessageCodec.decode(testJson);
		TimeUtils.printTimeDiffNano("decodeFromJson", start);

		assertEquals(Integer.valueOf(42), obj1.getNumber());
		assertEquals("no message", obj1.getMessage());
		assertTrue(!obj1.getActive());
		assertArrayEquals(new String[] { "one", "two" }, obj1.getArray());
		assertTrue(obj1.getList().containsAll(Arrays.asList("text1", "text2")));
		assertEquals(personMap, obj1.getPersonMap());

		System.out.println("Obj: " + obj1);
	}

	@Test
	void cameraCodecJsonCycleTest() {

		final byte[] imageBytes = StreamUtils.inputStreamToByteArray(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("snapshot.png"));
		String encodeString = new String(Base64.getEncoder().encode(imageBytes));

		CameraMessage cameraMessage = new CameraMessage();
		cameraMessage.setImage(encodeString);
		cameraMessage.setType("jpg");
		cameraMessage.setValue("22");

		long start = System.nanoTime();
		String cameraJson0 = cameraMessageCodec.encode(cameraMessage);
		TimeUtils.printTimeDiffNano("cameraJson0", start);

		start = System.nanoTime();
		cameraMessageCodec.decode(cameraJson0);
		TimeUtils.printTimeDiffNano("decodeCameraMessage0", start);

		start = System.nanoTime();
		String cameraJson = cameraMessageCodec.encode(cameraMessage);
		TimeUtils.printTimeDiffNano("decodeFromJson", start);
		System.out.println("cameraJson: " + cameraJson);

		start = System.nanoTime();
		CameraMessage codecCameraMessage = cameraMessageCodec.decode(cameraJson);
		TimeUtils.printTimeDiffNano("decodeFromJson", start);

		assertEquals(cameraMessage, codecCameraMessage);

	}

}
