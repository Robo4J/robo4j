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
