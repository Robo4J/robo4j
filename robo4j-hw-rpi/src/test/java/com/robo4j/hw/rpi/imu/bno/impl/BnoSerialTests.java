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
package com.robo4j.hw.rpi.imu.bno.impl;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.imu.bno.impl.Bno055SerialDevice;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class BnoSerialTests {

	@Test
	void testBNOSerialReadRequest() {
		byte[] readRequest = Bno055SerialDevice.createReadRequest(0x20, 2);
		assertEquals(0xAA, 0xFF & readRequest[0]);
		assertEquals(0x01, readRequest[1]);
		assertEquals(0x20, readRequest[2]);
		assertEquals(2, readRequest[3]);
	}
}
