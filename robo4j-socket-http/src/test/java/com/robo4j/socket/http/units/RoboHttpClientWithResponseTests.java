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

import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.socket.http.units.test.SocketMessageDecoratedProducerUnit;
import com.robo4j.socket.http.units.test.StringConsumer;
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * testing http method GET with response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */

public class RoboHttpClientWithResponseTests {
	private static final int TIMEOUT = 20;
	private static final TimeUnit TIME_UNIT = TimeUnit.SECONDS;
	private static final Integer MAX_NUMBER = 20;
	private static final String ROBO_SYSTEM_DESC = "[{\"id\":\"stringConsumer\",\"state\":\"STARTED\"},{\"id\":\"httpServer\",\"state\":\"STARTED\"}]";

	@SuppressWarnings("unchecked")
	@Test
	@Ignore
	public void simpleRoboSystemGetRequestTest() throws Exception {

		RoboContext producerSystem = RoboContextUtils
				.loadRoboContextByXml("robo_http_client_request_producer_text.xml");
		RoboContext consumerSystem = RoboContextUtils
				.loadRoboContextByXml("robo_http_client_request_consumer_text.xml");

		consumerSystem.start();
		producerSystem.start();

		System.out.println("consumer: State after start:");
		System.out.println(SystemUtil.printStateReport(consumerSystem));

		System.out.println("producer: State after start:");
		System.out.println(SystemUtil.printStateReport(producerSystem));

		RoboReference<Integer> decoratedProducer = producerSystem.getReference("decoratedProducer");
		CountDownLatch producerSetupLatch = decoratedProducer
				.getAttribute(SocketMessageDecoratedProducerUnit.DESCRIPTOR_SETUP_LATCH).get();
		decoratedProducer.sendMessage(MAX_NUMBER);
		producerSetupLatch.await(TIMEOUT, TIME_UNIT);
		CountDownLatch producerLatch = decoratedProducer
				.getAttribute(SocketMessageDecoratedProducerUnit.DESCRIPTOR_MESSAGES_LATCH).get();
		producerLatch.await(TIMEOUT, TIME_UNIT);

		final RoboReference<String> producerStringConsumer = producerSystem.getReference(StringConsumer.NAME);
		final CountDownLatch messagesLatchStringConsumer = producerStringConsumer
				.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_LATCH).get();
		messagesLatchStringConsumer.await(TIMEOUT, TIME_UNIT);

		final Integer totalNumber = producerStringConsumer.getAttribute(StringConsumer.DESCRIPTOR_MESSAGES_TOTAL).get();
		Assert.assertEquals(MAX_NUMBER, totalNumber);

		producerSystem.shutdown();
		consumerSystem.shutdown();
	}

}
