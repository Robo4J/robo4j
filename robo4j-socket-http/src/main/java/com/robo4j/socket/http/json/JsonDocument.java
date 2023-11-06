/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */
package com.robo4j.socket.http.json;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * representation of the read String stream
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class JsonDocument {

	private final List<Object> array = new LinkedList<>();
	private final Map<String, Object> map = new LinkedHashMap<>();
	private final Type type;

	// TODO: review the public constructor caused by JPMS
	public JsonDocument(Type type) {
		this.type = type;
	}

	public void put(String key, Object value) {
		map.put(key, value);
	}

	public void add(Object value) {
		array.add(value);
	}

	public Object getKey(String key) {
		return map.get(key);
	}

	public List<Object> getArray() {
		return array;
	}

	public Map<String, Object> getMap() {
		return map;
	}

	public Type getType() {
		return type;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JsonDocument document = (JsonDocument) o;
		return Objects.equals(array, document.array) &&
				Objects.equals(map, document.map) &&
				type == document.type;
	}

	@Override
	public int hashCode() {

		return Objects.hash(array, map, type);
	}

	@Override
	public String toString() {
		return "JsonDocument{" + "array=" + array + ", map=" + map + ", type=" + type + '}';
	}

	public enum Type {
		OBJECT, ARRAY
	}
}
