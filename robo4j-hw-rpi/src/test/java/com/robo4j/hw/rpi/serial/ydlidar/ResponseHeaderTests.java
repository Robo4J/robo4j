package com.robo4j.hw.rpi.serial.ydlidar;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class ResponseHeaderTests {
	@Test
	void testInvalidHeader() {
		byte[] data = new byte[] { (byte) 0xA1, (byte) 0xA2, (byte) 0xA3, (byte) 0xA4, (byte) 0xA5, (byte) 0x99,
				(byte) 0x99 };
		ResponseHeader header = new ResponseHeader(data);
		assertFalse(header.isValid());
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

}
