package com.robo4j.socket.http.util;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ClassFieldValueDTO;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.FieldValueDTO;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;
import static com.robo4j.util.Utf8Constant.UTF8_QUOTATION_MARK;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class ReflectUtils {
	private static final Set<Class<?>> METHOD_IS_TYPES = Stream.of(boolean.class).collect(Collectors.toSet());
	private static final String METHOD_IS = "is";
	private static final String METHOD_GET = "get";
	private static final String METHOD_SET = "set";

	public static Map<String, FieldValueDTO> getFieldsTypeValueMap(Class<?> clazz, Object obj) {
		// @formatter::off
		return Stream.of(clazz.getDeclaredFields()).map(field -> {
			try {
				field.setAccessible(true);
				FieldValueDTO value = new FieldValueDTO(field.getType(), field.get(obj));
				field.setAccessible(false);
				return new ClassFieldValueDTO(field.getName(), value);
			} catch (Exception e) {
				SimpleLoggingUtil.error(ReflectUtils.class, "fields", e);
				throw new RuntimeException(e);
			}
		}).collect(Collectors.toMap(ClassFieldValueDTO::getName, ClassFieldValueDTO::getValue));
		// @formatter::on
	}

	public static String getJsonValue(ClassGetSetDTO getterDTO, Object obj) {
		try {
			Object value = getterDTO.getGetMethod().invoke(obj);
			//@formatter:off
			return value == null ? null : JsonUtil.WITHOUT_QUOTATION_TYPES.contains(getterDTO.getClazz()) ? String.valueOf(value)
					: new StringBuilder(UTF8_QUOTATION_MARK).append(value).append(UTF8_QUOTATION_MARK).toString();
			//@formatter:on
		} catch (Exception e) {
			throw new RoboReflectException("object getter value", e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> T createInstanceSetterByFieldMap(Class<T> clazz, Map<String, ClassGetSetDTO> getterDTOMap,
												 Map<String, Object> jsonMap) {

		try {
			Object instance = clazz.newInstance();
			getterDTOMap.entrySet().stream()
                    .filter(e -> Objects.nonNull(jsonMap.get(e.getKey())))
                    .forEach(e -> {
                        try {
                            ClassGetSetDTO value = e.getValue();
                            value.getSetMethod().invoke(instance, adjustClassCast(value.getClazz(), jsonMap.get(e.getKey()).toString()));
                        } catch (Exception e1) {
                            throw new RoboReflectException("create instance field", e1);
                        }
			});
			return (T) instance;
		} catch (Exception e) {
			throw new RoboReflectException("create instance with setter", e);
		}
	}

    // FIXME: 12/15/17 move to the type parser
    private static Object adjustClassCast(Class<?> clazz, String value){
        switch (clazz.getSimpleName()){
            case "Integer":
            case "int":
                return Integer.valueOf(value);
            case "Boolean":
            case "boolean":
                return Boolean.valueOf(value);
            case "Float":
            case "float":
                return Float.valueOf(value);
            case "Long":
            case "long":
                return Long.valueOf(value);
            default:
                return value;

        }
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

	public static Map<String, ClassGetSetDTO> getFieldsTypeMap(Class<?> clazz){
        //@formatter::off
        return Stream.of(clazz.getDeclaredFields())
                .map(field -> {
                    try {
                    	String adjustedName = adjustFirstLetterUpperCase(field.getName());
                        String getMethodName = getGetterNameByType(field.getType(), adjustedName);
                        String setMethodName = METHOD_SET.concat(adjustedName);
                        Method getMethod = clazz.getDeclaredMethod(getMethodName);
                        Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());
                        return new ClassGetSetDTO(field.getName(), field.getType(), getMethod, setMethod);
                    } catch (Exception e){
                        throw new RoboReflectException("class configuration", e);
                    }
                })
                .collect(Collectors.toMap(ClassGetSetDTO::getName, e -> e, (e1, e2) -> e1, LinkedHashMap::new));
        //@formatter::on
    }

	private static String getGetterNameByType(Class<?> clazz, String fieldName) {
		return METHOD_IS_TYPES.contains(clazz) ? METHOD_IS + fieldName : METHOD_GET + fieldName;
	}

	private static String adjustFirstLetterUpperCase(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
