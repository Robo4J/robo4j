package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.CameraConfigMessage;
import com.robo4j.socket.http.codec.CameraConfigMessageCodec;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Camera Image, Config related tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraCodecTests {

	private CameraConfigMessageCodec codec;

	@Before
	public void setUp() {
		codec = new CameraConfigMessageCodec();
	}

	@Test
	public void cameraConfigCodecTest() {
		String properJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		String desiredJson = "{\"height\":800 ,\"width\" :600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

		String encodeJson = codec.encode(message);

		CameraConfigMessage decodeJson = codec.decode(desiredJson);

		Assert.assertTrue("encode json", encodeJson.equals(properJson));
		Assert.assertTrue("decode json", decodeJson.equals(message));
	}

	@Test
	public void cameraConfigCodecRandomOrderTest() {
		String properOrderJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		String inputOrderJson = "{\"height\":800 , \"width\":600,\"brightness\":80,\"sharpness\" : 56,\"timeout\" :2,\"timelapse\": 100}";
		CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

		String encodeJson = codec.encode(message);

		CameraConfigMessage decodeJson = codec.decode(inputOrderJson);

		Assert.assertTrue("encode json", encodeJson.equals(properOrderJson));
		Assert.assertTrue("decode json", decodeJson.equals(message));
	}

	@Test
	public void cameraConfigCodecNotFullTest() {
		String properOrderJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";

		CameraConfigMessage configMessage = codec.decode(properOrderJson);

		System.out.println("configMessage:" + configMessage);
	}

	@Test
	public void cameraConfigCodecDifferentTypeValuesTest() {
		// TBD
	}
}
