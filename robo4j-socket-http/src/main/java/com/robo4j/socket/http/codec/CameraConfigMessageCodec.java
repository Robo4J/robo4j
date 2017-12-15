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
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_WIDTH = "width";
	public static final String KEY_BRIGHTNESS = "brightness";
	public static final String KEY_SHARPNESS = "sharpness";
	public static final String KEY_TIMEOUT = "timeout";
	public static final String KEY_TIMELAPSE = "timelapse";

	@Override
	public CameraConfigMessage decode(String json) {
		final Map<String, Object> map = JsonUtil.getMapByJson(json);

		Integer height = getValue(map, KEY_HEIGHT);
		Integer width = getValue(map, KEY_WIDTH);
		Integer brightness = getValue(map, KEY_BRIGHTNESS);
		Integer sharpness = getValue(map, KEY_SHARPNESS);
		Integer timeout = getValue(map, KEY_TIMEOUT);
		Integer timelapse = getValue(map, KEY_TIMELAPSE);
		ReflectUtils.createInstanceSetterByFieldMap(CameraConfigMessage.class, fieldMethodMap,
				JsonUtil.getMapByJson(json));
		return new CameraConfigMessage(height, width, brightness, sharpness, timeout, timelapse);
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
