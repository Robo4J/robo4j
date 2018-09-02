package com.robo4j.socket.http.util;

import com.robo4j.socket.http.dto.PathAttributeDTO;
import com.robo4j.socket.http.dto.PathAttributeListDTO;
import com.robo4j.socket.http.units.test.enums.TestCommandEnum;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ReflectUtilTests {

	@Test
	public void objectWithEnumToJson() {
		final String expectedJson = "{\"command\":\"MOVE\",\"desc\":\"some description\"}";
		TestCommand command = new TestCommand();
		command.setCommand(TestCommandEnum.MOVE);
		command.setDesc("some description");

		final String result = ReflectUtils.createJson(command);

		System.out.println("result: " + result);
		Assert.assertNotNull(result);
		Assert.assertTrue(result.equals(expectedJson));

	}

	@Test
	public void objectWithEnumListToJson() {

		final String expectedJson = "{\"commands\":[\"MOVE\",\"STOP\",\"BACK\"],\"desc\":\"commands description\"}";
		TestCommandList commands = new TestCommandList();
		commands.setCommands(Arrays.asList(TestCommandEnum.MOVE, TestCommandEnum.STOP, TestCommandEnum.BACK));
		commands.setDesc("commands description");

		final String result = ReflectUtils.createJson(commands);
		System.out.println("result: " + result);

		Assert.assertNotNull(result);
		Assert.assertEquals(expectedJson, result);

	}

	@Test
	public void pathAttributesListToJsonTest() {
		final String expectedJson = "{\"attributes\":[{\"name\":\"name\",\"value\":\"java.lang.String\"},{\"name\":\"values\",\"value\":\"java.util.HashMap\"}]}";
		PathAttributeListDTO attributes = new PathAttributeListDTO();
		attributes.addAttribute(new PathAttributeDTO("name", "java.lang.String"));
		attributes.addAttribute(new PathAttributeDTO("values", "java.util.HashMap"));

		String result = ReflectUtils.createJson(attributes);
		System.out.println("result:" + result);
		Assert.assertEquals(expectedJson, result);
	}

	@Test
    public void complexObjectToJsonTest(){
        String expectedJson = "{\"name\":\"object\",\"value\":42,\"textList\":[\"one\",\"two\"],\"dictionary\":{\"one\":\"one1\",\"two\":\"two2\"},\"attributes\":{\"one\":{\"name\":\"name\",\"value\":\"test name\"},\"two\":{\"name\":\"value\",\"value\":\"42\"}}}";
        TestListMapValues obj = new TestListMapValues();
        obj.setName("object");
        obj.setValue(42);
        obj.setTextList(Arrays.asList("one", "two"));
        Map<String, String> textMap = new HashMap<>();
        textMap.put("one", "one1");
        textMap.put("two", "two2");
        obj.setDictionary(textMap);


        Map<String, PathAttributeDTO> attributes = new HashMap<>();
        attributes.put("one", new PathAttributeDTO("name", "test name"));
        attributes.put("two", new PathAttributeDTO("value", "42"));
        obj.setAttributes(attributes);

        String result = ReflectUtils.createJson(obj);
        System.out.println("result:"+result);
        Assert.assertEquals(expectedJson, result);
    }

    // FIXME
    @Test
    public void createJsonByStringListCollectionTest() {
		String expectedJson = "[\"One\",\"Two\"]";
        List<String> list = new ArrayList<>();
        list.add("One");
        list.add("Two");

        String result = ReflectUtils.createJson(list);
        System.out.println("result: " + result);
        Assert.assertEquals(expectedJson, result);

    }

	@Test
	public void createJsonByNumberListCollectionTest() {
		String expectedJson = "[1,2]";
		List<Integer> list = new ArrayList<>();
		list.add(1);
		list.add(2);

		String result = ReflectUtils.createJson(list);
		System.out.println("result: " + result);
		Assert.assertEquals(expectedJson, result);

	}

	@Test(expected = RoboReflectException.class)
	public void createJsonByObjectListCollectionTest(){

		List<PathAttributeDTO> list = new ArrayList<>();
		list.add(new PathAttributeDTO("one", "1"));
		list.add(new PathAttributeDTO("two", "2"));
		String result = ReflectUtils.createJson(list);
		System.out.println("result: " + result);
	}

	@Test
	public void createJsonByStringMapCollection() {
		String expectedString = "{\"one\":\"1\",\"two\":\"2\"}";
		Map<String, String> map = new HashMap<>();
		map.put("one", "1");
		map.put("two", "2");

		String result = ReflectUtils.createJson(map);
		System.out.println("result: " + result);
		Assert.assertEquals(expectedString, result);

	}

	@Test
	public void createJsonByNumberMapCollection() {
		String expectedString = "{\"one\":1,\"two\":2}";
		Map<String, Integer> map = new HashMap<>();
		map.put("one", 1);
		map.put("two", 2);

		String result = ReflectUtils.createJson(map);
		System.out.println("result: " + result);
		Assert.assertEquals(expectedString, result);

	}


	@Test(expected = RoboReflectException.class)
	public void createJsonByObjectMapCollection(){
		Map<String, PathAttributeDTO> map = new HashMap<>();
		map.put("one", new PathAttributeDTO("one1", "1"));
		map.put("two", new PathAttributeDTO("two2", "2"));
		String result = ReflectUtils.createJson(map);
		System.out.println("result: " + result);
	}

}
