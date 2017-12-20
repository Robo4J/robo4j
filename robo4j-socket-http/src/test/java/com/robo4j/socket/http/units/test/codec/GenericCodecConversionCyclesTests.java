package com.robo4j.socket.http.units.test.codec;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.oracle.javafx.jmx.json.JSONReader;
import com.oracle.javafx.jmx.json.impl.JSONStreamReaderImpl;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.json.JsonParser;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;
import org.junit.Assert;
import org.junit.Test;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.StringReader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class GenericCodecConversionCyclesTests {


	@Test
	public void genericClassCycleFromObjectToJsonWithNullExtractionTest() {
		int numberValue = 22;
		boolean isActive = true;
		String desiredJson = "{\"number\":" + numberValue + ",\"active\":" + isActive + "}";

		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(numberValue, null, isActive);
		Map<String, ClassGetSetDTO> fieldsNSBTypes = ReflectUtils.getFieldsTypeMap(NSBTypesTestMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBTypes, obj1);
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
		Map<String, ClassGetSetDTO> fieldsNSBTypes = ReflectUtils.getFieldsTypeMap(NSBTypesTestMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBTypes, obj1);
		Assert.assertTrue(desiredJson.equals(json));
		System.out.println("JSON: " + json);
	}

	@Test
	public void genericClassCycleFromObjectToJsonToObjectTest() {
		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, "some messge", true);
		Map<String, ClassGetSetDTO> fieldsNSBTypes = ReflectUtils.getFieldsTypeMap(NSBTypesTestMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBTypes, obj1);

		NSBTypesTestMessage createdObj = ReflectUtils.createInstanceSetterByFieldMap(NSBTypesTestMessage.class,
				fieldsNSBTypes, JsonUtil.getMapByJson(json));
		Assert.assertTrue(obj1.equals(createdObj));
		System.out.println("Result: " + createdObj);
	}

	@Test
	public void genericClassCycleFromObjectToJsonToObjectWithNulTest() {
		NSBTypesTestMessage obj1 = new NSBTypesTestMessage(22, null, true);
		Map<String, ClassGetSetDTO> fieldsNSBTypes = ReflectUtils.getFieldsTypeMap(NSBTypesTestMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBTypes, obj1);

		NSBTypesTestMessage createdObj = ReflectUtils.createInstanceSetterByFieldMap(NSBTypesTestMessage.class,
				fieldsNSBTypes, JsonUtil.getMapByJson(json));
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

		Map<String, ClassGetSetDTO> fieldsNSBWithListTypes = ReflectUtils
				.getFieldsTypeMap(NSBWithSimpleCollectionsTypesMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBWithListTypes, obj1);
		System.out.println("JSON:" + json);
		NSBWithSimpleCollectionsTypesMessage createdObj = ReflectUtils.createInstanceSetterByFieldMap(
				NSBWithSimpleCollectionsTypesMessage.class, fieldsNSBWithListTypes, JsonUtil.getMapByJson(json));

		System.out.println("json: " + json);
		System.out.println("createdObj: " + createdObj);
		Assert.assertTrue(desiredObjectToJson.equals(json));
		Assert.assertTrue(createdObj.equals(obj1));
	}

	@Test
	public void extractObjectMapByJson() {
		String json = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\","
				+ "\"text2\"],\"map\":{\"key\":\"value\"},\"listTwo\":[{\"name\":\"magic1\",\"value\":22}, {\"name\":\"magic2\",\"value\":42}]}";

		Map<String, Object> extractedMap = JsonUtil.getMapByJson(json);
		System.out.println("extractedMap: " + extractedMap);

	}

	@Test
	public void extractObjectByJsonParser() {
		String jsonText1 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\","
				+ "\"text2\"],\"map\":{\"key\":\"value\"},\"listTwo\":[{\"name\":\"magic1\",\"value\":22}, {\"name\":\"magic2\",\"value\":42}]}";

		String jsonText2 = "{ \"name\" :\"magic\", \"number\": 42 }";
		JsonParser jsonParser = new JsonParser();
		jsonParser.parse(jsonText2);
	}

	@Test
	public void extractJsonMapJavaFX() throws Exception {
		String desiredObjectToJson1 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
				"\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}}";
		String desiredObjectToJson2 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
				"\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, " +
				"\"persons\":[{\"name\":\"miro\",\"value\":22},{\"name\":\"tanja\",\"value\":5}]}";

		String desiredObjectToJson3 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
				"\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, " +
				"\"persons\":[{\"name\":\"miro\",\"value\":22, \"child\":{\"name\":\"maximilian\",\"value\":0, " +
				"\"child\":{\"name\":\"magic\",\"value\":42}}},{\"name\":\"tanja\",\"value\":5}]}";

		String desiredObjectToJson4 = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"]," +
				"\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}, " +
				"\"persons\":[{\"name\":\"miro\",\"value\":22, \"child\":{\"name\":\"maximilian\",\"value\":0, " +
				"\"child\":{\"name\":\"magic\",\"value\":42}}},{\"name\":\"tanja\",\"value\":5}], " +
				"\"personMap\":{\"key1\":\"value1\",\"key2,\":\"value2\"}}";


		Map<String, ClassGetSetDTO> fieldsNSBWithListTypes = ReflectUtils
				.getFieldsTypeMap(NSBWithSimpleCollectionsTypesMessage.class);

		JSONReader jsonReader = new JSONStreamReaderImpl(new StringReader(desiredObjectToJson4));
		JSONDocument document = jsonReader.build();


		NSBWithSimpleCollectionsTypesMessage createdObj = ReflectUtils.createInstanceSetterByJSONDocument(
				NSBWithSimpleCollectionsTypesMessage.class, fieldsNSBWithListTypes, document);

		System.out.println("createdObj: " + createdObj);
		List<TestPerson> miroPerson = createdObj.getPersons();
		System.out.println("specificPerson: " + miroPerson.get(0));

	}

	@Test
	public void testNashornAPI() throws Exception {
		String json = "{\"number\":42,\"message\":\"no message\",\"active\":false,\"array\":[\"one\",\"two\"],\"list\":[\"text1\",\"text2\"],\"map\":{\"key\":\"value\"}}";

		ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");

		URL inputStream = Thread.currentThread().getContextClassLoader().getResource("jsonparser.js");
		byte[] bytes =Files.readAllBytes(Paths.get(inputStream.toURI()));

		engine.eval(new String(bytes));

		Object object = ((Invocable) engine).invokeFunction("parseJSON", json);
		System.out.println("MAGIC: " + object);
	}

	@Test
	public void collectionSignatureTest() throws Exception {
		String mapSignature = "Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;";

		Class<?>[] clases = ReflectUtils.getClassesBySignature(Map.class, mapSignature);

		System.out.println("Classes:" + Arrays.asList(clases));

	}

	@Test
	@SuppressWarnings("unchecked")
	public void test() {
		Object[] obj = new String[] { "one", "two" };
		List<String> list = Stream.of(obj).map(String.class::cast).collect(Collectors.toList());
		System.out.println("test: " + list);

		String onlyArrayJson = "{\"array\": [\"one\",\"two\"]}";
		Map<String, Object> mapOnlyArrayJson = JsonUtil.getMapByJson(onlyArrayJson);
		System.out.println("mapOnlyArrayJson: " + mapOnlyArrayJson);

		List<String> arrayObject = (List<String>) mapOnlyArrayJson.get("array");
		Assert.assertTrue(arrayObject.contains("one"));
		Assert.assertTrue(arrayObject.contains("two"));

		String onlyMapJson = "{\"map\":{\"key\":\"value\"}}";
		Map<String, Object> mapOnlyMapJson = JsonUtil.getMapByJson(onlyMapJson);
		System.out.println("mapOnlyMapJson: " + mapOnlyMapJson);

	}

	// public static Constructor<?> findConstructorByClassAndParameters(Class<?>
	// clazz, List<Class<?>> constructorTypes) {
	// Constructor[] constructors = clazz.getConstructors();
//		//@formatter:off
//        Optional<Constructor> optionalConstructor =  Stream.of(constructors)
//                .filter(c -> c.getParameterTypes().length == constructorTypes.size())
//                .filter(c -> constructorTypes.containsAll(Arrays.asList(c.getParameterTypes())))
//                .findFirst();
//	     if(optionalConstructor.isPresent()){
//	         return optionalConstructor.get();
//	     } else {
//	         throw new RoboReflectException("not valid constructor");
//	     }
//        //@formatter:on
	// }
}
