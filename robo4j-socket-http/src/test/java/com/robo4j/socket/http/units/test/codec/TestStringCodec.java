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
package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.units.SocketDecoder;
import com.robo4j.socket.http.units.SocketEncoder;
import com.robo4j.socket.http.units.HttpProducer;

/**
 * Test class implementing both an encoder and decoder.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class TestStringCodec implements SocketDecoder<String, String>, SocketEncoder<String, String> {
	@Override
	public String decode(String json) {
		String withoutStart = json.replace("data:", "");
		String withoutBrackets = withoutStart.replaceAll("[\\[\\]\\{\\}]", "");
		return withoutBrackets;
	}

	@Override
	public Class<String> getDecodedClass() {
		return String.class;
	}

	@Override
	public String encode(String data) {
		StringBuilder builder = new StringBuilder();
		builder.append("{data:");
		builder.append(data);
		builder.append("}");
		return builder.toString();
	}

	@Override
	public Class<String> getEncodedClass() {
		return String.class;
	}
}
