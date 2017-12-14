package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.CameraConfigMessage;
import com.robo4j.socket.http.codec.CameraConfigMessageCodec;
import com.robo4j.socket.http.util.JsonUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Camera Image, Config related tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraCodecTests {

    private CameraConfigMessageCodec codec;

    @Before
    public void setUp(){
        codec = new CameraConfigMessageCodec();
    }

	@Test
	public void cameraConfigCodecTest() {
		String desiredJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
		CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

		String encodeJson = codec.encode(message);

		CameraConfigMessage decodeJson = codec.decode(desiredJson);

		Assert.assertTrue("encode json", encodeJson.equals(desiredJson));
		Assert.assertTrue("decode json", decodeJson.equals(message));
	}

	@Test
    public void cameraConfigCodecRandomOrderTest(){
        String properOrderJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
        String testJson = "{\"width\":600,\"height\":800,\"brightness\":80,\"timeout\":2,\"timelapse\":100,\"sharpness\":56}";
        CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

        String encodeJson = codec.encode(message);

        CameraConfigMessage decodeJson = codec.decode(testJson);

        Assert.assertTrue("encode json", encodeJson.equals(properOrderJson));
        Assert.assertTrue("decode json", decodeJson.equals(message));
    }

    @Test
    public void cameraConfigCodecNotFullTest(){
        String properOrderJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
        String testJson = "{\"width\":600,\"height\":800,\"brightness\":80,\"timeout\":2}";

        Map<String, Object> map = JsonUtil.getMapByJson(testJson);
        CameraConfigMessage configMessage = codec.decode(properOrderJson);

        System.out.println("configMessage map:" + map);
        System.out.println("configMessage:" + configMessage);
    }

    @Test
    public void cameraConfigCodecDifferentTypeValuesTest(){

    }
}
