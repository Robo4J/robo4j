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
package com.robo4j.units.rpi.http.camera;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageDTO {

    private final int width;
    private final int height;
    private final String encoding;
    private final byte[] content;

    public CameraImageDTO(int width, int height, String encoding, byte[] content) {
        this.width = width;
        this.height = height;
        this.encoding = encoding;
        this.content = content;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getEncoding() {
        return encoding;
    }

    public byte[] getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CameraImageDTO cameraImageDTO = (CameraImageDTO) o;
        return width == cameraImageDTO.width &&
                height == cameraImageDTO.height &&
                Objects.equals(encoding, cameraImageDTO.encoding) &&
                Arrays.equals(content, cameraImageDTO.content);
    }

    @Override
    public int hashCode() {

        int result = Objects.hash(width, height, encoding);
        result = 31 * result + Arrays.hashCode(content);
        return result;
    }

    @Override
    public String toString() {
        return "CameraImageDTO{" +
                "width=" + width +
                ", height=" + height +
                ", encoding='" + encoding + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
