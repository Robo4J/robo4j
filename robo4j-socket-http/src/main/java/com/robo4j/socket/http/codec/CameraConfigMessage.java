package com.robo4j.socket.http.codec;

import java.util.Objects;

/**
 * Camera Image configuration message
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraConfigMessage {

	private final Integer height;
	private final Integer width;
	private final Integer brightness;
	private final Integer sharpness;

	public CameraConfigMessage(Integer height, Integer width, Integer brightness, Integer sharpness) {
		this.height = height;
		this.width = width;
		this.brightness = brightness;
		this.sharpness = sharpness;
	}

	public Integer getHeight() {
		return height;
	}

	public Integer getWidth() {
		return width;
	}

	public Integer getBrightness() {
		return brightness;
	}

	public Integer getSharpness() {
		return sharpness;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CameraConfigMessage message = (CameraConfigMessage) o;
		return Objects.equals(height, message.height) && Objects.equals(width, message.width)
				&& Objects.equals(brightness, message.brightness) && Objects.equals(sharpness, message.sharpness);
	}

	@Override
	public int hashCode() {
		return Objects.hash(height, width, brightness, sharpness);
	}

	@Override
	public String toString() {
		return "CameraConfigMessage{" + "height=" + height + ", width=" + width + ", brightness='" + brightness + '\''
				+ ", sharpness='" + sharpness + '\'' + '}';
	}
}
