package com.robo4j.socket.http.util;

import com.robo4j.socket.http.json.JsonBooleanAdapter;
import com.robo4j.socket.http.json.JsonEnumAdapter;
import com.robo4j.socket.http.json.JsonNumberAdapter;
import com.robo4j.socket.http.json.JsonStringAdapter;
import com.robo4j.socket.http.json.JsonTypeAdapter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Translates primitive classes to the appropriate wrappers
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public enum TypeMapper {

	//@formatter:off
    BOOLEAN         (Boolean.class, Boolean.class, (Object o) -> Boolean.valueOf(o.toString()), new JsonBooleanAdapter()),
    BOOLEAN_PRIM    (boolean.class, Boolean.class, (Object o) -> Boolean.valueOf(o.toString()), new JsonBooleanAdapter()),
    BYTE            (Byte.class, Byte.class, (Object o) -> Byte.valueOf(o.toString()), new JsonStringAdapter()),
    BYTE_PRIM       (byte.class, Byte.class, (Object o) -> Byte.valueOf(o.toString()), new JsonStringAdapter()),
    CHAR            (Character.class, Character.class, (Object o) -> o, new JsonStringAdapter()),
    CHAR_PRIM       (char.class, Character.class, (Object o) -> Character.valueOf((char)o), new JsonStringAdapter()),
    SHORT           (Short.class, Short.class, (Object o) -> Short.valueOf(o.toString()), new JsonNumberAdapter()),
    SHORT_PRIM      (short.class, Short.class, (Object o) -> Short.valueOf(o.toString()), new JsonNumberAdapter()),
    INTEGER         (Integer.class, Integer.class, (Object o) -> Integer.valueOf(o.toString()),  new JsonNumberAdapter()),
    INT             (int.class, Integer.class, (Object o) -> Integer.valueOf(o.toString()),  new JsonNumberAdapter()),
    LONG            (Long.class, Long.class, (Object o) -> Long.valueOf(o.toString()),  new JsonNumberAdapter()),
    LONG_PRIM       (long.class, Long.class, (Object o) -> Long.valueOf(o.toString()),  new JsonNumberAdapter()),
    FLOAT           (Float.class, Float.class, (Object o) -> Float.valueOf(o.toString()),  new JsonNumberAdapter()),
    FLOAT_PRIM      (float.class, Float.class, (Object o) -> Float.valueOf(o.toString()),  new JsonNumberAdapter()),
    DOUBLE          (Double.class, Double.class, (Object o) -> Double.valueOf(o.toString()),  new JsonNumberAdapter()),
    DOUBLE_PRIM     (double.class, Double.class, (Object o) -> Double.valueOf(o.toString()),  new JsonNumberAdapter()),
    STRING          (String.class, String.class, String::valueOf, new JsonStringAdapter()),
	ENUM			(Enum.class, String.class, (Object e) -> ((Enum<?>)e).name(), new JsonEnumAdapter()),
    ;
    //@formatter:on

	private Class<?> source;
	private Class<?> target;
	private Function<Object, ?> translate;
	private JsonTypeAdapter adapter;
	private static Map<Class<?>, TypeMapper> internMapByName;

	TypeMapper(Class<?> source, Class<?> target, Function<Object, ?> translate, JsonTypeAdapter adapter) {
		this.source = source;
		this.target = target;
		this.translate = translate;
		this.adapter = adapter;
	}

	public static TypeMapper getBySource(Class<?> source) {
		if (internMapByName == null) {
			internMapByName = initMapping();
		}
		return internMapByName.get(source);
	}

	public Class<?> getSource() {
		return source;
	}

	public Class<?> getTarget() {
		return target;
	}

	public Function<Object, ?> getTranslate() {
		return translate;
	}

	public JsonTypeAdapter getAdapter() {
		return adapter;
	}

	private static Map<Class<?>, TypeMapper> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(TypeMapper::getSource, e -> e));
	}

}
