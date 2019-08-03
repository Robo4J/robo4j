package com.robo4j.hw.rpi.serial.ydlidar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseMode;
import com.robo4j.hw.rpi.serial.ydlidar.ResponseHeader.ResponseType;

public class ResponseHeaderTests {
	@Test
	void testInvalidHeader() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99,
				(byte) 0x99 };
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
	void testValidHeader() {
		byte[] data = new byte[] { (byte) 0xA5, (byte) 0x5A, (byte) 0x14, (byte) 0x00, (byte) 0x00, (byte) 0x00,
				(byte) 0x04 };
		ResponseHeader header = new ResponseHeader(data);
		assertTrue(header.isValid(), "This header should be valid!");
		assertSame(header.getResponseType(), ResponseType.DEVICE_INFO);
		assertSame(header.getResponseMode(), ResponseMode.SINGLE);
		assertEquals(header.getResponseLength(), 0x14);
	}
}
