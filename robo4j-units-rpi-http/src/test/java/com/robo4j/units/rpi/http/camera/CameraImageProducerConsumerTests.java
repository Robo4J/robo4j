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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class CameraImageProducerConsumerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraImageProducerConsumerTests.class);

    @Test
    void cameraImageProdConTest() throws Exception {

        RoboBuilder builderProducer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemProducer.xml"));
        InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_producer_test.xml");
        builderProducer.add(clientConfigInputStream);
        RoboContext producerSystem = builderProducer.build();

        RoboBuilder builderConsumer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemConsumer.xml"));
        InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_consumer_test.xml");
        builderConsumer.add(serverConfigInputStream);

        RoboContext consumerSystem = builderConsumer.build();

        long startTime = System.currentTimeMillis();
        consumerSystem.start();
        producerSystem.start();

        RoboReference<CameraMessage> imageConsumer = consumerSystem.getReference("imageProcessor");
        RoboReference<Boolean> imageProducer = producerSystem.getReference("imageController");

        CountDownLatch startConsumerLatch = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_START_LATCH).get();
        startConsumerLatch.await(5, TimeUnit.MINUTES);

        Integer totalImagesProducer = imageProducer.getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_TOTAL_IMAGES)
                .get();
        CountDownLatch imageProducerLatch = imageProducer
                .getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_GENERATED_IMAGES_LATCH).get();
        CountDownLatch imageConsumerLatch = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_IMAGES_LATCH).get();

        LOGGER.info("LATCH");
        imageProducerLatch.await(5, TimeUnit.MINUTES);
        LOGGER.info("ONE");
        imageConsumerLatch.await(5, TimeUnit.MINUTES);
        LOGGER.info("TWO");

        Integer receivedImagesConsumer = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_RECEIVED_IMAGES).get();
        assertEquals(totalImagesProducer, receivedImagesConsumer);

        RoboHttpUtils.printMeasuredTime(getClass(), "duration", startTime);
        LOGGER.info("receivedImagesConsumer: " + receivedImagesConsumer);
        producerSystem.shutdown();
        consumerSystem.shutdown();

    }
}
