/*
 * Copyright (c) 2014, 2019, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring.configuration;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.net.LookupServiceProvider;
import com.robo4j.spring.configuration.net.MessageProducer;
import com.robo4j.spring.configuration.robo4j.StringConsumer;
import com.robo4j.spring.configuration.service.SpringReceiverService;
import com.robo4j.util.SystemUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Robo4JSpringUnitsNetworkingTests testing networking communication between
 * Robo4J systems and Spring
 *
 * necessary to add VM option: -Djava.net.preferIPv4Stack=true
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { SpringReceiverService.class, AbstractRobo4jSpringTest.Initializer.class })
class Robo4JSpringUnitsNetworkingTests {

	private static final Log log = LogFactory.getLog(Robo4JSpringUnitsNetworkingTests.class);

	@Autowired
	private RoboContext roboContext;

	@Autowired
	private SpringReceiverService receiverService;

	private RoboContext remoteSystem;

	@BeforeEach
	void setUp() {
		try {
			LookupServiceProvider.getDefaultLookupService().start();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		roboContext.start();
		remoteSystem = getRemoteSystem();
	}

	@AfterEach
	void cleanSetup() {
		try {
			LookupServiceProvider.getDefaultLookupService().stop();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		roboContext.stop();
		remoteSystem.shutdown();
	}

	/**
	 * Robo4J unit emit messages and send them by network discovery protocol to
	 * another Robo4j unit Spring application provides the message list
	 */
	@SuppressWarnings("unchecked")
	@Test
	void robo4jSystemsWithUnitsAndSpringServiceNetworkingTest() throws InterruptedException, ExecutionException {
		final int messagesNumber = 100;

		remoteSystem.start();

		final CountDownLatch producerLatch = remoteSystem.getReference(MessageProducer.NAME)
				.getAttribute(MessageProducer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		producerLatch.await(20, TimeUnit.SECONDS);

		final CountDownLatch consumerLatch = roboContext.getReference(StringConsumer.NAME)
				.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		consumerLatch.await(20, TimeUnit.SECONDS);

		final List<String> producedMessageList = (List<String>) remoteSystem.getReference(MessageProducer.NAME)
				.getAttribute(MessageProducer.DESCRIPTOR_MESSAGE_LIST).get();

		log.info("Robo4J producer messages: " + producedMessageList);
		log.info("Spring received messages: " + receiverService.getMessages());

		Assertions.assertEquals(messagesNumber, receiverService.getMessages().size());
	}

	private RoboContext getRemoteSystem() {
		final InputStream remoteSystemIS = SystemUtil.getInputStreamByResourceName("robo4jRemoteSystem.xml");
		final InputStream remoteContextIS = SystemUtil.getInputStreamByResourceName("robo4jRemoteContext.xml");
		try {
			return new RoboBuilder(remoteSystemIS).add(remoteContextIS).build();
		} catch (RoboBuilderException e) {
			throw new IllegalStateException(e);
		}
	}

}
