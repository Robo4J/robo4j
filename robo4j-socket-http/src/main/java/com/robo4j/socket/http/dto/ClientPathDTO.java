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

package com.robo4j.socket.http.dto;

import com.robo4j.socket.http.HttpMethod;

import java.util.List;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ClientPathDTO {

	private String roboUnit;
	private HttpMethod method;
	private List<String> callbacks;

	public ClientPathDTO(){

	}

	public ClientPathDTO(String roboUnit, HttpMethod method, List<String> callbacks) {
		this.roboUnit = roboUnit;
		this.method = method;
		this.callbacks = callbacks;
	}

	public String getRoboUnit() {
		return roboUnit;
	}

	public void setRoboUnit(String roboUnit) {
		this.roboUnit = roboUnit;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public List<String> getCallbacks() {
		return callbacks;
	}

	public void setCallbacks(List<String> callbacks) {
		this.callbacks = callbacks;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ClientPathDTO that = (ClientPathDTO) o;
		return Objects.equals(roboUnit, that.roboUnit) &&
				method == that.method &&
				Objects.equals(callbacks, that.callbacks);
	}

	@Override
	public int hashCode() {

		return Objects.hash(roboUnit, method, callbacks);
	}

	@Override
	public String toString() {
		return "ClientPathDTO{" + "roboUnit='" + roboUnit + '\'' + ", method=" + method + ", callbacks="
				+ callbacks + '}';
	}
}
