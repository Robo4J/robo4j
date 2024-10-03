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

import com.robo4j.RoboContext;
import com.robo4j.util.StreamUtils;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraImageProducerTestUnit extends CameraImageProducerDesTestUnit {
    public CameraImageProducerTestUnit(RoboContext context, String id) {
        super(context, id);
    }


    @Override
    protected void createImage(int imageNumber) {

        final byte[] image = StreamUtils
                .inputStreamToByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
        final CameraImageDTO cameraImageDTO = CameraImageDTOBuilder.Build()
                .setWidth(800)
                .setHeight(600)
                .setEncoding(IMAGE_ENCODING)
                .setContent(image).build();
        getContext().getReference(target).sendMessage(cameraImageDTO);
        generatedImagesLatch.countDown();
        progress.set(false);

    }
}
