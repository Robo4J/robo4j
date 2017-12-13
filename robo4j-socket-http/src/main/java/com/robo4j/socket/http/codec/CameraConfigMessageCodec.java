package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpDecoder;
import com.robo4j.socket.http.units.HttpEncoder;
import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.util.JsonElementStringBuilder;
import com.robo4j.util.StringConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;

/**
 *
 * Camera Image config codec
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@HttpProducer
public class CameraConfigMessageCodec implements HttpDecoder<CameraConfigMessage>, HttpEncoder<CameraConfigMessage> {

	private static final Pattern CONFIG_PATTERN = Pattern.compile("^\\{\\s*\"|\\s*\\}$");
	private static final String PATTERN_SPLIT = "\"?(\\s*:\\s*|\\s*,\"\\s*)";
	private static final String KEY_HEIGHT = "height";
	private static final String KEY_WIDTH = "width";
	private static final String KEY_BRIGHTNESS = "brightness";
	private static final String KEY_SHARPNESS = "sharpness";
	private static final String KEY_TIMEOUT = "timeout";
	private static final String KEY_TIMELAPSE = "timelapse";

	@Override
	public CameraConfigMessage decode(String json) {
		final Map<String, String> map = new HashMap<>();
		final String[] parts = CONFIG_PATTERN.matcher(json).replaceAll(StringConstants.EMPTY).split(PATTERN_SPLIT);
		for (int i = 0; i < parts.length - 1; i += 2) {
			map.put(parts[i].trim(), parts[i + 1].trim());
		}

		Integer height = getValue(map, KEY_HEIGHT);
		Integer width = getValue(map, KEY_WIDTH);
		Integer brightness = getValue(map, KEY_BRIGHTNESS);
		Integer sharpness = getValue(map, KEY_SHARPNESS);
		Integer timeout = getValue(map, KEY_TIMEOUT);
		Integer timelapse = getValue(map, KEY_TIMELAPSE);

		return new CameraConfigMessage(height, width, brightness, sharpness, timeout, timelapse);
	}

	private Integer getValue(Map<String, String> map, String key) {
		return Integer.valueOf(map.get(key));
	}

	@Override
	public Class<CameraConfigMessage> getDecodedClass() {
		return CameraConfigMessage.class;
	}

	@Override
	public String encode(CameraConfigMessage message) {
		//@formatter:off
		return JsonElementStringBuilder.Builder()
				.add(UTF8_CURLY_BRACKET_LEFT)
				.addQuotationWithDelimiter(UTF8_COLON, KEY_HEIGHT)
				.addWithDelimiter(UTF8_COMMA, message.getHeight())
				.addQuotationWithDelimiter(UTF8_COLON, KEY_WIDTH)
				.addWithDelimiter(UTF8_COMMA, message.getWidth())
                .addQuotationWithDelimiter(UTF8_COLON, KEY_BRIGHTNESS)
				.addWithDelimiter(UTF8_COMMA, message.getBrightness())
                .addQuotationWithDelimiter(UTF8_COLON, KEY_SHARPNESS)
				.addWithDelimiter(UTF8_COMMA,message.getSharpness())
                .addQuotationWithDelimiter(UTF8_COLON, KEY_TIMEOUT)
                .addWithDelimiter(UTF8_COMMA, message.getTimeout())
                .addQuotationWithDelimiter(UTF8_COLON, KEY_TIMELAPSE)
                .add(message.getTimelapse())
				.add(UTF8_CURLY_BRACKET_RIGHT)
				.build();
		//@formatter:on
	}

	@Override
	public Class<CameraConfigMessage> getEncodedClass() {
		return CameraConfigMessage.class;
	}
}
