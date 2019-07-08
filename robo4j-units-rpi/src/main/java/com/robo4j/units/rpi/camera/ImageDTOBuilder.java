/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.units.rpi.camera;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ImageDTOBuilder {

    private Integer width;
    private Integer height;
    private String encoding;
    private byte[] content;
    private ImageDTOBuilder(){
    }

    public static ImageDTOBuilder Build(){
        return new ImageDTOBuilder();
    }

    public ImageDTOBuilder setWidth(Integer width) {
        this.width = width;
        return this;
    }

    public ImageDTOBuilder setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public ImageDTOBuilder setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    public ImageDTOBuilder setContent(byte[] content) {
        this.content = content;
        return this;
    }

    public ImageDTO build(){
        return new ImageDTO(width, height, encoding, content);
    }
}
