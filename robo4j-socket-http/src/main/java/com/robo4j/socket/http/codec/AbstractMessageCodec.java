package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.util.ReflectUtils;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractMessageCodec<T> implements HttpDecoder<T>, HttpEncoder<T> {
	private final Class<T> clazz;

	protected AbstractMessageCodec(Class<T> clazz) {
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
