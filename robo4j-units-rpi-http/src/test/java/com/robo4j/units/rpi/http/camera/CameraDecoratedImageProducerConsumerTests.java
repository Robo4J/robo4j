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
import com.robo4j.RoboReference;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
class CameraDecoratedImageProducerConsumerTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(CameraDecoratedImageProducerConsumerTests.class);

    @Test
    void decoratorProducerConsumerTest() throws Exception {

        var builderProducer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemProducer.xml"));
        var clientConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_producer_decorated_test.xml");
        builderProducer.add(clientConfigInputStream);
        var producerSystem = builderProducer.build();

        var builderConsumer = new RoboBuilder(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemConsumer.xml"));
        var serverConfigInputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("robo_camera_consumer_decorated_test.xml");
        builderConsumer.add(serverConfigInputStream);
        var consumerSystem = builderConsumer.build();

        long startTime = System.currentTimeMillis();
        consumerSystem.start();
        producerSystem.start();

        RoboReference<Boolean> imageProducer = producerSystem.getReference("imageController");
        RoboReference<CameraMessage> imageConsumer = consumerSystem.getReference("imageProcessor");

        var startLatchConsumer = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_START_LATCH).get();
        startLatchConsumer.await(5, TimeUnit.MINUTES);

        var imagesLatchProducer = imageProducer
                .getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_GENERATED_IMAGES_LATCH).get();
        imagesLatchProducer.await(5, TimeUnit.MINUTES);
        Integer totalImagesProducer = imageProducer.getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_TOTAL_IMAGES)
                .get();

        var imagesLatchConsumer = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_IMAGES_LATCH).get();
        imagesLatchConsumer.await(5, TimeUnit.MINUTES);
        Integer totalImagesConsumer = imageConsumer.getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_RECEIVED_IMAGES)
                .get();
        RoboHttpUtils.printMeasuredTime(getClass(), "duration", startTime);

        assertEquals(totalImagesProducer, totalImagesConsumer);

        producerSystem.shutdown();
        consumerSystem.shutdown();

        LOGGER.info("Press <Enter> to quit!");

    }

}
