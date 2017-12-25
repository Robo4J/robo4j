package com.robo4j.socket.http.util;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.json.JsonDocument;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;
import static com.robo4j.util.Utf8Constant.UTF8_DOT;
import static com.robo4j.util.Utf8Constant.UTF8_QUOTATION_MARK;
import static com.robo4j.util.Utf8Constant.UTF8_SOLIDUS;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ReflectUtils {
	private static final Pattern ARRAY_PATTERN = Pattern.compile("L(.*);");
	private static final Pattern LIST_PATTERN = Pattern.compile("List<(.*)>");
	private static final Set<Class<?>> METHOD_IS_TYPES = Stream.of(boolean.class).collect(Collectors.toSet());
	private static final String METHOD_IS = "is";
	private static final String METHOD_GET = "get";
	private static final String METHOD_SET = "set";

	private static final Map<Class<?>, Map<String, ClassGetSetDTO>> clazzDescriptorMap = new HashMap<>();

	/**
	 * translate Object to proper JSON string
	 * 
	 * @param getterDTO
	 * @param obj
	 * @return
	 */
	public static String getJsonValue(ClassGetSetDTO getterDTO, Object obj) {
		try {
			Object value = getterDTO.getGetMethod().invoke(obj);
			//@formatter:off
			return value == null ? null : JsonUtil.WITHOUT_QUOTATION_TYPES.contains(getterDTO.getField().getType()) ? String.valueOf(value)
					: processObjectToJson(value);
			//@formatter:on
		} catch (Exception e) {
			throw new RoboReflectException("object getter value", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static String processObjectToJson(Object obj) {
		StringBuilder result = new StringBuilder();
		if (obj.getClass().isArray()) {
			Object[] objects = (Object[]) obj;
			result.append(UTF8_SQUARE_BRACKET_LEFT).append(
					Stream.of(objects).map(ReflectUtils::processObjectToJson).collect(Collectors.joining(UTF8_COMMA)))
					.append(UTF8_SQUARE_BRACKET_RIGHT);
		} else if (obj instanceof List<?>) {
			List<Object> objects = (List<Object>) obj;
			result.append(UTF8_SQUARE_BRACKET_LEFT).append(
					objects.stream().map(ReflectUtils::processObjectToJson).collect(Collectors.joining(UTF8_COMMA)))
					.append(UTF8_SQUARE_BRACKET_RIGHT);
		} else if (obj instanceof Map<?, ?>) {
			Map<Object, Object> objectMap = (Map<Object, Object>) obj;
			result.append(UTF8_CURLY_BRACKET_LEFT).append(objectMap.entrySet().stream()
					.map(entry -> new StringBuilder(ReflectUtils.processObjectToJson(entry.getKey())).append(UTF8_COLON)
							.append(ReflectUtils.processObjectToJson(entry.getValue())).toString())
					.collect(Collectors.joining(UTF8_COMMA))).append(UTF8_CURLY_BRACKET_RIGHT);
		} else {
			result.append(UTF8_QUOTATION_MARK).append(obj).append(UTF8_QUOTATION_MARK);
		}
		return result.toString();
	}

	private static Class<?> extractListClassSignature(String value) {
		return extractSimpleClassSignatureByPattern(LIST_PATTERN, value);
	}

	private static Class<?> extractArrayClassSignature(String value) {
		return extractSimpleClassSignatureByPattern(ARRAY_PATTERN, value);
	}

	private static Class<?> extractSimpleClassSignatureByPattern(Pattern pattern, String value) {
		Matcher matcher = pattern.matcher(value);
		if (matcher.find() && matcher.groupCount() == 1) {
			String classPath = matcher.group(1);
			try {
				return Class.forName(classPath);
			} catch (Exception e) {
				throw new RoboReflectException("problem", e);
			}
		}
		throw new RoboReflectException("not found: " + value);
	}


	@SuppressWarnings("unchecked")
	public static <T> T createInstanceSetterByRoboJsonDocument(Class<T> clazz,	JsonDocument jsonDocument) {

		final Map<String, ClassGetSetDTO> getterDTOMap = getFieldsTypeMap(clazz);
		try {
			Object instance = clazz.newInstance();
			getterDTOMap.entrySet().stream().filter(e -> Objects.nonNull(jsonDocument.getKey(e.getKey()))).forEach(e -> {
				try {
					ClassGetSetDTO value = e.getValue();
					value.getSetMethod().invoke(instance, adjustRoboJsonDocumentCast(value, jsonDocument, e.getKey()));
				} catch (Exception e1) {
					throw new RoboReflectException("create instance field", e1);
				}
			});
			return (T) instance;
		} catch (Exception e) {
			throw new RoboReflectException("create instance with setter", e);
		}

	}

	@SuppressWarnings("unchecked")
	public static <E> E[] listToArray(List<E> list) {
		int s;
		if (list == null || (s = list.size()) < 1)
			return null;
		E[] result;
		E typeHelper = list.get(0);

		try {
			Object o = Array.newInstance(typeHelper.getClass(), s);
			result = (E[]) o;

			for (int i = 0; i < list.size(); i++)
				Array.set(result, i, list.get(i));
		} catch (Exception e) {
			throw new RoboReflectException("to array conversion", e);
		}

		return result;
	}

	private static TypeCollection getClazzCollection(Class<?> clazz) {
		if(Collection.class.isAssignableFrom(clazz)){
			return TypeCollection.LIST;
		} else if(Map.class.isAssignableFrom(clazz)){
			return TypeCollection.MAP;
		} else {
			return null;
		}
	}


	private static Object adjustRoboJsonDocumentCast(ClassGetSetDTO classGetSetDTO, JsonDocument document, String key) {
		Class<?> clazz = classGetSetDTO.getField().getType();
		if (clazz.isArray()) {
			Class<?> arrayClass = extractArrayClassSignature(clazz.getName());
			// FIXME: 12/25/17 (miro) -> correct this
			List<?> list = ((JsonDocument)document.getKey(key)).getArray().stream().map(arrayClass::cast).collect(Collectors.toList());
			return listToArray(list);
		} else {
			final TypeMapper typeMapper = TypeMapper.getBySource(clazz);
			final TypeCollection typeCollection = getClazzCollection(clazz);

			if(typeCollection != null){
				switch (typeCollection){
					case LIST:
						Class<?> listClass = extractListClassSignature(
								classGetSetDTO.getField().getGenericType().getTypeName());
						return ((JsonDocument)document.getKey(key)).getArray().stream().map(e -> castObjectByRoboJsonDocument(listClass, e))
								.collect(Collectors.toList());
					case MAP:
						JsonDocument mapDocument = (JsonDocument) document.getKey(key);
						return mapDocument.getMap();
					default:
						throw new RoboReflectException("wrong collection" + typeCollection);
				}

			} else if (typeMapper != null) {
				return document.getKeyValueByType(typeMapper, key);
			}
		}
		throw new RoboReflectException("wrong adjustment");
	}

	@SuppressWarnings("unchecked")
	private static <T> T castObjectByRoboJsonDocument(Class<T> clazz, Object value) {
		if (clazz.equals(String.class) || JsonUtil.WITHOUT_QUOTATION_TYPES.contains(clazz)) {
			return (T) adjustRoboClassCast(clazz, value);
		} else {
			try {
				Object instance = clazz.newInstance();
				Map<String, ClassGetSetDTO> fieldNameMethods =  getFieldsTypeMap(clazz);

				JsonDocument document = (JsonDocument) value;
				fieldNameMethods.forEach((k, v) -> {
					Object setValue = adjustRoboClassCastByField(v.getField(), document.getKey(k));
					try {
						v.getSetMethod().invoke(instance, setValue);
					} catch (Exception e) {
						throw new RoboReflectException("set value", e);
					}
				});
				return (T)instance;

			} catch (Exception e) {
				throw new RoboReflectException("casting: new class instance", e);
			}
		}
	}

	// FIXME: 12/15/17 move to the type parser
	private static Object adjustRoboClassCastByField(Field field, Object value) {
		return adjustRoboClassCast(field.getType(), value);
	}


	@SuppressWarnings("unchecked")
	private static  Object adjustRoboClassCast(Class<?> clazz, Object value) {
		if(value != null){
			final TypeMapper typeMapper = TypeMapper.getBySource(clazz);
			return typeMapper != null ? typeMapper.getTranslate().apply(value) : castObjectByRoboJsonDocument(clazz, value);
		}
		return null;
	}

	private static class MapEntyDTO {
		private final String key;
		private final Object value;

		public MapEntyDTO(String key, Object value) {
			this.key = key;
			this.value = value;
		}

		public String getKey() {
			return key;
		}

		public Object getValue() {
			return value;
		}
	}

	private static Class<?> getClassByPath(String path) {
		String packageLink = adjustPathToPackage(path);
		try {
			return Class.forName(packageLink);
		} catch (ClassNotFoundException e) {
			throw new RoboReflectException("get class by path", e);
		}
	}

	private static String adjustPathToPackage(String path) {
		return path.replace(UTF8_SOLIDUS, UTF8_DOT);
	}

	public static String createJson(Object obj) {
		Map<String, ClassGetSetDTO> map = getFieldsTypeMap(obj.getClass());
		return createJsonByFieldClassGetter(map, obj);
	}

	private static String createJsonByFieldClassGetter(Map<String, ClassGetSetDTO> map, Object obj) {
		//@formatter:off
		return new StringBuilder(UTF8_CURLY_BRACKET_LEFT)
				.append(map.entrySet().stream()
						.map(entry -> new MapEntyDTO(entry.getKey(), ReflectUtils.getJsonValue(entry.getValue(), obj)))
						.filter(entry -> Objects.nonNull(entry.getValue()))
						.map(entry -> new StringBuilder(UTF8_QUOTATION_MARK)
								.append(entry.getKey())
								.append(UTF8_QUOTATION_MARK)
								.append(UTF8_COLON)
								.append(entry.getValue()))
						.collect(Collectors.joining(UTF8_COMMA))).append(UTF8_CURLY_BRACKET_RIGHT)
				.toString();
		//@formatter:on
	}


	public static Map<String, ClassGetSetDTO> getFieldsTypeMap(Class<?> clazz) {
		if(clazzDescriptorMap.containsKey(clazz)) {
			return clazzDescriptorMap.get(clazz);
		} else {
			// @formatter::off
			Map<String, ClassGetSetDTO> clazzFields = Stream.of(clazz.getDeclaredFields()).map(field -> {
				try {
					String adjustedName = adjustFirstLetterUpperCase(field.getName());
					String getMethodName = getGetterNameByType(field.getType(), adjustedName);
					String setMethodName = METHOD_SET.concat(adjustedName);
					Method getMethod = clazz.getDeclaredMethod(getMethodName);
					Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());
					return new ClassGetSetDTO(field.getName(), field, getMethod, setMethod);
				} catch (Exception e) {
					throw new RoboReflectException("class configuration", e);
				}
			}).collect(Collectors.toMap(ClassGetSetDTO::getName, e -> e, (e1, e2) -> e1, LinkedHashMap::new));
			// @formatter::on
			clazzDescriptorMap.put(clazz, clazzFields);
			return clazzFields;
		}


	}

	private static String getGetterNameByType(Class<?> clazz, String fieldName) {
		return METHOD_IS_TYPES.contains(clazz) ? METHOD_IS + fieldName : METHOD_GET + fieldName;
	}

	private static String adjustFirstLetterUpperCase(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
