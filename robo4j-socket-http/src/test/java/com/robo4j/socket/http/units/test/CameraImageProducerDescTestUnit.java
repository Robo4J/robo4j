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

package com.robo4j.socket.http.units.test;

import com.robo4j.AttributeDescriptor;
import com.robo4j.ConfigurationException;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import com.robo4j.RoboUnit;
import com.robo4j.commons.ImageDTO;
import com.robo4j.configuration.Configuration;
import com.robo4j.socket.http.codec.CameraMessageCodec;
import com.robo4j.util.StreamUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageProducerDescTestUnit extends RoboUnit<Boolean> {

    public static final String ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME = "numberOfSentImages";
    public static final String ATTRIBUTE_NUMBER_OF_IMAGES_NAME = "numberOfImages";
    public static final Collection<AttributeDescriptor<?>> ATTRIBUTE_DESCRIPTORS = Collections.unmodifiableCollection(
            Arrays.asList(DefaultAttributeDescriptor.create(Integer.class, ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME),
                    DefaultAttributeDescriptor.create(Integer.class, ATTRIBUTE_NUMBER_OF_IMAGES_NAME)));


    private final CameraMessageCodec codec = new CameraMessageCodec();
    private final AtomicBoolean progress = new AtomicBoolean(false);
    private final AtomicInteger counter = new AtomicInteger(0);
    private String target;
    private String fileName;
    private Integer numberOfImages;



    public CameraImageProducerDescTestUnit(RoboContext context, String id) {
        super(Boolean.class, context, id);
    }

    @Override
    protected void onInitialization(Configuration configuration) throws ConfigurationException {
        target = configuration.getString("target", null);
        fileName = configuration.getString("fileName", null);
        numberOfImages = configuration.getInteger("numberOfImages", null);
    }

    @Override
    public void onMessage(Boolean message) {
        if (message) {
            createImage();
        }
    }

    @Override
    public void start() {
        EnumSet<LifecycleState> acceptedStates = EnumSet.of(LifecycleState.STARTING, LifecycleState.STARTED);
        getContext().getScheduler().execute(() -> {
            while (acceptedStates.contains(getState())) {
                if (progress.compareAndSet(false, true) && counter.getAndIncrement() < numberOfImages) {
                    createImage();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <R> R onGetAttribute(AttributeDescriptor<R> descriptor) {
        if (descriptor.getAttributeType() == Integer.class) {
            if (descriptor.getAttributeName().equals(ATTRIBUTE_NUMBER_OF_IMAGES_NAME)) {
                return (R) numberOfImages;
            } else if (descriptor.getAttributeName().equals(ATTRIBUTE_NUMBER_OF_SENT_IMAGES_NAME)) {
                return (R) Integer.valueOf(counter.get());
            }
        }
        return super.onGetAttribute(descriptor);
    }

    private void createImage() {
        final byte[] image = StreamUtils.inputStreamToByteArray(
                Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName));
        ImageDTO imageDTO = new ImageDTO(
                Integer.valueOf("800"),
                Integer.valueOf("600"),
                "jpg", image);
        getContext().getReference(target).sendMessage(imageDTO);
        progress.set(false);
    }
}
