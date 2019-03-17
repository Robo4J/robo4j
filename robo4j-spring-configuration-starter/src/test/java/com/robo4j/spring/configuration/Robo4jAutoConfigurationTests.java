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

import com.robo4j.LifecycleState;
import com.robo4j.RoboContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = { AbstractRobo4jSpringTest.Initializer.class })
class Robo4jAutoConfigurationTests {

	@Autowired
	private RoboContext roboContext;

	@BeforeEach
	void setUp() {
		roboContext.stop();
	}

	@Test
	void statRoboContextTest() {
		roboContext.start();
		assertEquals(LifecycleState.STARTED, roboContext.getState());
	}

	@Test
	void stopRoboContextTest() {
		roboContext.start();
		roboContext.stop();
		assertEquals(LifecycleState.STOPPED, roboContext.getState());
	}

	@Test
	void unitRoboContextTest() {
		roboContext.start();
		assertNotNull(roboContext.getUnits());
		assertTrue(!roboContext.getUnits().isEmpty());
	}

}
