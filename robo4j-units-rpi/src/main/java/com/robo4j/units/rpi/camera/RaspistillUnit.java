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
package com.robo4j.units.rpi.camera;

import com.robo4j.ConfigurationException;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.hw.rpi.camera.RaspiDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Unit generates image by using Rapsberry Pi raspistill utility
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 * @see <a href=
 * "https://www.raspberrypi.org/documentation/usage/camera/raspicam/raspistill.md">raspistill
 * documentation</a>
 */
public class RaspistillUnit extends RoboUnit<RaspistillRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RaspistillUnit.class);
    private static final EnumSet<LifecycleState> acceptedStates = EnumSet.of(LifecycleState.STARTING,
            LifecycleState.STARTED);
    private static final String PROPERTY_TARGET = "target";
    private final AtomicBoolean continualMode = new AtomicBoolean(false);
    private final AtomicBoolean cameraProgress = new AtomicBoolean(false);

    private final RaspiDevice device = new RaspiDevice();
    private String target;

    public RaspistillUnit(RoboContext context, String id) {
        super(RaspistillRequest.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString(PROPERTY_TARGET, null);
        if (target == null) {
            throw ConfigurationException.createMissingConfigNameException(PROPERTY_TARGET);
        }
    }

    @Override
    public void onMessage(RaspistillRequest message) {
        processMessage(message);
    }

    private void processMessage(RaspistillRequest message) {
        getContext().getScheduler().execute(() -> {
            if (continualMode.get()) {
                stopProgress();
            }
            if (message.isActive()) {
                startContinualMode(message);
            } else if (cameraProgress.compareAndSet(false, true)) {
                createImage(message);
            }
        });
    }

    private void stopProgress() {
        continualMode.set(false);
        while (cameraProgress.get()) ;
    }

    private void startContinualMode(RaspistillRequest message) {
        getContext().getScheduler().execute(() -> {
            continualMode.set(true);
            while (acceptedStates.contains(getState()) && continualMode.get()) {
                if (cameraProgress.compareAndSet(false, true)) {
                    createImage(message);
                }
            }
        });

    }

    private void createImage(RaspistillRequest message) {
        try {
            final byte[] image = device.executeCommandRaspistill(message.create());
            final RoboReference<ImageDTO> targetReference = getContext().getReference(target);
            if (targetReference != null && image.length > 0) {
                ImageDTO imageDTO = CameraUtil.createImageDTOBydMessageAndBytes(message, image);
                targetReference.sendMessage(imageDTO);
            }
            cameraProgress.set(false);
        } catch (Exception e) {
            LOGGER.error("create image:{}", e.getMessage(), e);
        }
    }

}
