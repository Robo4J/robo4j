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
package com.robo4j;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RoboApplicationTests contains tests for {@link RoboApplication}
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@Disabled("individual test")
class RoboApplicationTests {

	/**
	 * Build and shutdown the system
	 * 
	 * @throws RoboBuilderException
	 *             unexpected
	 */
	@Test
	void roboApplicationLifeCycleTestNoExit() throws RoboBuilderException {
		final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		final RoboBuilder builder = new RoboBuilder();
		builder.add(contextIS);

		final RoboContext system = builder.build();
		final RoboApplication roboApp = new RoboApplication();
		roboApp.launchNoExit(system, 3, TimeUnit.SECONDS);

		assertEquals(LifecycleState.SHUTDOWN, system.getState());
	}

	/**
	 * Build and shutdown the system
	 * 
	 * @throws RoboBuilderException
	 *             unexpected
	 */
	@Test
	void roboApplicationLifeCycleTestWithExit() throws RoboBuilderException {
		final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		final RoboBuilder builder = new RoboBuilder();
		builder.add(contextIS);

		final RoboContext system = builder.build();
		final RoboApplication roboApp = new RoboApplication();
		roboApp.launchWithExit(system, 3, TimeUnit.SECONDS);

		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {
				assertEquals(Integer.valueOf(0), Integer.valueOf(status));
			}
		});
	}

	@Disabled("individual test")
	@Test
    void roboApplicationLifeCycle() throws RoboBuilderException {
        final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
        final RoboBuilder builder = new RoboBuilder();
        builder.add(contextIS);

        final RoboContext system = builder.build();
        final RoboApplication roboApp = new RoboApplication();
        roboApp.launch(system);
    }
}
