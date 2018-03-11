/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

package com.robo4j.spring;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.spring.service.SimpleServiceImpl;
import com.robo4j.spring.unit.SimpleRoboSpringUnit;
import com.robo4j.util.PropertyMapBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboSpringTests {

	private static final String ROBO_SPRING_UNIT = "roboSpringUnit";
	private static final String MAGIC_MESSAGE = "magic message";

	@SuppressWarnings("unchecked")
	@Test
	public void simpleRoboSystemWithSimpleService() throws Exception {

		RoboContext system = getRoboSpringSystem();
		system.start();

		RoboReference<String> springUnit = system.getReference(ROBO_SPRING_UNIT);
		springUnit.sendMessage(MAGIC_MESSAGE);

        TimeUnit.MILLISECONDS.sleep(10);
		List<String> receivedMessages = (List<String>) springUnit
				.getAttribute(SimpleRoboSpringUnit.DESCRIPTOR_RECEIVED_MESSAGES).get();

		system.shutdown();

		Assert.assertTrue("message: " + receivedMessages, receivedMessages.contains("SPRING:".concat(MAGIC_MESSAGE)));

	}

	private RoboContext getRoboSpringSystem() throws Exception {
		/* system which is testing main system */

		final Map<String, Object> springComponents = new PropertyMapBuilder<String, Object>()
				.put(SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE, new SimpleServiceImpl()).create();

		final RoboBuilder result = new RoboBuilder();

		Configuration config = ConfigurationFactory.createEmptyConfiguration();
		config.setValue(RoboSpringRegisterUnit.PROPERTY_COMPONENTS, springComponents);
		result.add(RoboSpringRegisterUnit.class, config, RoboSpringRegisterUnit.NAME);

		config = ConfigurationFactory.createEmptyConfiguration();
		config.setString(SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE, SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE);
		result.add(SimpleRoboSpringUnit.class, config, ROBO_SPRING_UNIT);
		return result.build();
	}
}
