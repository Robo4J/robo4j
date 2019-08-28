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
package com.robo4j.hw.rpi.serial.ydlidar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseMode;
import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseType;

/**
 * Tests for the response headers.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ResponseHeaderTests {
	@Test
	void testInvalidHeader() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99, (byte) 0x99 };
		ResponseHeader header = new ResponseHeader(data);
		assertFalse(header.isValid(), "This valid should be invalid!");
	}

	@Test
	void testInvalidLength() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99 };
		try {
			new ResponseHeader(data);
			assertTrue(false, "This header should fail!");
		} catch (IllegalArgumentException e) {
		}
	}

	@Test
	void testValidDeviceInfoHeader() {
		byte[] data = new byte[] { (byte) 0xA5, (byte) 0x5A, (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x04 };
		ResponseHeader header = new ResponseHeader(data);
		assertTrue(header.isValid(), "This header should be valid!");
		assertSame(header.getResponseType(), ResponseType.DEVICE_INFO);
		assertSame(header.getResponseMode(), ResponseMode.SINGLE);
		assertEquals(header.getResponseLength(), 0x14);
	}

	@Test
	void testValidHealthInfoHeader() {
		byte[] data = new byte[] { (byte) 0xA5, (byte) 0x5A, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06 };
		ResponseHeader header = new ResponseHeader(data);
		assertTrue(header.isValid(), "This header should be valid!");
		assertSame(header.getResponseType(), ResponseType.DEVICE_HEALTH);
		assertSame(header.getResponseMode(), ResponseMode.SINGLE);
		assertEquals(header.getResponseLength(), 0x03);
	}

	@Test
	void testScanResponseHeader() {
		byte[] data = new byte[] { (byte) -91, (byte) 90, (byte) 5, (byte) 0x00, (byte) 0x00, (byte) 64, (byte) -127 };
		ResponseHeader header = new ResponseHeader(data);
		assertTrue(header.isValid(), "This header should be valid!");
		assertSame(header.getResponseType(), ResponseType.MEASUREMENT);
		assertSame(header.getResponseMode(), ResponseMode.CONTINUOUS);
	}
}
