package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.FieldValueDTO;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
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
	public void simpleTest() {
		NSBTypesTestMessage ob1 = new NSBTypesTestMessage(22, "some message", false);
		NSBWithSimpleCollectionsTypesMessage ob2 = new NSBWithSimpleCollectionsTypesMessage();
		ob2.setActive(false);
		ob2.setList(Arrays.asList("text1", "text2"));
		ob2.setMessage("no message");
		ob2.setNumber(42);

		Map<String, FieldValueDTO> fieldsNSBTypes = ReflectUtils.getFieldsTypeValueMap(NSBTypesTestMessage.class, ob1);
		Map<String, FieldValueDTO> fieldsNSBWithList = ReflectUtils
				.getFieldsTypeValueMap(NSBWithSimpleCollectionsTypesMessage.class, ob2);
		System.out.println("fieldsNSBTypes: " + fieldsNSBTypes);
		System.out.println("fieldsNSBWithList: " + fieldsNSBWithList);
	}

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
		obj1.setArray(new String[]{"one", "two"});
		obj1.setList(Arrays.asList("text1", "text2"));
		obj1.setMap(Collections.singletonMap("key", "value"));

		Map<String, ClassGetSetDTO> fieldsNSBWithListTypes = ReflectUtils
				.getFieldsTypeMap(NSBWithSimpleCollectionsTypesMessage.class);

		String json = ReflectUtils.createJsonByFieldClassGetter(fieldsNSBWithListTypes, obj1);
		NSBWithSimpleCollectionsTypesMessage createdObj = ReflectUtils.createInstanceSetterByFieldMap(
				NSBWithSimpleCollectionsTypesMessage.class, fieldsNSBWithListTypes, JsonUtil.getMapByJson(json));

		System.out.println("json: " + json);
		System.out.println("createdObj: " + createdObj);
		Assert.assertTrue(desiredObjectToJson.equals(json));
		Assert.assertTrue(createdObj.equals(obj1));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void test(){
		Object[] obj = new String[]{"one", "two"};
		List<String> list = Stream.of(obj).map(String.class::cast).collect(Collectors.toList());
		System.out.println("test: " + list);


		String onlyArrayJson = "{\"array\": [\"one\",\"two\"]}";
		Map<String, Object> mapOnlyArrayJson = JsonUtil.getMapByJson(onlyArrayJson);
		System.out.println("mapOnlyArrayJson: " + mapOnlyArrayJson);

		List<String> arrayObject = (List<String> )mapOnlyArrayJson.get("array");
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
