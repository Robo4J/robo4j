package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.dto.ClassGetSetDTO;
import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.socket.http.util.ReflectUtils;

import java.util.Map;

/**
 *
 * Camera Image config codec
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@HttpProducer
public class CameraConfigMessageCodec implements HttpDecoder<CameraConfigMessage>, HttpEncoder<CameraConfigMessage> {

	private static final Map<String, ClassGetSetDTO> fieldMethodMap = ReflectUtils.getFieldsTypeMap(CameraConfigMessage.class);

	@Override
	public CameraConfigMessage decode(String json) {
		return  ReflectUtils.createInstanceSetterByFieldMap(CameraConfigMessage.class, fieldMethodMap,
				JsonUtil.getMapByJson(json));
	}

	private Integer getValue(Map<String, Object> map, String key) {
		return Integer.valueOf(map.get(key).toString());
	}

	@Override
	public Class<CameraConfigMessage> getDecodedClass() {
		return CameraConfigMessage.class;
	}

	@Override
	public String encode(CameraConfigMessage message) {
		return ReflectUtils.createJsonByFieldClassGetter(fieldMethodMap, message);
	}

	@Override
	public Class<CameraConfigMessage> getEncodedClass() {
		return CameraConfigMessage.class;
	}
}
