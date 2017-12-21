package com.robo4j.socket.http.util;

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
public enum TypeMapper{

	//@formatter:off
    BOOLEAN         (Boolean.class, Boolean.class, (Object o) -> Boolean.valueOf(o.toString())),
    BOOLEAN_PRIM    (boolean.class, Boolean.class, (Object o) -> Boolean.valueOf(o.toString())),
    BYTE            (Byte.class, Byte.class, (Object o) -> Byte.valueOf(o.toString())),
    BYTE_PRIM       (byte.class, Byte.class, (Object o) -> Byte.valueOf(o.toString())),
    CHAR            (Character.class, Character.class, (Object o) -> Character.valueOf((char)o)),
    CHAR_PRIM       (char.class, Character.class, (Object o) -> Character.valueOf((char)o)),
    SHORT           (Short.class, Short.class, (Object o) -> Short.valueOf(o.toString())),
    SHORT_PRIM      (short.class, Short.class, (Object o) -> Short.valueOf(o.toString())),
    INTEGER         (Integer.class, Integer.class, (Object o) -> Integer.valueOf(o.toString())),
    INT             (int.class, Integer.class, (Object o) -> Integer.valueOf(o.toString())),
    LONG            (Long.class, Long.class, (Object o) -> Long.valueOf(o.toString())),
    LONG_PRIM       (long.class, Long.class, (Object o) -> Long.valueOf(o.toString())),
    FLOAT           (Float.class, Float.class, (Object o) -> Float.valueOf(o.toString())),
    FLOAT_PRIM      (float.class, Float.class, (Object o) -> Float.valueOf(o.toString())),
    DOUBLE          (Double.class, Double.class, (Object o) -> Double.valueOf(o.toString())),
    DOUBLE_PRIM     (double.class, Double.class, (Object o) -> Double.valueOf(o.toString())),
    STRING          (String.class, String.class, String::valueOf),
    ;
    //@formatter:on

	private Class<?> source;
	private Class<?> target;
	private Function<Object, ?> translate;
	private static Map<Class<?>, TypeMapper> internMapByName;

	TypeMapper(Class<?> source, Class<?> target, Function<Object, ?> translate) {
		this.source = source;
		this.target = target;
		this.translate = translate;
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

    private static Map<Class<?>, TypeMapper> initMapping() {
		return Stream.of(values()).collect(Collectors.toMap(TypeMapper::getSource, e -> e));
	}

}
