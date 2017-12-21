package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.NSBTypesTestMessageCodec;
import com.robo4j.socket.http.codec.NSBWithSimpleCollectionsTypesMessageCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GenericCodecConversionCyclesTests {

	private NSBTypesTestMessageCodec  fieldTypesMessageCodec;
	private NSBWithSimpleCollectionsTypesMessageCodec collectionsTypesMessageCodec;

	@Before
	public void setUp(){
		fieldTypesMessageCodec = new NSBTypesTestMessageCodec();
		collectionsTypesMessageCodec = new NSBWithSimpleCollectionsTypesMessageCodec();
	}

//	@Test
//	public void castingTest(){
//		boolean test = false;
//		boolean value = ReflectUtils.adjustClassCast(boolean.class, test);
//		System.out.println("value: " + value);
//	}

	@Test
	public void genericClassCycleFromObjectToJsonWithNullExtractionTest() {
		int numberValue = 22;
		boolean isActive = true;
		String desiredJson = "{\"number\":" + numberValue + ",\"active\":" + isActive + "}";

		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(numberValue, null, isActive);

		String json = fieldTypesMessageCodec.encode(obj1);
		Assert.assertTrue(desiredJson.equals(json));
		System.out.println("JSON: " + json);
	}

	@Test
	public void genericClassCycleFromObjectToJsonExtractionTest() {
		int numberValue = 22;
		boolean isActive = true;
		String message = "some messge";
		String desiredJson = "{\"number\":" + numberValue + ",\"message\":\"" + message + "\",\"active\":" + isActive
				+ "}";
		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(numberValue, message, isActive);

		String json = fieldTypesMessageCodec.encode(obj1);
		Assert.assertTrue(desiredJson.equals(json));
		System.out.println("JSON: " + json);
	}

	@Test
	public void genericClassCycleFromObjectToJsonToObjectTest() {

		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, "some messge", true);

		String json = fieldTypesMessageCodec.encode(obj1);
		NSBTypesTestMessage createdObj = fieldTypesMessageCodec.decode(json);

		Assert.assertTrue(obj1.equals(createdObj));
		System.out.println("Result: " + createdObj);
	}

	@Test
	public void genericClassCycleFromObjectToJsonToObjectWithNulTest() {
		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, null, true);

		String json = fieldTypesMessageCodec.encode(obj1);
		NSBTypesTestMessage createdObj = fieldTypesMessageCodec.decode(json);

		Assert.assertTrue(obj1.equals(createdObj));
		System.out.println("json: " + json);
		System.out.println("Result: " + createdObj);
	}

	@Test
	public void genericClassCycleFromObjectToJsonToObjectWithArrayTest() {
		String desiredObjectToJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}}";
		NSBWithSimpleCollectionsTypesMessage obj1 = new NSBWithSimpleCollectionsTypesMessage();
		obj1.setNumber(42);
		obj1.setMessage("no message");
		obj1.setActive(false);
		obj1.setArray(new String[] { "one", "two" });
		obj1.setList(Arrays.asList("text1", "text2"));
		Map<String,String> mapStrings = new HashMap<>();
		mapStrings.put("key1", "value1");
		mapStrings.put("key2", "value2");
		mapStrings.put("key3", "value3");
		obj1.setMap(Collections.singletonMap("key", "value"));

		String json = collectionsTypesMessageCodec.encode(obj1);
		System.out.println("JSON:" + json);
		NSBWithSimpleCollectionsTypesMessage createdObj = collectionsTypesMessageCodec.decode(json);

		Assert.assertTrue(desiredObjectToJson.equals(json));
		Assert.assertTrue(createdObj.getMessage().equals(("no message")));
		Assert.assertTrue(!createdObj.getActive());
		Assert.assertTrue(createdObj.getPersons().isEmpty());
		Assert.assertTrue(createdObj.getPersonMap().isEmpty());
		System.out.println("createdObj: " + createdObj);
		System.out.println("json: " + json);

	}

	@Test
	public void testJson(){

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
	public void collectionNSBWithSimpleCollectionsTypesMessageNestedObject(){
		String json = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
				"\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, " +
				"\"persons\":[{\"name\":\"name1\",\"value\":22, \"child\":{\"name\":\"name11\",\"value\":0, " +
				"\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}], " +
				"\"personMap\":{\"key1\":\"value1\",\"key2,\":\"value2\"}}";

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

		Assert.assertTrue(createdObj.getNumber() == 42);
		Assert.assertTrue(!createdObj.getActive());
		Assert.assertTrue(Arrays.equals(createdObj.getArray(), new String[]{"one", "two"}));
		Assert.assertTrue(createdObj.getMap().equals(expectedMap));
		Assert.assertTrue(createdObj.getList().size() == expectedList.size());
		Assert.assertTrue(createdObj.getList().containsAll(expectedList));
		Assert.assertTrue(createdObj.getMessage().equals("no message"));
		Assert.assertTrue(createdObj.getPersons().get(0).equals(testPerson1));
		Assert.assertTrue(createdObj.getPersons().get(1).equals(testPerson2));
		Assert.assertTrue(createdObj.getPersons().get(0).getChild().equals(testPerson11));

		System.out.println("createdObj: " + createdObj);
	}


}
