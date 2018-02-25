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

import com.robo4j.socket.http.codec.CameraMessage;
import com.robo4j.socket.http.codec.SimpleCommand;
import com.robo4j.socket.http.codec.SimpleCommandCodec;
import com.robo4j.socket.http.units.test.codec.TestArrayDecoder;
import com.robo4j.socket.http.units.test.codec.TestArrayEncoder;
import com.robo4j.socket.http.units.test.enums.TestCommandEnum;
import com.robo4j.socket.http.util.HttpUnitHelper;
import com.robo4j.util.StreamUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Base64;

/**
 *
 * Http units coder, decoder related tests
 *
 * @see HttpServerUnit
 * @see HttpClientUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class HttpUnitTests {
	@Test
	public void testArrayDecoder() {
		TestArrayDecoder arrayDecoder = new TestArrayDecoder();
		String[] array = arrayDecoder.decode("[]{}}{");
		Assert.assertArrayEquals(new String[] { "" }, array);
		Assert.assertArrayEquals(new String[] { "Lalaa", "Lalala" }, arrayDecoder.decode("Lalaa,Lalala"));
	}

	@Test
	public void testArrayEncoder() {
		TestArrayEncoder arrayEncoder = new TestArrayEncoder();
		String json = arrayEncoder.encode(new String[] { "A", "B", "C" });
		Assert.assertEquals("{array:[A,B,C]}", json);
	}

	@Test
	public void testSimpleCommandCodecEncoded() {
		SimpleCommand simpleCommand = new SimpleCommand("move");
		String jsonString = "{\"value\":\"move\"}";
		SimpleCommandCodec simpleCommandCodec = new SimpleCommandCodec();
		SimpleCommand decSimpleCommand = simpleCommandCodec.decode(jsonString);

		String enSimpleCommand = simpleCommandCodec.encode(simpleCommand);

		Assert.assertEquals(enSimpleCommand, jsonString);
		Assert.assertEquals(simpleCommand.getValue(), decSimpleCommand.getValue());
	}

	@Test
	public void testHttpCodecRegistry() {
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.units.test.codec");
		SocketEncoder<String[], String> encoder = registry.getEncoder(String[].class);
		SocketDecoder<String, String[]> decoder = registry.getDecoder(String[].class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		String[] originalData = new String[] { "A", "B", "C" };
		String encoded = encoder.encode(originalData);
		String[] decoded = decoder.decode(encoded);

		Assert.assertArrayEquals(originalData, decoded);
	}

	@Test
	public void testHttpCodecRegistryCodec() {
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.units.test.codec");
		SocketEncoder<String, String> encoder = registry.getEncoder(String.class);
		SocketDecoder<String, String> decoder = registry.getDecoder(String.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		String originalData = "Oh my god, it's full of stars";
		String encoded = encoder.encode(originalData);
		String decoded = decoder.decode(encoded);

		Assert.assertEquals(originalData, decoded);
	}

	/**
	 * Translates Enum to String and otherwise
	 */
	@Test
	public void testHttpTestCommandValueEnumMessage() {
		final String jsonCorruptedString = "{  \"value\" :  \"move\"  }";
		final String jsonProperString = "{\"value\":\"move\"}";
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.units.test.codec");
		SocketEncoder<TestCommandEnum, String> encoder = registry.getEncoder(TestCommandEnum.class);
		SocketDecoder<String, TestCommandEnum> decoder = registry.getDecoder(TestCommandEnum.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		TestCommandEnum originalData = TestCommandEnum.MOVE;
		String encoded = encoder.encode(TestCommandEnum.MOVE);

		TestCommandEnum decoded = decoder.decode(jsonCorruptedString);

		Assert.assertEquals(originalData, decoded);
		Assert.assertEquals(encoded, jsonProperString);
	}

	@Test
	public void testHttpTestCommandValueCorrectEnumMessage() {
		final String jsonProperString = "{\"value\":\"move\"}";
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.units.test.codec");
		SocketEncoder<TestCommandEnum, String> encoder = registry.getEncoder(TestCommandEnum.class);
		SocketDecoder<String, TestCommandEnum> decoder = registry.getDecoder(TestCommandEnum.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		TestCommandEnum originalData = TestCommandEnum.MOVE;
		String encoded = encoder.encode(TestCommandEnum.MOVE);

		TestCommandEnum decoded = decoder.decode(jsonProperString);

		Assert.assertEquals(originalData, decoded);
		Assert.assertEquals(encoded, jsonProperString);
	}

	@Test
	public void testHttpCameraMessage() {
		final String jsonCammeraMessageCorrupted = "{ \"type\"  :  \"jpg\" ,  \"value\"   :  \"description\"  ,\"image\":\"12345\"}";
		final String jsonCammeraMessage = "{\"type\":\"jpg\",\"value\":\"description\",\"image\":\"12345\"}";
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.codec");
		SocketEncoder<CameraMessage, String> encoder = registry.getEncoder(CameraMessage.class);
		SocketDecoder<String, CameraMessage> decoder = registry.getDecoder(CameraMessage.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		CameraMessage cameraMessage = new CameraMessage("jpg", "description", "12345");
		String encoded = encoder.encode(cameraMessage);
		CameraMessage decoded = decoder.decode(jsonCammeraMessageCorrupted);

		Assert.assertEquals(jsonCammeraMessage, encoded);
		Assert.assertEquals(cameraMessage.getType(), decoded.getType());
		Assert.assertEquals(cameraMessage.getValue(), decoded.getValue());

	}

	@Test
	public void testHttpCameraMessageImage() throws Exception {

		final InputStream imageData = new BufferedInputStream(
				Thread.currentThread().getContextClassLoader().getResourceAsStream("snapshot.png"));
		byte[] imageArray = StreamUtils.inputStreamToByteArray(imageData);

		String encodedImage = Base64.getEncoder().encodeToString(imageArray);

		final String jsonCameraMessageCorrupted = "{ \"type\"  :  \"jpg\" ,  \"value\"   :  \"description\"  ,\"image\":\""
				+ encodedImage + "\"}";
		final String jsonCameraMessage = "{\"type\":\"jpg\",\"value\":\"description\",\"image\":\"" + encodedImage
				+ "\"}";
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.codec");
		SocketEncoder<CameraMessage, String> encoder = registry.getEncoder(CameraMessage.class);
		SocketDecoder<String, CameraMessage> decoder = registry.getDecoder(CameraMessage.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		CameraMessage cameraMessage = new CameraMessage("jpg", "description", encodedImage);
		String encoded = encoder.encode(cameraMessage);
		CameraMessage decoded = decoder.decode(jsonCameraMessageCorrupted);

		final byte[] imageDecoded = Base64.getDecoder().decode(decoded.getImage());

		Assert.assertEquals(jsonCameraMessage, encoded);
		Assert.assertEquals(cameraMessage.getType(), decoded.getType());
		Assert.assertEquals(cameraMessage.getValue(), decoded.getValue());
		Assert.assertEquals(imageArray.length, imageDecoded.length);
	}

	@Test
	public void testHttpCameraRealMessageImage() throws Exception {

		String encodedImage = HttpUnitHelper.getExampleCamera();

		final String jsonCammeraMessageCorrupted = "{ \"type\"  :  \"jpg\" ,  \"value\"   :  \"description\"  ,\"image\":\""
				+ encodedImage + "\"}";
		final String jsonCammeraMessage = "{\"type\":\"jpg\",\"value\":\"description\",\"image\":\"" + encodedImage
				+ "\"}";
		CodecRegistry registry = new CodecRegistry("com.robo4j.socket.http.codec");
		SocketEncoder<CameraMessage, String> encoder = registry.getEncoder(CameraMessage.class);
		SocketDecoder<String, CameraMessage> decoder = registry.getDecoder(CameraMessage.class);
		Assert.assertNotNull(encoder);
		Assert.assertNotNull(decoder);

		CameraMessage cameraMessage = new CameraMessage("jpg", "description", encodedImage);
		String encoded = encoder.encode(cameraMessage);
		CameraMessage decoded = decoder.decode(jsonCammeraMessageCorrupted);

		final byte[] imageDecoded = Base64.getDecoder().decode(decoded.getImage());

		Assert.assertEquals(jsonCammeraMessage, encoded);
		Assert.assertEquals(cameraMessage.getType(), decoded.getType());
		Assert.assertEquals(cameraMessage.getValue(), decoded.getValue());
		Assert.assertNotNull(imageDecoded);
		Assert.assertTrue(imageDecoded.length > 0);
	}

}
