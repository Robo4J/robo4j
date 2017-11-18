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
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpResponseDescriptor extends AbstractHttpMessageDescriptor {

	private final StatusCode code;
	private String callbackUnit;

	public HttpResponseDescriptor(Map<String, String> header, StatusCode code, String version) {
		super(header, version);
		this.code = code;
	}

	public StatusCode getCode() {
		return code;
	}

    /**
     * define unit RoboUnit to be call after response message
     *
     * @param callbackUnit robo unit name that should be called
     */
	public String getCallbackUnit() {
		return callbackUnit;
	}

	public void setCallbackUnit(String callbackUnit) {
		this.callbackUnit = callbackUnit;
	}

	@Override
	public String toString() {
		return "HttpResponseDescriptor{" + "code=" + code + ", callbackUnit='" + callbackUnit + '\''
				+ super.toString() + '}';
	}
}
