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

package com.robo4j.socket.http;

import java.util.Map;

/**
 * Generic message used by Server and Client units.
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpMessageDescriptor {

	final private Map<String, String> header;
	final private HttpMethod method;
	final private String version;
	final private String path;
	private Integer length;
	private String message;
	private String callbackUnit;


	public HttpMessageDescriptor(Map<String, String> header, HttpMethod method, String version, String path) {
		this.header = header;
		this.method = method;
		this.version = version;
		this.path = path;
		this.length = null;
		this.message = null;
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getVersion() {
		return version;
	}

	public String getPath() {
		return path;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length){
		this.length = length;
	}

	public String getMessage() {
		return message;
	}

	public void addMessage(String message) {
		this.message = this.message == null ? message : this.message.concat(message);
	}

	public String getCallbackUnit() {
		return callbackUnit;
	}


}
