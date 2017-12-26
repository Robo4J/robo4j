package com.robo4j.socket.http.util;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.json.JsonDocument;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
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
	private static final Pattern MAP_PATTERN = Pattern.compile(".*<.*\\.*,\\s(.*\\..*)>");
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
			return value == null ? null : JsonUtil.WITHOUT_QUOTATION_TYPES.contains(getterDTO.getValueClass()) ? String.valueOf(value)
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

	public static Class<?> extractMapValueClassSignature(String value) {
		return extractSimpleClassSignatureByPattern(MAP_PATTERN, value);
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
	public static <T> T createInstanceSetterByRoboJsonDocument(Class<T> clazz, JsonDocument jsonDocument) {

		final Map<String, ClassGetSetDTO> getterDTOMap = getFieldsTypeMap(clazz);
		try {
			Object instance = clazz.newInstance();
			getterDTOMap.entrySet().stream().filter(e -> Objects.nonNull(jsonDocument.getKey(e.getKey())))
					.forEach(e -> {
						try {
							ClassGetSetDTO value = e.getValue();
							value.getSetMethod().invoke(instance,
									adjustRoboJsonDocumentCast(value, jsonDocument, e.getKey()));
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
	private static <E> E[] listToArray(List<E> list) {
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

	private static Object adjustRoboJsonDocumentCast(ClassGetSetDTO classGetSetDTO, JsonDocument document, String key) {
		Class<?> clazz = classGetSetDTO.getValueClass();
		if (clazz.isArray()) {
			Class<?> arrayClass = extractArrayClassSignature(clazz.getName());
			List<?> list = ((JsonDocument) document.getKey(key)).getArray().stream()
					.map(arrayClass::cast)
					.collect(Collectors.toCollection(LinkedList::new));
			return listToArray(list);
		} else {

			if (classGetSetDTO.getCollection() != null) {
				switch (classGetSetDTO.getCollection()) {
				case LIST:
					Class<?> listClass = classGetSetDTO.getValueClass();
					//@formatter:off
					return ((JsonDocument) document.getKey(key)).getArray().stream()
							.filter(Objects::nonNull)
							.map(e -> adjustRoboClassCast(listClass, e))
							.collect(Collectors.toList());
					//@formatter:on
				case MAP:
					JsonDocument mapDocument = (JsonDocument) document.getKey(key);
					Map<String, Object> documentMap = mapDocument.getMap();
					if (documentMap.isEmpty()) {
						return documentMap;
					} else {
						//@formatter:off
						return documentMap.entrySet().stream()
								.collect(Collectors
										.toMap(Map.Entry::getKey, e -> {
											if (e.getValue() != null && e.getValue() instanceof JsonDocument) {
												Class<?> mapValueClazz = classGetSetDTO.getValueClass();
												return adjustRoboClassCast(mapValueClazz, e.getValue());
											}
											return e.getValue();}));
						//@formatter:on

					}
				default:
					throw new RoboReflectException("wrong collection" + classGetSetDTO);
				}
			} else {
				return adjustRoboClassCast(classGetSetDTO.getValueClass(), document.getKey(key));
			}
		}
	}


	@SuppressWarnings("unchecked")
	private static <T> T castObjectByRoboJsonDocument(Class<T> clazz, Object value) {
		if (clazz.equals(String.class) || JsonUtil.WITHOUT_QUOTATION_TYPES.contains(clazz)) {
			return (T) adjustRoboClassCast(clazz, value);
		} else {
			try {
				Object instance = clazz.newInstance();
				Map<String, ClassGetSetDTO> fieldNameMethods = getFieldsTypeMap(clazz);

				JsonDocument document = (JsonDocument) value;
				fieldNameMethods.forEach((k, v) -> {
					Object setValue = adjustRoboClassCast(v.getValueClass(), document.getKey(k));
					try {
						v.getSetMethod().invoke(instance, setValue);
					} catch (Exception e) {
						throw new RoboReflectException("set value", e);
					}
				});
				return (T) instance;

			} catch (Exception e) {
				throw new RoboReflectException("casting: new class instance", e);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static Object adjustRoboClassCast(Class<?> clazz, Object value) {
		if (value != null) {
			final TypeMapper typeMapper = TypeMapper.getBySource(clazz);
			return typeMapper != null ? typeMapper.getTranslate().apply(value)
					: castObjectByRoboJsonDocument(clazz, value);
		}
		return null;
	}

	private static class MapEntryDTO {
		private final String key;
		private final Object value;

		public MapEntryDTO(String key, Object value) {
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
						.map(entry -> new MapEntryDTO(entry.getKey(), ReflectUtils.getJsonValue(entry.getValue(), obj)))
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
		if (clazzDescriptorMap.containsKey(clazz)) {
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

					if (field.getType().isAssignableFrom(Map.class)) {
						Class<?> mapValueClazz = ReflectUtils
								.extractMapValueClassSignature(field.getGenericType().getTypeName());
						return new ClassGetSetDTO(field.getName(), mapValueClazz, TypeCollection.MAP, getMethod,
								setMethod);
					} else if (field.getType().isAssignableFrom(List.class)) {
						Class<?> listValueClazz = extractListClassSignature(field.getGenericType().getTypeName());
						return new ClassGetSetDTO(field.getName(), listValueClazz, TypeCollection.LIST, getMethod,
								setMethod);
					} else {
						return new ClassGetSetDTO(field.getName(), field.getType(), getMethod, setMethod);
					}

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
