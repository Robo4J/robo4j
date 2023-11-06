/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.socket.http.enums.VideoMessageType;

import java.io.Serializable;
import java.util.Objects;

/**
 * VideoConfigMessage configuration message for the RaspividUnit
 * from robo4j-unit-rpi module
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class VideoConfigMessage implements Serializable {
	private static final long serialVersionUID = 1L;
	private VideoMessageType type;
    private Integer height;
    private Integer width;
    private Integer rotation;
    private Integer timeout;

    public VideoConfigMessage() {
    }

    public VideoConfigMessage(VideoMessageType type, Integer height, Integer width, Integer rotation, Integer timeout) {
        this.type = type;
        this.height = height;
        this.width = width;
        this.rotation = rotation;
        this.timeout = timeout;
    }

    public VideoMessageType getType() {
        return type;
    }

    public void setType(VideoMessageType type) {
        this.type = type;
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

    public Integer getRotation() {
        return rotation;
    }

    public void setRotation(Integer rotation) {
        this.rotation = rotation;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoConfigMessage that = (VideoConfigMessage) o;
        return type == that.type &&
                Objects.equals(height, that.height) &&
                Objects.equals(width, that.width) &&
                Objects.equals(rotation, that.rotation) &&
                Objects.equals(timeout, that.timeout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, height, width, rotation, timeout);
    }

    @Override
    public String toString() {
        return "VideoConfigMessage{" +
                "type=" + type +
                ", height=" + height +
                ", width=" + width +
                ", rotation=" + rotation +
                ", timeout=" + timeout +
                '}';
    }
}
