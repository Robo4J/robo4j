package com.robo4j.socket.http.util;

import com.oracle.javafx.jmx.json.JSONDocument;
import com.robo4j.socket.http.dto.ClassGetSetDTO;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
	private static final Pattern MAP_PATTERN = Pattern.compile("L.*<L(.*/.*);L(.*/.*);>;");
	// [Ljava.lang.String;
	private static final Pattern ARRAY_PATTERN = Pattern.compile("L(.*);");
	private static final Pattern LIST_PATTERN = Pattern.compile("List<(.*)>");
	private static final Set<Class<?>> METHOD_IS_TYPES = Stream.of(boolean.class).collect(Collectors.toSet());
	private static final String METHOD_IS = "is";
	private static final String METHOD_GET = "get";
	private static final String METHOD_SET = "set";

	private static final Map<Class<?>, Map<String, ClassGetSetDTO>> clazzSettersMap = new HashMap<>();

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
			return value == null ? null : JsonUtil.WITHOUT_QUOTATION_TYPES.contains(getterDTO.getField().getClass()) ? String.valueOf(value)
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
	public static <T> T createInstanceSetterByFieldMap(Class<T> clazz, Map<String, ClassGetSetDTO> getterDTOMap,
			Map<String, Object> jsonMap) {

		try {
			Object instance = clazz.newInstance();
			getterDTOMap.entrySet().stream().filter(e -> Objects.nonNull(jsonMap.get(e.getKey()))).forEach(e -> {
				try {
					ClassGetSetDTO value = e.getValue();
					value.getSetMethod().invoke(instance,
							adjustClassCastByField(value.getField(), jsonMap.get(e.getKey())));
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
	public static <T> T createInstanceSetterByJSONDocument(Class<T> clazz, Map<String, ClassGetSetDTO> getterDTOMap,
			JSONDocument jsonDocument) {
		try {
			Object instance = clazz.newInstance();
			getterDTOMap.entrySet().stream().filter(e -> Objects.nonNull(jsonDocument.get(e.getKey()))).forEach(e -> {
				try {
					ClassGetSetDTO value = e.getValue();
					value.getSetMethod().invoke(instance, adjustJSONDocumentCast(value, jsonDocument, e.getKey()));
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

	private static Object adjustJSONDocumentCast(ClassGetSetDTO classGetSetDTO, JSONDocument document, String key) {
		Class<?> clazz = classGetSetDTO.getField().getType();
		if (clazz.isArray()) {
			Class<?> arrayClass = extractArrayClassSignature(clazz.getName());
			List<?> list = document.get(key).array().stream().map(arrayClass::cast).collect(Collectors.toList());

			return listToArray(list);
		} else {
			switch (clazz.getSimpleName()) {
			case "String":
				return document.getString(key);
			case "Integer":
			case "int":
				return document.getNumber(key).intValue();
			case "Boolean":
			case "boolean":
				return document.getBoolean(key);
			case "Float":
			case "float":
				return document.getNumber(key).floatValue();
			case "Long":
			case "long":
				return document.getNumber(key).longValue();
			case "List":
				Class<?> listClass = extractListClassSignature(
						classGetSetDTO.getField().getGenericType().getTypeName());
				return document.get(key).array().stream().map(e -> castObjectByJSONDocument(listClass, e))
						.collect(Collectors.toList());
			case "Map":
				JSONDocument mapDocument = document.get(key);
				Map<Object, Object> map = new LinkedHashMap<>();
				if (mapDocument.object().size() > 0) {
					mapDocument.object().forEach(map::put);
				}
				return map;
			default:
				return document.getString(key);

			}
		}

	}

	private static <T> T castObjectByJSONDocument(Class<T> clazz, Object value) {
		if (clazz.equals(String.class) || JsonUtil.WITHOUT_QUOTATION_TYPES.contains(clazz)) {
			return (T) adjustClassCast(clazz, value);
		} else {
			try {
				Object instance = clazz.newInstance();
				Map<String, ClassGetSetDTO> fieldNameMethods = clazzSettersMap.containsKey(clazz) ? clazzSettersMap.get(clazz)
						:  getFieldsTypeMap(clazz);

                JSONDocument document = (JSONDocument) value;
                fieldNameMethods.forEach((k, v) -> {
                    Object setValue = adjustClassCastByField(v.getField(), document.object().get(k));
                    System.out.println("method: " + k + " v: " + adjustClassCastByField(v.getField(), document.object().get(k)));
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
	private static Object adjustClassCastByField(Field field, Object value) {
		return adjustClassCast(field.getType(), value);
	}

	private static Object adjustClassCast(Class<?> clazz, Object value) {
	    if(value != null){
            switch (clazz.getSimpleName()) {
                case "String":
                    return String.valueOf(value.toString());
                case "Integer":
                case "int":
                    return Integer.valueOf(value.toString());
                case "Boolean":
                case "boolean":
                    return Boolean.valueOf(value.toString());
                case "Float":
                case "float":
                    return Float.valueOf(value.toString());
                case "Long":
                case "long":
                    return Long.valueOf(value.toString());
                default:
                    return castObjectByJSONDocument(clazz, value);
            }
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

	public static Class<?>[] getClassesBySignature(Class<?> type, String signature) {

		Matcher mapMather = MAP_PATTERN.matcher(signature);

		if (mapMather.find() && mapMather.groupCount() == 2) {
			return getClassesByPackage(type, mapMather.group(1), mapMather.group(2));

		}
		return null;

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

	private static Class<?>[] getClassesByPackage(Class<?> collectionClass, String... destinations) {
		final Class<?>[] result = new Class<?>[destinations.length + 1];
		result[0] = collectionClass;
		for (int i = 0; i < destinations.length; i++) {
			try {
				result[i + 1] = getClassByPath(destinations[i]);
			} catch (Exception e) {
				throw new RoboReflectException("not found", e);
			}
		}
		return result;
	}

	public static String createJsonByFieldClassGetter(Map<String, ClassGetSetDTO> map, Object obj) {
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
		// @formatter::off
		return Stream.of(clazz.getDeclaredFields()).map(field -> {
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
	}

	private static String getGetterNameByType(Class<?> clazz, String fieldName) {
		return METHOD_IS_TYPES.contains(clazz) ? METHOD_IS + fieldName : METHOD_GET + fieldName;
	}

	private static String adjustFirstLetterUpperCase(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
