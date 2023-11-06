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
package com.robo4j.units.rpi.camera;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class CameraUtil {

    public static ImageDTO createImageDTOBydMessageAndBytes(final RaspistillRequest message, final byte[] content){
        return ImageDTOBuilder.Build()
                .setWidth(Integer.valueOf(message.getProperty(RpiCameraProperty.WIDTH)))
                .setHeight(Integer.valueOf(message.getProperty(RpiCameraProperty.HEIGHT)))
                .setEncoding(message.getProperty(RpiCameraProperty.ENCODING))
                .setContent(content).build();
    }
}
