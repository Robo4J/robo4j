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

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.util.HttpDenominator;
import com.robo4j.socket.http.util.RequestDenominator;

import java.util.Map;

/**
 * Inbound Http message used by Server units.
 * Message does contains all necessary information for processing the request
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpDecoratedRequest extends AbstractHttpDecoratedMessage {

	private final RequestDenominator denominator;

	public HttpDecoratedRequest(RequestDenominator denominator){
		super(denominator.getVersion());
		this.denominator = denominator;
	}

	public HttpDecoratedRequest(Map<String, String> header, RequestDenominator denominator) {
		super(header, denominator.getVersion());
		this.denominator = denominator;
	}

	@Override
	public HttpDenominator getDenominator() {
		return denominator;
	}

	public HttpMethod getMethod() {
		return denominator.getMethod();
	}

	public String getPath() {
		return denominator.getPath();
	}


	@Override
	public String toString() {
		return "HttpDecoratedRequest{" + " denominator=" + denominator + "\' " + super.toString() + '}';
	}
}
