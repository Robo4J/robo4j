/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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

import java.io.Serializable;
import java.util.Objects;

/**
 * Camera Image configuration message
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraConfigMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer height;
	private Integer width;
	private Integer brightness;
	private Integer sharpness;
	private Integer timeout;
	private Integer timelapse;

	public CameraConfigMessage() {
	}

	public CameraConfigMessage(Integer height, Integer width, Integer brightness, Integer sharpness, Integer timeout,
			Integer timelapse) {
		this.height = height;
		this.width = width;
		this.brightness = brightness;
		this.sharpness = sharpness;
		this.timeout = timeout;
		this.timelapse = timelapse;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getBrightness() {
		return brightness;
	}

	public void setBrightness(Integer brightness) {
		this.brightness = brightness;
	}

	public Integer getSharpness() {
		return sharpness;
	}

	public void setSharpness(Integer sharpness) {
		this.sharpness = sharpness;
	}

	public Integer getTimeout() {
		return timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	public Integer getTimelapse() {
		return timelapse;
	}

	public void setTimelapse(Integer timelapse) {
		this.timelapse = timelapse;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CameraConfigMessage message = (CameraConfigMessage) o;
		return Objects.equals(height, message.height) && Objects.equals(width, message.width)
				&& Objects.equals(brightness, message.brightness) && Objects.equals(sharpness, message.sharpness)
				&& Objects.equals(timeout, message.timeout) && Objects.equals(timelapse, message.timelapse);
	}

	@Override
	public int hashCode() {

		return Objects.hash(height, width, brightness, sharpness, timeout, timelapse);
	}

	@Override
	public String toString() {
		return "CameraConfigMessage{" + "height=" + height + ", width=" + width + ", brightness=" + brightness
				+ ", sharpness=" + sharpness + ", timeout=" + timeout + ", timelapse=" + timelapse + '}';
	}
}
