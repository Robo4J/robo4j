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

import java.util.Arrays;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageDTO {

    private int width;
    private int height;
    private String encoding;
    private byte[] content;

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
    public String toString() {
        return "CameraImageDTO{" +
                "width=" + width +
                ", height=" + height +
                ", encoding='" + encoding + '\'' +
                ", content=" + Arrays.toString(content) +
                '}';
    }
}
