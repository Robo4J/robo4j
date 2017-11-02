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
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StringConstants;

/**
 * Default codec for any unit that takes string messages.
 *
 * @see SimpleCommand
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
@HttpProducer
public class StringCodec implements HttpDecoder<String>, HttpEncoder<String> {
	private static final String KEY_MESSAGE = "message";

	@Override
	public String encode(String message) {
		return "{\"message\":\"" + message + "\"}";
	}

	@Override
	public Class<String> getEncodedClass() {
		return String.class;
	}

	@Override
	public String decode(String json) {
		return objectToString(JsonUtil.getMapNyJson(json).get(KEY_MESSAGE));
	}

	@Override
	public Class<String> getDecodedClass() {
		return String.class;
	}
	
	private String objectToString(Object object) {
		return object != null ? object.toString().trim() : StringConstants.EMPTY;
	}
}
