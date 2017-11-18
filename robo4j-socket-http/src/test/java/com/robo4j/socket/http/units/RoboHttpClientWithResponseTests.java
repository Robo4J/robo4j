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

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import org.junit.Test;

import java.io.InputStream;

/**
 * testing http method GET with response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class RoboHttpClientWithResponseTests {

	@Test
	public void simpleRoboSystemGetRequestTest() throws Exception {
		RoboBuilder builderConsumer = new RoboBuilder(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("robo4jSystemTest.xml"));
		InputStream serverConfigInputStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("robo_camara_consumer_test.xml");
		builderConsumer.add(serverConfigInputStream);
		RoboContext consumerSystem = builderConsumer.build();
	}

}
