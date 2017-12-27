package com.robo4j.socket.http.json;

import com.google.gson.Gson;
import com.robo4j.socket.http.codec.NSBWithSimpleCollectionsTypesMessageCodec;
import com.robo4j.socket.http.units.test.codec.NSBWithSimpleCollectionsTypesMessage;
import com.robo4j.socket.http.units.test.codec.TestPerson;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class JsonToObjectTests {

	private static String testJson = "{\"number\":42,\"message\":\"no message\",\"active\":false,"
			+ "\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"},"
			+ "\"persons\":[{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
			+ "\"child\":{\"name\":\"name111\",\"value\":42}}},{\"name\":\"name2\",\"value\":5}],"
			+ "\"personMap\":{\"person1\":{\"name\":\"name1\",\"value\":22,\"child\":{\"name\":\"name11\",\"value\":0,"
			+ "\"child\":{\"name\":\"name111\",\"value\":42}}},\"person2\":{\"name\":\"name2\",\"value\":5}}}";

	private static String testJsonResult = "JSON1: {\"number\":42,\"message\":\"no message\",\"active\":false," +
			"\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}," +
			"\"persons\":[\"TestPerson{name='name1', value=22, child=TestPerson{name='name11', value=0, child=TestPerson{name='name111', value=42, child=null}}}\"," +
			"\"TestPerson{name='name2', value=5, child=null}\"]," +
			"\"personMap\":{\"person1\":\"TestPerson{name='name1', value=22, child=TestPerson{name='name11', value=0, " +
			"child=TestPerson{name='name111', value=42, child=null}}}\",\"person2\":\"TestPerson{name='name2', value=5, child=null}\"}}";
	private NSBWithSimpleCollectionsTypesMessageCodec collectionsTypesMessageCodec;

	private Gson gson;
	@Before
	public void setUp() {
		collectionsTypesMessageCodec = new NSBWithSimpleCollectionsTypesMessageCodec();
		gson = new Gson();
	}

	@Test
	public void jsonConvertFromJson() {

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


		long start = System.currentTimeMillis();
		String json = collectionsTypesMessageCodec.encode(obj1);
		System.out.println("JSON1: " + json);
//		Assert.assertTrue(json.equals(testJson));
		System.out.println("duration: " + timeDiff(start));


		start = System.currentTimeMillis();
		String gSon = gson.toJson(obj1);
		System.out.println("GSON1: " + gSon);
		System.out.println("duration: " + timeDiff(start));

		Assert.assertTrue(gSon.equals(json));

	}

	@Test
	public void jsonToObject() {

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

		NSBWithSimpleCollectionsTypesMessage obj1 = collectionsTypesMessageCodec.decode(testJson);

		Assert.assertTrue(obj1.getNumber() == 42);
		Assert.assertTrue(obj1.getMessage().equals("no message"));
		Assert.assertTrue(!obj1.getActive());
		Assert.assertTrue(Arrays.equals(obj1.getArray(), new String[] { "one", "two" }));
		Assert.assertTrue(obj1.getPersonMap().equals(personMap));

		System.out.println("Obj: " + obj1);

	}

	private long timeDiff(long start) {
		return System.currentTimeMillis() - start;
	}
}
