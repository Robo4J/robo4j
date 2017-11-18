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

import java.util.Map;

/**
 * General Http Message properties
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public abstract class AbstractHttpMessageDescriptor {
	private final Map<String, String> header;
	private final String version;
	private int length;
	private String message;

	AbstractHttpMessageDescriptor(Map<String, String> header, String version) {
		this.header = header;
		this.version = version;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getMessage() {
		return message;
	}

	public void addMessage(String message) {
		this.message = this.message == null ? message : this.message.concat(message);
	}

	public Map<String, String> getHeader() {
		return header;
	}

	public String getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return "header=" + header +
				", version='" + version + '\'' +
				", length=" + length +
				", message='" + message + '\'' +
				'}';
	}
}
