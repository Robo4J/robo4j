/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

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
 * Camera Image codec
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class CameraMessageCodec implements HttpDecoder<CameraMessage>, HttpEncoder<CameraMessage> {
	private static final Pattern CAMERA_PATTERN = Pattern.compile("^\\{\\s*\"|\"\\s*\\}$");
	private static final String PATTERN_SPLIT = "\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?";
	private static final String KEY_TYPE = "type";
	private static final String KEY_VALUE = "value";
	private static final String KEY_IMAGE = "image";

	@Override
	public String encode(CameraMessage message) {
		//@formatter:off
		return JsonElementStringBuilder.Builder()
				.add(UTF8_CURLY_BRACKET_LEFT)
				.addQuotationWithDelimiter(UTF8_COLON, KEY_TYPE)
				.addQuotationWithDelimiter(UTF8_COMMA, message.getType())
				.addQuotationWithDelimiter(UTF8_COLON, KEY_VALUE)
				.addQuotationWithDelimiter(UTF8_COMMA, message.getValue())
				.addQuotationWithDelimiter(UTF8_COLON, KEY_IMAGE)
				.addQuotation(message.getImage())
				.add(UTF8_CURLY_BRACKET_RIGHT)
				.build();
		//@formatter:on
    }

    @Override
    public CameraMessage decode(String json) {
        final Map<String, String> map = new HashMap<>();
		final String[] parts = CAMERA_PATTERN.matcher(json).replaceAll(StringConstants.EMPTY).split(PATTERN_SPLIT);
		for (int i = 0; i < parts.length - 1; i += 2) {
			map.put(parts[i].trim(), parts[i + 1].trim());
		}
		final String type = map.get(KEY_TYPE);
		final String value =  map.get(KEY_VALUE);
		final String image = map.get(KEY_IMAGE);
		return new CameraMessage(type, value, image);

	}

	@Override
	public Class<CameraMessage> getEncodedClass() {
		return CameraMessage.class;
	}

	@Override
	public Class<CameraMessage> getDecodedClass() {
		return CameraMessage.class;
	}

}
