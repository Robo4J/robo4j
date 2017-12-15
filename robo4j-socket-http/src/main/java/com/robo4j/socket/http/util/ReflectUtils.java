package com.robo4j.socket.http.util;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.dto.ClassFieldValueDTO;
import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.dto.FieldValueDTO;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
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
			return JsonUtil.WITHOUT_QUOTATION_TYPES.contains(getterDTO.getClazz()) ? String.valueOf(value)
					: new StringBuilder(UTF8_QUOTATION_MARK).append(value).append(UTF8_QUOTATION_MARK).toString();
		} catch (Exception e) {
			throw new RoboReflectException("object getter value", e);
		}
	}

//	public static Constructor<?> findConstructorByClassAndParameters(Class<?> clazz, List<Class<?>> constructorTypes) {
//		Constructor[] constructors = clazz.getConstructors();
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
//	}

	@SuppressWarnings("unchecked")
	public static <T> T createInstanceSetterByFieldMap(Class<T> clazz, Map<String, ClassGetSetDTO> getterDTOMap,
												 Map<String, Object> jsonMap) {

		try {
			Object instance = clazz.newInstance();

			getterDTOMap.forEach((key, value) -> {
				try {
					value.getSetMethod().invoke(instance, JsonUtil.adjustClassCast(value.getClazz(), jsonMap.get(key).toString()));
				} catch (Exception e) {
					throw new RoboReflectException("create instance field", e);
				}
			});
			return (T) instance;
		} catch (Exception e) {
			throw new RoboReflectException("create instance with setter", e);
		}
	}


	public static String createJsonByFieldClassGetter(Map<String, ClassGetSetDTO> map, Object obj) {
		return new StringBuilder(UTF8_CURLY_BRACKET_LEFT).append(map.entrySet().stream()
				.map(entry -> new StringBuilder(UTF8_QUOTATION_MARK).append(entry.getKey()).append(UTF8_QUOTATION_MARK)
						.append(UTF8_COLON).append(ReflectUtils.getJsonValue(entry.getValue(), obj)))
				.collect(Collectors.joining(UTF8_COMMA))).append(UTF8_CURLY_BRACKET_RIGHT).toString();
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
