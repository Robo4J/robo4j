package com.robo4j.units.rpi.http.camera;

import com.robo4j.RoboContext;
import com.robo4j.units.rpi.camera.ImageDTO;
import com.robo4j.units.rpi.camera.ImageDTOBuilder;
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
        final ImageDTO imageDTO = ImageDTOBuilder.Build()
                .setWidth(800)
                .setHeight(600)
                .setEncoding("jpg")
                .setContent(image).build();
        getContext().getReference(target).sendMessage(imageDTO);

        progress.set(false);

    }
}
