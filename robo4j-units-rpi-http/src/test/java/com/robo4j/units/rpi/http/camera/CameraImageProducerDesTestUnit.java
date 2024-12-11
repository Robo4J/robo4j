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

import com.robo4j.AttributeDescriptor;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.enums.SystemPath;
import com.robo4j.socket.http.units.ClientMessageWrapper;
import com.robo4j.socket.http.util.HttpPathUtils;
import com.robo4j.socket.http.util.JsonUtil;
import com.robo4j.util.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageProducerDesTestUnit extends RoboUnit<Boolean> {


    public static final String ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME = "numberOfSentImages";
    public static final String ATTR_TOTAL_IMAGES = "numberOfImages";
    public static final String ATTR_GENERATED_IMAGES_LATCH = "generatedImagesLatch";
    public static final DefaultAttributeDescriptor<CountDownLatch> DESCRIPTOR_GENERATED_IMAGES_LATCH = DefaultAttributeDescriptor
            .create(CountDownLatch.class, ATTR_GENERATED_IMAGES_LATCH);
    public static final AttributeDescriptor<Integer> DESCRIPTOR_TOTAL_IMAGES = new DefaultAttributeDescriptor<>(Integer.class,
            CameraImageProducerDesTestUnit.ATTR_TOTAL_IMAGES);
    protected static final String IMAGE_ENCODING = "jpg";
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraImageProducerDesTestUnit.class);
    protected final AtomicBoolean progress = new AtomicBoolean(false);
    protected String target;
    protected String httpTarget;
    protected String fileName;
    private final AtomicInteger counter = new AtomicInteger(0);
    protected volatile CountDownLatch generatedImagesLatch;
    private Integer numberOfImages;

    public CameraImageProducerDesTestUnit(RoboContext context, String id) {
        super(Boolean.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) {
        target = configuration.getString("target", null);
        httpTarget = configuration.getString("httpTarget", null);
        fileName = configuration.getString("fileName", null);
        numberOfImages = configuration.getInteger(ATTR_TOTAL_IMAGES, null);
        generatedImagesLatch = new CountDownLatch(numberOfImages);
    }

    @Override
    public void onMessage(Boolean message) {
        if (message) {
            LOGGER.info("message:{}", message);
            createImage(counter.get());
        }
    }

    @Override
    public void start() {
        EnumSet<LifecycleState> acceptedStates = EnumSet.of(LifecycleState.STARTING, LifecycleState.STARTED);
        getContext().getScheduler().execute(() -> {
            while (acceptedStates.contains(getState())) {
                if (progress.compareAndSet(false, true) && counter.getAndIncrement() < numberOfImages) {
                    createImage(counter.get());
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected synchronized <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
        if (descriptor.attributeType() == Integer.class) {
            if (descriptor.attributeName().equals(ATTR_TOTAL_IMAGES)) {
                return (R) numberOfImages;
            } else if (descriptor.attributeName().equals(ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME)) {
                return (R) Integer.valueOf(counter.get());
            }
        }
        if (descriptor.attributeName().equals(ATTR_GENERATED_IMAGES_LATCH) && descriptor.attributeType() == CountDownLatch.class) {
            return (R) generatedImagesLatch;
        }
        return super.onGetAttribute(descriptor);
    }

    protected void countDonwSentImage() {

    }

    protected void createImage(int imageNumber) {
        final byte[] image = StreamUtils
                .inputStreamToByteArray(Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
        final CameraMessage cameraMessage = new CameraMessage(IMAGE_ENCODING, String.valueOf(imageNumber),
                JsonUtil.toBase64String(image));
        final ClientMessageWrapper resultMessage = new ClientMessageWrapper(
                HttpPathUtils.toPath(SystemPath.UNITS.getPath(), httpTarget), CameraMessage.class, cameraMessage);
        getContext().getReference(target).sendMessage(resultMessage);
        generatedImagesLatch.countDown();
        progress.set(false);
    }
}
