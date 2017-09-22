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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.units;

import com.robo4j.RoboBuilder;
import com.robo4j.RoboContext;
import com.robo4j.socket.http.units.test.HttpCommandTestController;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * For target unit U defined by ID, we specify one of possible HTTP methods
 * (GET, POST,...) Such target unit U is registered by http server unit S by
 * usage of HttpUriRegister R.
 *
 * Test is focused on test such potential register R.
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpUriRegisterTests {
	private static final String PATH_UNITS = "units/";
	private static final String TARGET_UNIT_CONTROLLER = "controller";
	private static final String[] METHODS = { "GET", "POST" };

	@Test
	public void registerSimpleTest() {

		/* tested system configuration */
		RoboBuilder builder = new RoboBuilder();
		RoboContext system = builder.build();

		HttpUriRegister register = HttpUriRegister.getInstance();

		register.addUnitPathNode(TARGET_UNIT_CONTROLLER, METHODS[0]);
		register.addUnitPathNode(TARGET_UNIT_CONTROLLER, METHODS[1]);

		HttpCommandTestController ctrl = new HttpCommandTestController(system, TARGET_UNIT_CONTROLLER);
		String path = PATH_UNITS.concat(TARGET_UNIT_CONTROLLER);
		register.addUnitToNode(TARGET_UNIT_CONTROLLER, ctrl);
		assertNotNull(register.getMethodsBytPath(path));
		assertEquals(register.getMethodsBytPath(path).getUnit(), ctrl);
		assertTrue(register.getMethodsBytPath(path).getMethods().containsAll(Arrays.asList(METHODS)));
	}

}
