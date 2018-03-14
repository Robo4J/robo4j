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

import com.robo4j.DefaultAttributeDescriptor;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationFactory;
import com.robo4j.spring.service.SimpleService;
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
	public void withServiceTest() throws Exception {

		RoboContext system = getRoboWithServiceSystem(TestMode.WITH_SERVICE);
		system.start();

		RoboReference<String> springUnit = system.getReference(ROBO_SPRING_UNIT);
		springUnit.sendMessage(MAGIC_MESSAGE);

		TimeUnit.MILLISECONDS.sleep(10);
		List<String> receivedMessages = (List<String>) springUnit
				.getAttribute(SimpleRoboSpringUnit.DESCRIPTOR_RECEIVED_MESSAGES).get();

		system.shutdown();

		Assert.assertTrue("message: " + receivedMessages, receivedMessages.contains("SPRING:".concat(MAGIC_MESSAGE)));

	}

	@Test(expected = RoboBuilderException.class)
	public void withoutServiceTest() throws Exception {
		RoboContext system = getRoboWithServiceSystem(TestMode.WITHOUT_SERVICE);
		system.start();

	}

	@Test
	public void getRegisteredComponentTest() throws Exception {

		DefaultAttributeDescriptor<Object> REGISTERED_SPRING_COMPONENT = DefaultAttributeDescriptor
				.create(Object.class, SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE);
		RoboContext system = getRoboWithServiceSystem(TestMode.WITH_SERVICE);
		system.start();

		RoboReference<Object> registerReference = system.getReference(RoboSpringRegisterUnit.NAME);

		Object simpleServiceReference = registerReference.getAttribute(REGISTERED_SPRING_COMPONENT).get();
		SimpleService simpleService  = SimpleService.class.cast(simpleServiceReference);

		System.out.println("component: " + simpleService.getRandom());
		Assert.assertNotNull(simpleService.getRandom());
	}

	private enum TestMode {
		WITH_SERVICE, WITHOUT_SERVICE
	}

	private RoboContext getRoboWithServiceSystem(TestMode testMode) throws Exception {
		/* system which is testing main system */

		final RoboBuilder result = new RoboBuilder();
		Configuration configSpringUnit = ConfigurationFactory.createEmptyConfiguration();

		switch (testMode) {
		case WITH_SERVICE:

			//register all spring beans use by unit under the appropriate unique names
			final Map<String, Object> springComponents = new PropertyMapBuilder<String, Object>()
					.put(SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE, new SimpleServiceImpl()).create();

			RoboSpringRegisterUnit registerUnit = new RoboSpringRegisterUnit(null, RoboSpringRegisterUnit.NAME);
			registerUnit.registerComponents(springComponents);
			result.add(registerUnit);

			configSpringUnit.setString(SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE,
					SimpleRoboSpringUnit.COMPONENT_SIMPLE_SERVICE);

			break;
		case WITHOUT_SERVICE:
			break;
		}

		result.add(SimpleRoboSpringUnit.class, configSpringUnit, SimpleRoboSpringUnit.NAME);

		return result.build();
	}
}
