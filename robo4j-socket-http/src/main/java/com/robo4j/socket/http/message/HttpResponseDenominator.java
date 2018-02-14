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

package com.robo4j.socket.http.message;

import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.enums.StatusCode;
import com.robo4j.socket.http.message.HttpDenominator;

import static com.robo4j.socket.http.util.HttpMessageUtils.SPACE;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class HttpResponseDenominator implements HttpDenominator {

	private final StringBuilder sb = new StringBuilder();
	private final StatusCode status;
	private final HttpVersion version;

	public HttpResponseDenominator(StatusCode status, HttpVersion version) {
		this.status = status;
		this.version = version;
	}

	public StatusCode getStatus() {
		return status;
	}

	@Override
	public String  getVersion() {
		return version.getValue();
	}

	/**
	 * example: HTTP/1.1 200 OK
	 * 
	 * @return response 1st line
	 */
	@Override
	public String generate() {
        assert status != null;
        assert version != null;
	    //@formatter:off
		return sb.append(getVersion())
                .append(SPACE)
                .append(status.getCode())
                .append(SPACE)
				.append(status.getReasonPhrase())
                .toString();
	    //@formatter:on
	}

	@Override
	public String toString() {
		return "HttpResponseDenominator{" +
				"status=" + status +
				", version=" + version +
				'}';
	}
}
