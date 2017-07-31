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
package com.robo4j.hw.rpi.imu.impl;

import org.junit.Assert;
import org.junit.Test;

public class BNOSerialTests {

//	@Test
// FIXME: (Marcus 31.07.17) we need to fix the test :)
	public void testBNOSerialReadRequest() {
		byte[] readRequest = BNO055SerialDevice.createReadRequest(0x20, 2);
		Assert.assertEquals(0xAA, 0xFF & readRequest[0]);
		Assert.assertEquals(0x00, readRequest[1]);
		Assert.assertEquals(0x20, readRequest[2]);
		Assert.assertEquals(2, readRequest[3]);
	}
}
