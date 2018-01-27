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

package com.robo4j.units.rpi.http.camera;

import com.robo4j.AttributeDescriptor;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.util.RoboHttpUtils;

import org.junit.Test;

import java.io.InputStream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraDecoratedImageProducerConsumerTests {

    AttributeDescriptor<Integer> ATTRIBUTE_NUMBER_OF_IMAGES = new DefaultAttributeDescriptor<>(Integer.class,
            CameraImageProducerDesTestUnit.ATTRIBUTE_NUMBER_OF_IMAGES_NAME);
    AttributeDescriptor<Integer> ATTRIBUTE_COUNTER = new DefaultAttributeDescriptor<>(Integer.class,
            CameraImageConsumerTestUnit.ATTRIBUTE_NUMBER_OF_RECEIVED_IMAGES_NAME);


    @Test
    public void decoratorProducerConsumerTest() throws Exception {

        RoboBuilder builderProducer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemTest.xml"));
        InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_producer_decorated_test.xml");
        builderProducer.add(clientConfigInputStream);
        RoboContext producerSystem = builderProducer.build();

        RoboBuilder builderConsumer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemTest.xml"));
        InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_consumer_decorated_test.xml");
        builderConsumer.add(serverConfigInputStream);
        RoboContext consumerSystem = builderConsumer.build();

        long startTime = System.currentTimeMillis();
        consumerSystem.start();
        producerSystem.start();

        RoboReference<Boolean> imageProducer = producerSystem.getReference("imageController");
        RoboReference<CameraMessage> imageConsumer = consumerSystem.getReference("imageProcessor");

        Integer numberOfImages = imageProducer.getAttribute(ATTRIBUTE_NUMBER_OF_IMAGES).get();
        while (imageConsumer.getAttribute(ATTRIBUTE_COUNTER).get() < numberOfImages) {
        }
        RoboHttpUtils.printMeasuredTime(getClass(), "duration", startTime);
        System.out.println("sendImages: " + numberOfImages);
        producerSystem.shutdown();
        consumerSystem.shutdown();

        System.out.println("Press any key to End...");

    }

}
