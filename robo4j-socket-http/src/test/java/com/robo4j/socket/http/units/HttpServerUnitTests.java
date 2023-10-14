/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.units;

import com.robo4j.LifecycleState;
import com.robo4j.RoboBuilder;
import com.robo4j.RoboBuilderException;
import com.robo4j.RoboContext;
import com.robo4j.RoboReference;
import com.robo4j.configuration.Configuration;
import com.robo4j.configuration.ConfigurationBuilder;
import com.robo4j.util.SystemUtil;
import org.junit.jupiter.api.Test;

import static com.robo4j.socket.http.units.RoboHttpPingPongTest.PACKAGE_CODECS;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_PACKAGES;
import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_SOCKET_PORT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class HttpServerUnitTests {
	private static final int PORT = 9000;
	private static final String ID_HTTP_SERVER = "empty_server";

	@Test
	void httpServerUnitNoCodecsPackageTest() throws Exception {

		Throwable exception = assertThrows(RoboBuilderException.class, () -> {
			RoboBuilder builder = new RoboBuilder();

			Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT).build();
			builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
			RoboContext system = builder.build();

			system.start();
			System.out.println("system: State after start:");
			System.out.println(SystemUtil.printStateReport(system));
			RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
			system.shutdown();
			System.out.println("system: State after shutdown:");
			System.out.println(SystemUtil.printStateReport(system));
			assertEquals(LifecycleState.SHUTDOWN, systemReference.getState());
		});

		assertEquals("Error initializing RoboUnit", exception.getMessage());

	}

	@Test
	void httpServerUnitNoPathTest() throws Exception {
		RoboBuilder builder = new RoboBuilder();

		Configuration config = new ConfigurationBuilder().addInteger(PROPERTY_SOCKET_PORT, PORT)
				.addString(PROPERTY_CODEC_PACKAGES, PACKAGE_CODECS).build();
		builder.add(HttpServerUnit.class, config, ID_HTTP_SERVER);
		RoboContext system = builder.build();

		system.start();
		System.out.println("system: State after start:");
		System.out.println(SystemUtil.printStateReport(system));
		RoboReference<HttpServerUnit> systemReference = system.getReference(ID_HTTP_SERVER);
		system.shutdown();
		System.out.println("system: State after shutdown:");
		System.out.println(SystemUtil.printStateReport(system));
		assertEquals(LifecycleState.SHUTDOWN, systemReference.getState());
	}

}
