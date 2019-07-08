/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.SocketEncoder;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple encoder that encodes an array of string to Json.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class TestArrayEncoder implements SocketEncoder<String[], String> {

	@Override
	public String encode(String[] stuff) {
		StringBuilder builder = new StringBuilder();
		builder.append("{array:[");
		builder.append(Stream.of(stuff).collect(Collectors.joining(",")));
		builder.append("]}");
		return builder.toString();
	}

	@Override
	public Class<String[]> getEncodedClass() {
		return String[].class;
	}
}
