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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.serial.ydlidar.DataHeader.PacketType;

/**
 * Tests for the data headers.
 * 
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class DataHeaderTests {
	@Test
	void testInvalidLengthHeader() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99, (byte) 0x99 };
		try {
			new DataHeader(data);
			assertTrue(false, "Should have thrown exception!");
		} catch (IllegalArgumentException e) {

		}
	}

	@Test
	void testInvalidHeader() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99, (byte) 0x99, (byte) 0x99,
				(byte) 0x99, (byte) 0x99 };
		DataHeader header = new DataHeader(data);
		assertTrue(!header.isValid(), "Should have been invalid header!");
	}

	@Test
	void testValidPacketHeader() {
		byte[] data = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99, (byte) 0x99, (byte) 0x99,
				(byte) 0x99, (byte) 0x99 };
		DataHeader header = new DataHeader(data);
		assertTrue(header.isValid(), "Should have been valid packet header!");
	}

	@Test
	void testCloudType() {
		byte[] data = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0xA4, (byte) 0xA5, (byte) 0x99, (byte) 0x99, (byte) 0x99,
				(byte) 0x99, (byte) 0x99 };
		DataHeader header = new DataHeader(data);
		assertSame(header.getPacketType(), PacketType.POINT_CLOUD, "Should have been point cloud!");
	}

	@Test
	void testLength() {
		byte[] data = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x08, (byte) 0xE5, (byte) 0x6F, (byte) 0xBD, (byte) 0x79,
				(byte) 0x12, (byte) 0x34 };
		DataHeader header = new DataHeader(data);
		assertEquals(header.getDataLength(), 0x10, "Got wrong length");
	}

	@Test
	void testAngle() {
		byte[] data = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x28, (byte) 0xE5, (byte) 0x6F, (byte) 0xBD, (byte) 0x79,
				(byte) 0x12, (byte) 0x34 };
		DataHeader header = new DataHeader(data);
		assertEquals(0x50, header.getDataLength(), "Got wrong length");
		assertEquals(0x6FE5, header.getFSA(), "Wrong start angle factor");
		assertEquals(0x79BD, header.getLSA(), "Wrong end angle factor");

		// These values are from the examples - since Java is doing many of the
		// calculations in double precision it's fine to not get exactly the
		// same results.

		float startAngle = header.getAngleAt(0, 1000, 0);

		float endAngle = header.getAngleAt(header.getSampleCount() - 1, 8000, 0);

		float midAngle = header.getAngleAt((header.getSampleCount() - 1) / 2, 4000, DataHeader.getAngularDiff(startAngle, endAngle));
		assertEquals(217.0178, startAngle, 0.005);
		assertEquals(235.6326, endAngle, 0.005);
		assertTrue(midAngle > startAngle);
		assertTrue(midAngle < endAngle);
	}

	@Test
	void testChecksum() {
		byte[] data = new byte[] { (byte) 0xAA, (byte) 0x55, (byte) 0x00, (byte) 0x28, (byte) 0xE5, (byte) 0x6F, (byte) 0xBD, (byte) 0x79,
				(byte) 0x12, (byte) 0x34 };
		DataHeader header = new DataHeader(data);
		assertEquals(0x3412, header.getExpectedChecksum());
	}
}
