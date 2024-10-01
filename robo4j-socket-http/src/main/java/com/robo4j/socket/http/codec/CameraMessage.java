/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.codec;

import java.util.Objects;

/**
 * Camera message contains description and image as byte array used for http
 * codec.
 *
 * note: example joystick example
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraMessage {

	private String type;
	private String value;
	private String image;

	public CameraMessage() {
	}

	public CameraMessage(String type, String value, String image) {
		this.type = type;
		this.value = value;
		this.image = image;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		CameraMessage that = (CameraMessage) o;
		return Objects.equals(type, that.type) &&
				Objects.equals(value, that.value) &&
				Objects.equals(image, that.image);
	}

	@Override
	public int hashCode() {

		return Objects.hash(type, value, image);
	}

	@Override
	public String toString() {
		return "{" + "\"type\":\"" + type + "\"" + ",\"value\":\"" + value + "\"" + ",\"image\":\"" + image + "\"}";
	}
}
