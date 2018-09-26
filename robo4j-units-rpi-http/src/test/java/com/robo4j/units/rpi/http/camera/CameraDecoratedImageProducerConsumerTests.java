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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.util.RoboHttpUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class CameraDecoratedImageProducerConsumerTests {

	@Test
	public void decoratorProducerConsumerTest() throws Exception {

		RoboBuilder builderProducer = new RoboBuilder(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemProducer.xml"));
		InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_camera_producer_decorated_test.xml");
		builderProducer.add(clientConfigInputStream);
		RoboContext producerSystem = builderProducer.build();

		RoboBuilder builderConsumer = new RoboBuilder(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemConsumer.xml"));
		InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_camera_consumer_decorated_test.xml");
		builderConsumer.add(serverConfigInputStream);
		RoboContext consumerSystem = builderConsumer.build();

		long startTime = System.currentTimeMillis();
		consumerSystem.start();
		producerSystem.start();

		RoboReference<Boolean> imageProducer = producerSystem.getReference("imageController");
        RoboReference<CameraMessage> imageConsumer = consumerSystem.getReference("imageProcessor");

        CountDownLatch startLatchConsumer = imageConsumer
                .getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_START_LATCH).get();
        startLatchConsumer.await(5, TimeUnit.MINUTES);

		CountDownLatch imagesLatchProducer = imageProducer
				.getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_GENERATED_IMAGES_LATCH).get();
		imagesLatchProducer.await(5, TimeUnit.MINUTES);
		Integer totalImagesProducer = imageProducer.getAttribute(CameraImageProducerDesTestUnit.DESCRIPTOR_TOTAL_IMAGES)
				.get();

		CountDownLatch imagesLatchConsumer = imageConsumer
				.getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_IMAGES_LATCH).get();
		imagesLatchConsumer.await(5, TimeUnit.MINUTES);
		Integer totalImagesConsumer = imageConsumer.getAttribute(CameraImageConsumerTestUnit.DESCRIPTOR_RECEIVED_IMAGES)
				.get();
		RoboHttpUtils.printMeasuredTime(getClass(), "duration", startTime);

        Assert.assertEquals(totalImagesProducer, totalImagesConsumer);

		producerSystem.shutdown();
		consumerSystem.shutdown();

		System.out.println("Press any key to End...");

	}

}
