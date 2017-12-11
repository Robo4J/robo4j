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
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StringConstants;

import java.util.Map;

import static com.robo4j.util.Utf8Constant.UTF8_COLON;
import static com.robo4j.util.Utf8Constant.UTF8_COMMA;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_LEFT;
import static com.robo4j.util.Utf8Constant.UTF8_CURLY_BRACKET_RIGHT;

/**
 * default simple codec for simple commands Simple codec is currently used for
 * Enum
 *
 * @see SimpleCommand
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class SimpleCommandCodec implements HttpDecoder<SimpleCommand>, HttpEncoder<SimpleCommand> {
	private static final String KEY_TYPE = "type";
	private static final String KEY_VALUE = "value";

	@Override
	public String encode(SimpleCommand message) {
		//@formatter:off
		JsonElementStringBuilder builder =  JsonElementStringBuilder.Builder()
				.add(UTF8_CURLY_BRACKET_LEFT)
				.addQuotationWithDelimiter(UTF8_COLON, KEY_VALUE);
		if (message.getType().equals(StringConstants.EMPTY)) {
			builder.addQuotation(message.getValue())
					.add(UTF8_CURLY_BRACKET_RIGHT);
		} else {
			builder.addQuotationWithDelimiter(UTF8_COMMA, message.getValue())
					.addQuotationWithDelimiter(UTF8_COLON, KEY_TYPE)
					.addQuotation(message.getType())
					.add(UTF8_CURLY_BRACKET_RIGHT);
		}
		//@formatter:on
		return builder.build();
	}

	@Override
	public SimpleCommand decode(String json) {
		Map<String, Object> map = JsonUtil.getMapByJson(json);
		return map.containsKey(KEY_TYPE)
				? new SimpleCommand(objectToString(map.get(KEY_VALUE)), objectToString(map.get(KEY_TYPE)))
				: new SimpleCommand(objectToString(map.get(KEY_VALUE)));
	}

	@Override
	public Class<SimpleCommand> getEncodedClass() {
		return SimpleCommand.class;
	}

	@Override
	public Class<SimpleCommand> getDecodedClass() {
		return SimpleCommand.class;
	}

	// Private Methods
	private String objectToString(Object object) {
		return object != null ? object.toString().trim() : StringConstants.EMPTY;
	}
}
