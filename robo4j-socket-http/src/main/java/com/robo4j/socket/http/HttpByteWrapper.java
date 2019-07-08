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
package com.robo4j.socket.http;

import java.util.Arrays;

/**
 * Wrapper to Http Message.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpByteWrapper {

	private final String[] header;
	private final String body;

	public HttpByteWrapper(String[] header, String body) {
		this.header = header;
		this.body = body;
	}

	public String[] getHeader() {
		return header;
	}

	public String getBody() {
		return body;
	}

	@Override
	public String toString() {
		return "HttpByteWrapper{" + "header=" + Arrays.asList(header) + ", body=" + body + '}';
	}
}
