package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.json.JsonDocument;
import com.robo4j.socket.http.json.JsonReader;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public abstract class AbstractMessageCodec<T> implements HttpDecoder<T>, HttpEncoder<T> {
	private final Class<T> clazz;
	private final Map<String, ClassGetSetDTO> descriptorMap;

	protected AbstractMessageCodec(Class<T> clazz) {
		this.clazz = clazz;
		this.descriptorMap = ReflectUtils.getFieldsTypeMap(clazz);
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
		return ReflectUtils.createInstanceByClazzAndDescriptorAndJsonDocument(clazz, descriptorMap, document);
	}

	@Override
	public String encode(T message) {
		return ReflectUtils.createJson(descriptorMap, message);
	}

}
