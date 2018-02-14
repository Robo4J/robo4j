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

package com.robo4j.socket.http.message;

import com.robo4j.socket.http.enums.StatusCode;

import java.util.Map;

/**
 * Outbound Http message used by Server units.
 * Message contains all necessary information about processing the response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpDecoratedResponse extends AbstractHttpDecoratedMessage {

	private final HttpResponseDenominator denominator;

	public HttpDecoratedResponse(Map<String, String> header, HttpResponseDenominator denominator) {
		super(header, denominator.getVersion());
		this.denominator = denominator;
	}

	@Override
	public HttpDenominator getDenominator() {
		return denominator;
	}

	public StatusCode getCode() {
		return denominator.getStatus();
	}

	@Override
	public String toString() {
		return "HttpDecoratedResponse{" + "denominator=" + denominator + '\'' + super.toString() + '}';
	}
}
