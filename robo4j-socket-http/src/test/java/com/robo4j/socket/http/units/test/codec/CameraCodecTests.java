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
package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.CameraConfigMessage;
import com.robo4j.socket.http.codec.CameraConfigMessageCodec;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Camera Image, Config related tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
class CameraCodecTests {

	private CameraConfigMessageCodec codec;

	@BeforeEach
	void setUp() {
		codec = new CameraConfigMessageCodec();
	}

	@Test
	void cameraConfigCodecTest() {
		String properJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		String desiredJson = "{\"height\":800 ,\"width\" :600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

		String encodeJson = codec.encode(message);

		CameraConfigMessage decodeJson = codec.decode(desiredJson);

		assertEquals(properJson, encodeJson, "encode json");
		assertEquals(message, decodeJson, "decode json");
	}

	@Test
	void cameraConfigCodecRandomOrderTest() {
		String properOrderJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		String inputOrderJson = "{\"height\":800 , \"width\":600,\"brightness\":80,\"sharpness\" : 56,\"timeout\" :2,\"timelapse\": 100}";
		CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

		String encodeJson = codec.encode(message);

		CameraConfigMessage decodeJson = codec.decode(inputOrderJson);

		assertEquals(properOrderJson, encodeJson, "encode json");
		assertEquals(message, decodeJson, "decode json");
	}

}
