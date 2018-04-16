package com.robo4j.socket.http.util;

import static com.robo4j.socket.http.util.JsonUtil.WITHOUT_QUOTATION_TYPES;
import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_SQUARE_BRACKET_RIGHT;

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

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonGenericTypeAdapter;
import com.robo4j.socket.http.json.JsonTypeAdapter;

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
	private static final Map<Class<?>, JsonTypeAdapter> clazzAdapter = new HashMap<>();


	@SuppressWarnings("unchecked")
	public static <T> T createInstanceByClazzAndDescriptorAndJsonDocument(Class<T> clazz, JsonDocument jsonDocument) {
		try {
			Object instance = clazz.newInstance();

			getFieldsTypeMap(clazz).entrySet().stream().filter(e -> Objects.nonNull(jsonDocument.getKey(e.getKey())))
					.forEach(e -> {
						try {
							ClassGetSetDTO value = e.getValue();
							if(value.getValueClass().isEnum()){
								if(e.getValue().getCollection() != null){
									switch (e.getValue().getCollection()){
										case LIST:
											List<Enum<?>> enumList = ((JsonDocument)jsonDocument.getKey(e.getKey())).getArray().stream()
													.map(el -> extractEnumConstant(el.toString(),
															(Enum<?>[]) value.getValueClass().getEnumConstants()))
													.collect(Collectors.toCollection(LinkedList::new));
											value.getSetMethod().invoke(instance, enumList);
											break;
										case MAP:
											break;
										default:
											throw new IllegalStateException("not allowed");
									}
								} else {
									value.getSetMethod().invoke(instance,
											extractEnumConstant(jsonDocument.getKey(e.getKey()).toString(),
													(Enum<?>[]) value.getValueClass().getEnumConstants()));
								}

							} else {
								value.getSetMethod().invoke(instance,
										adjustRoboJsonDocumentCast(value, jsonDocument, e.getKey()));

							}
						} catch (Exception e1) {
							throw new RoboReflectException("create instance field", e1);
						}
					});
			return (T) instance;
		} catch (Exception e) {
			throw new RoboReflectException("create instance with setter", e);
		}

	}

	private static Enum<?> extractEnumConstant(String name, Enum<?>[] constants){
		for(Enum<?> constant: constants){
			if(constant.name().equals(name)){
				return constant;
			}
		}
		return null;
	}

	private static String getJsonValue(ClassGetSetDTO getterDTO, Object obj) {
		try {
			Object value = getterDTO.getGetMethod().invoke(obj);
			Class<?> clazz = getterDTO.getValueClass().isEnum() ? Enum.class : getterDTO.getValueClass();
			TypeMapper typeMapper = TypeMapper.getBySource(clazz);
			if(value == null){
				return null;
			} else if(getterDTO.getCollection() == null && typeMapper != null){
				JsonTypeAdapter adapter = typeMapper.getAdapter();
				return adapter.adapt(value);
			}
			return processCollectionToJson(getterDTO, typeMapper, value);
		} catch (Exception e) {
			throw new RoboReflectException("object getter value: " + getterDTO + " obj: " + obj, e);
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
		if (classGetSetDTO.getCollection() != null) {
			switch (classGetSetDTO.getCollection()) {
				case ARRAY:
					Class<?> arrayClass = classGetSetDTO.getValueClass();
					List<?> list = ((JsonDocument) document.getKey(key)).getArray().stream().map(arrayClass::cast)
							.collect(Collectors.toCollection(LinkedList::new));
					return listToArray(list);
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
					throw new RoboReflectException("wrong collection: " + classGetSetDTO);
				}
		} else {
			return adjustRoboClassCast(classGetSetDTO.getValueClass(), document.getKey(key));
		}

	}

	@SuppressWarnings("unchecked")
	private static <T> T castObjectByRoboJsonDocument(Class<T> clazz, Object value) {
		if (clazz.equals(String.class) || WITHOUT_QUOTATION_TYPES.contains(clazz)) {
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

	public static String createJson(Object obj) {
		Map<String, ClassGetSetDTO> map = getFieldsTypeMap(obj.getClass());
		return createJson(map, obj);
	}

	public static String createJson(Map<String, ClassGetSetDTO> descriptorMap, Object obj) {
		//@formatter:off
		return new StringBuilder(UTF8_CURLY_BRACKET_LEFT)
				.append(descriptorMap.entrySet().stream()
						.map(entry -> new MapEntryDTO(entry.getKey(), ReflectUtils.getJsonValue(entry.getValue(), obj)))
						.filter(entry -> Objects.nonNull(entry.getValue()))
						.map(entry -> JsonElementStringBuilder.Builder()
								.addQuotationWithDelimiter(UTF8_COLON, entry.getKey())
								.add(entry.getValue())
								.build())
						.collect(Collectors.joining(UTF8_COMMA)))
				.append(UTF8_CURLY_BRACKET_RIGHT)
				.toString();
		//@formatter:on
	}

	public static Map<String, ClassGetSetDTO> getFieldsTypeMap(Class<?> clazz) {
		if (clazzDescriptorMap.containsKey(clazz)) {
			return clazzDescriptorMap.get(clazz);
		} else {
			Map<String, ClassGetSetDTO> clazzFields = getClazzDescriptionDTO(clazz);
			clazzDescriptorMap.put(clazz, clazzFields);
			return clazzFields;
		}
	}

	private static Map<String, ClassGetSetDTO> getClazzDescriptionDTO(Class<?> clazz) {
		return Stream.of(clazz.getDeclaredFields()).map(field -> {
			try {
				String adjustedName = adjustFirstLetterUpperCase(field.getName());
				String getMethodName = getGetterNameByType(field.getType(), adjustedName);
				String setMethodName = METHOD_SET.concat(adjustedName);
				Method getMethod = clazz.getDeclaredMethod(getMethodName);
				Method setMethod = clazz.getDeclaredMethod(setMethodName, field.getType());

				if (field.getType().isAssignableFrom(Map.class)) {
					Class<?> mapValueClazz = ReflectUtils
							.extractMapValueClassSignature(field.getGenericType().getTypeName());
					return new ClassGetSetDTO(field.getName(), mapValueClazz, TypeCollection.MAP, getMethod, setMethod);
				} else if (field.getType().isAssignableFrom(List.class)) {
					Class<?> listValueClazz = extractListClassSignature(field.getGenericType().getTypeName());
					return new ClassGetSetDTO(field.getName(), listValueClazz, TypeCollection.LIST, getMethod,
							setMethod);
				} else if(field.getType().isArray()){
					Class<?> arrayClass = extractArrayClassSignature(field.getType().getName());
					return new ClassGetSetDTO(field.getName(), arrayClass, TypeCollection.ARRAY, getMethod,
							setMethod);
				}
				return new ClassGetSetDTO(field.getName(), field.getType(), getMethod, setMethod);

			} catch (Exception e) {
				throw new RoboReflectException("class configuration", e);
			}
		}).collect(Collectors.toMap(ClassGetSetDTO::getName, e -> e, (e1, e2) -> e1, LinkedHashMap::new));
	}

	private static JsonTypeAdapter getAdapterByClazz(Class<?> clazz, TypeMapper mapper){
		TypeMapper typeMapper = mapper != null ? mapper : TypeMapper.getBySource(clazz);
		return typeMapper == null ? getJsonTypeAdapter(clazz) :
				typeMapper.getAdapter();
	}


	@SuppressWarnings("unchecked")
	private static String processCollectionToJson(ClassGetSetDTO getterDTO, TypeMapper typeMapper, Object obj) {
		JsonElementStringBuilder result = JsonElementStringBuilder.Builder();
		JsonTypeAdapter jsonTypeAdapter = getAdapterByClazz(getterDTO.getValueClass(), typeMapper);
		switch (getterDTO.getCollection()) {
			case ARRAY:
				Object[] arrayObjects = (Object[]) obj;

				String arrayValue = Stream.of(arrayObjects)
						.map(element -> jsonTypeAdapter.adapt(element)).collect(Collectors.joining(UTF8_COMMA));
				//formatter:off
				result.add(UTF8_SQUARE_BRACKET_LEFT)
						.add(arrayValue)
						.add(UTF8_SQUARE_BRACKET_RIGHT);
				//formatter:off

				break;
			case LIST:
				List<Object> objects = (List<Object>) obj;
				String listValue = objects.stream().map(element -> jsonTypeAdapter.adapt(element)).collect(Collectors.joining(UTF8_COMMA));
				//formatter:off
				result.add(UTF8_SQUARE_BRACKET_LEFT)
						.add(listValue)
						.add(UTF8_SQUARE_BRACKET_RIGHT);
				//formatter:on
				break;
			case MAP:
				Map<Object, Object> objectMap = (Map<Object, Object>) obj;
				result.add(UTF8_CURLY_BRACKET_LEFT)
						.add(objectMap.entrySet().stream()
								.map(entry ->
									JsonElementStringBuilder.Builder()
											.addQuotationWithDelimiter(UTF8_COLON, entry.getKey())
											.add(jsonTypeAdapter.adapt(entry.getValue()))
											.build())
								.collect(Collectors.joining(UTF8_COMMA)))
						.add(UTF8_CURLY_BRACKET_RIGHT);
				break;
		}
		return result.build();
	}

	public static <T> JsonTypeAdapter getJsonTypeAdapter(Class<T> clazz){
		JsonTypeAdapter result = clazzAdapter.get(clazz);
		if(result != null){
			return result;
		}
		result = new JsonGenericTypeAdapter<>(clazz);
		clazzAdapter.put(clazz, result);
		return result;


	}


	private static Class<?> extractListClassSignature(String value) {
		return extractSimpleClassSignatureByPattern(LIST_PATTERN, value);
	}

	private static Class<?> extractArrayClassSignature(String value) {
		return extractSimpleClassSignatureByPattern(ARRAY_PATTERN, value);
	}

	private static Class<?> extractMapValueClassSignature(String value) {
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

	private static String getGetterNameByType(Class<?> clazz, String fieldName) {
		return METHOD_IS_TYPES.contains(clazz) ? METHOD_IS + fieldName : METHOD_GET + fieldName;
	}

	private static String adjustFirstLetterUpperCase(String text) {
		return text.substring(0, 1).toUpperCase() + text.substring(1);
	}
}
