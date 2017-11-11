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

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.ProtocolType;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpFirstLineBuilder {
	private volatile StringBuilder sb;

	private HttpFirstLineBuilder(String value) {
		sb = new StringBuilder(value);
	}

	public static HttpFirstLineBuilder Build(String startValue) {
		return new HttpFirstLineBuilder(startValue);
	}

	public HttpFirstLineBuilder addProtocolAndHostAndPath(ProtocolType protocol, String host, String path){
		sb.append(HttpMessageUtil.SPACE)
				.append(protocol.getName())
				.append(HttpMessageUtil.getHttpSeparator(12))
				.append(HttpMessageUtil.getHttpSeparator(12))
				.append(host);
				if(protocol.equals(ProtocolType.HTTPS)){
					sb.append(HttpMessageUtil.getHttpSeparator(9))
							.append(protocol.getPort());
				}
				sb.append(HttpMessageUtil.getHttpSeparator(12))
				.append(path);
		return this;

	}

	public HttpFirstLineBuilder add(Object value) {
		sb.append(HttpMessageUtil.SPACE).append(value);
		return this;
	}

	public String build() {
		return sb.append(HttpMessageUtil.NEXT_LINE).toString();
	}
}
