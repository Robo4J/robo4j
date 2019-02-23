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

import com.robo4j.RoboContext;
import com.robo4j.spring.configuration.robo4j.StringConsumer;
import com.robo4j.spring.configuration.service.SimpleScheduler;
import com.robo4j.spring.configuration.service.SpringReceiverService;
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

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Robo4JSpringUnitsTests testing communication between spring and robo4j ecosystems
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { SpringReceiverService.class, AbstractRobo4jSpringTest.Initializer.class,
		SimpleScheduler.class })
class Robo4JSpringUnitsTests {

	private static final Log log = LogFactory.getLog(Robo4JSpringUnitsTests.class);
	@Autowired
	private RoboContext roboContext;

	@Autowired
	private SpringReceiverService receiverService;

	@BeforeEach
	void setUp() {
		roboContext.start();
	}

	@AfterEach
	void stop() {
		roboContext.stop();
	}

	/**
	 * Spring app scheduler emmit messages, messages are send to the Robo4J unit
	 * @throws Exception exception
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	@Test
	void robo4jUnitAndSpringServiceDataExchangeTest() throws Exception {

		final CountDownLatch latch = roboContext.getReference(StringConsumer.NAME)
				.getAttribute(StringConsumer.DESCRIPTOR_COUNT_DOWN_LATCH).get();
		latch.await(20, TimeUnit.SECONDS);

		List<String> robo4jConsumerMessage = (List<String>) roboContext.getReference(StringConsumer.NAME)
				.getAttribute(StringConsumer.DESCRIPTOR_TOTAL_MESSAGES).get();

		log.info("Robo4j received message: " + robo4jConsumerMessage);
		log.info("Spring received message: " + receiverService.getMessages());
		Assertions.assertEquals(robo4jConsumerMessage, receiverService.getMessages());
	}
}
