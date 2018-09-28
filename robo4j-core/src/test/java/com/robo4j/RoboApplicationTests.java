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

package com.robo4j;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * RoboApplicationTests contains tests for {@link RoboApplication}
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class RoboApplicationTests {

	/**
	 * Build and shutdown the system
	 * 
	 * @throws RoboBuilderException
	 *             unexpected
	 */
	@Test
	public void roboApplicationLifeCycleTestNoExit() throws RoboBuilderException {
		final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		final RoboBuilder builder = new RoboBuilder();
		builder.add(contextIS);

		final RoboContext system = builder.build();
		final RoboApplication roboApp = new RoboApplication();
		roboApp.launchNoExit(system, 3, TimeUnit.SECONDS);

		Assert.assertEquals(LifecycleState.SHUTDOWN, system.getState());
	}

	/**
	 * Build and shutdown the system
	 * 
	 * @throws RoboBuilderException
	 *             unexpected
	 */
	@Ignore
	@Test
	public void roboApplicationLifeCycleTestWithExit() throws RoboBuilderException {
		final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
		final RoboBuilder builder = new RoboBuilder();
		builder.add(contextIS);

		final RoboContext system = builder.build();
		final RoboApplication roboApp = new RoboApplication();
		roboApp.launchWithExit(system, 3, TimeUnit.SECONDS);

		System.setSecurityManager(new SecurityManager() {
			@Override
			public void checkExit(int status) {
				Assert.assertEquals(Integer.valueOf(0), Integer.valueOf(status));
			}
		});
	}

	@Ignore
	@Test
    public void roboApplicationLifeCycle() throws RoboBuilderException {
        final InputStream contextIS = Thread.currentThread().getContextClassLoader().getResourceAsStream("test.xml");
        final RoboBuilder builder = new RoboBuilder();
        builder.add(contextIS);

        final RoboContext system = builder.build();
        final RoboApplication roboApp = new RoboApplication();
        roboApp.launch(system);
    }
}
