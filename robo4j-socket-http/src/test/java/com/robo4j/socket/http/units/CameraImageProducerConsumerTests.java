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

package com.robo4j.socket.http.units;

import com.robo4j.AttributeDescriptor;
import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.units.test.CameraImageConsumerTestUnit;
import com.robo4j.socket.http.units.test.CameraImageProducerTestUnit;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraImageProducerConsumerTests {
    AttributeDescriptor<Integer> ATTRIBUTE_NUMBER_OF_IMAGES= new DefaultAttributeDescriptor<>(Integer.class, CameraImageProducerTestUnit.ATTRIBUTE_NUMBER_OF_IMAGES_NAME);
    AttributeDescriptor<Integer> ATTRIBUTE_COUNTER = new DefaultAttributeDescriptor<>(Integer.class, CameraImageConsumerTestUnit.ATTRIBUTE_NUMBER_OF_RECEIVED_IMAGES_NAME);

    @Ignore
    @Test
    public void simpleTest() throws  Exception{

        RoboBuilder builderProducer = new RoboBuilder(Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemTest.xml"));
        InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo_camera_producer_test.xml");
        builderProducer.add(clientConfigInputStream);
        RoboContext producerSystem = builderProducer.build();

        RoboBuilder builderConsumer = new RoboBuilder(Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemTest.xml"));
        InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("robo_camara_consumer_test.xml");
        builderConsumer.add(serverConfigInputStream);
        RoboContext consumerSystem = builderConsumer.build();

        consumerSystem.start();
        producerSystem.start();

        RoboReference<CameraImageProducerConsumerTests> imageProducer = producerSystem.getReference("imageController");
        RoboReference<CameraImageConsumerTestUnit> imageConsumer = consumerSystem.getReference("imageProcessor");

        Integer numberOfSentImages = imageProducer.getAttribute(ATTRIBUTE_NUMBER_OF_IMAGES).get() - 1;
        while(!imageConsumer.getAttribute(ATTRIBUTE_COUNTER).get().equals(numberOfSentImages) ){
        }
        System.out.println("sendImages: " + (numberOfSentImages + 1));
        producerSystem.shutdown();
        consumerSystem.shutdown();

        System.out.println("Press any key to End...");

    }
}
