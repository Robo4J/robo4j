/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.SocketEncoder;
import com.robo4j.socket.http.util.ReflectUtils;

/**
 * AbstractHttpMessageCodec decodes appropriate class instance into JSON string
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractHttpMessageCodec<T> implements SocketDecoder<String, T>, SocketEncoder<T, String> {
	private final Class<T> clazz;

	protected AbstractHttpMessageCodec(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public Class<T> getDecodedClass() {
		return clazz;
	}

	@Override
	public Class<T> getEncodedClass() {
		return clazz;
	}

	@Override
	public T decode(String json) {
		JsonReader jsonReader = new JsonReader(json);
		JsonDocument document = jsonReader.read();
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, document);
	}

	@Override
	public String encode(T message) {
		return ReflectUtils.createJson(message);
	}

}
