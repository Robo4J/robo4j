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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.http.units.httpunit.codec;

import java.util.HashMap;
import java.util.Map;

import com.robo4j.http.units.Constants;
import com.robo4j.http.units.HttpDecoder;
import com.robo4j.http.units.HttpEncoder;
import com.robo4j.http.units.HttpProducer;

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
	public String encode(SimpleCommand stuff) {
		final StringBuilder sb = new StringBuilder("{\"value\":\"").append(stuff.getValue());
		if (stuff.getType().equals(Constants.EMPTY_STRING)) {
			sb.append("\"}");
		} else {
			sb.append("\",\"type\":\"").append(stuff.getType()).append("\"}");
		}
		return sb.toString();
	}

	@Override
	public SimpleCommand decode(String json) {
		final Map<String, String> map = new HashMap<>();
		//@formatter:off
		final String[] parts = json.replaceAll("^\\{\\s*\"|\"\\s*\\}$", Constants.EMPTY_STRING)
				.split("\"?(\"?\\s*:\\s*\"?|\\s*,\\s*)\"?");
		//@formatter:on
		for (int i = 0; i < parts.length - 1; i += 2) {
			map.put(parts[i], parts[i + 1]);
		}

		return map.containsKey(KEY_TYPE) ? new SimpleCommand(map.get(KEY_VALUE).trim(), map.get(KEY_TYPE).trim())
				: new SimpleCommand(map.get(KEY_VALUE).trim());
	}

	@Override
	public Class<SimpleCommand> getEncodedClass() {
		return SimpleCommand.class;
	}

	@Override
	public Class<SimpleCommand> getDecodedClass() {
		return SimpleCommand.class;
	}
}
