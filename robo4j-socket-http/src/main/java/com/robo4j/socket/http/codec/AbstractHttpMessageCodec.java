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
