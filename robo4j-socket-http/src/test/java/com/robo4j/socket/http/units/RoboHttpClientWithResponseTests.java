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
import com.robo4j.util.SystemUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * testing http method GET with response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpClientWithResponseTests {
	private static final int MAX_NUMBER = 2;
	private static final String ROBO_SYSTEM_DESC = "[{\"id\":\"stringConsumer\",\"com.robo4j.LifecycleState\":\"STARTED\"}"
			+ ",{\"id\":\"httpServer\",\"com.robo4j.LifecycleState\":\"STARTED\"}]";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void simpleRoboSystemGetRequestTest() throws Exception {

		RoboContext producerSystem = getProducer();
		RoboContext consumerSystem = getConsumer();

		consumerSystem.start();
		producerSystem.start();

		System.out.println("consumer: State after start:");
		System.out.println(SystemUtil.printStateReport(consumerSystem));

		System.out.println("producer: State after start:");
		System.out.println(SystemUtil.printStateReport(producerSystem));

		RoboReference<Integer> descriptorProducer = producerSystem.getReference("descriptorProducer");
		descriptorProducer.sendMessage(MAX_NUMBER);
		RoboReference<String> stringConsumer = producerSystem.getReference("stringConsumer");

		AttributeDescriptor<List> receivedMessagesAttribute = new DefaultAttributeDescriptor<>(List.class,
				"getReceivedMessages");
		AttributeDescriptor<Integer> setMessagesAttribute = new DefaultAttributeDescriptor<>(Integer.class,
				"getNumberOfSentMessages");
		while (stringConsumer.getAttribute(setMessagesAttribute).get() < MAX_NUMBER) {
		}
		List<String> receivedMessageList = (List<String>) stringConsumer.getAttribute(receivedMessagesAttribute).get();
		Assert.assertTrue(stringConsumer.getAttribute(setMessagesAttribute).get() == MAX_NUMBER);
		Assert.assertTrue(receivedMessageList.contains(ROBO_SYSTEM_DESC));

		producerSystem.shutdown();
		consumerSystem.shutdown();

	}

	private RoboContext getProducer() throws Exception {
		RoboBuilder builderProducer = new RoboBuilder();
		InputStream clientConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_client_request_producer_text.xml");
		builderProducer.add(clientConfigInputStream);
		return builderProducer.build();
	}

	private RoboContext getConsumer() throws Exception {
		RoboBuilder builderConsumer = new RoboBuilder();
		InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_client_request_consumer_text.xml");
		builderConsumer.add(serverConfigInputStream);
		return builderConsumer.build();
	}

}
