package com.robo4j.units.rpi.camera;

import com.robo4j.commons.ImageDTO;
import com.robo4j.commons.ImageDTOBuilder;

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
