package com.robo4j.socket.http.units.test.codec;

import com.robo4j.socket.http.codec.CameraConfigMessage;
import com.robo4j.socket.http.codec.CameraConfigMessageCodec;
import org.junit.Assert;
import org.junit.Test;

/**
 * Camera Image, Config related tests
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class CameraCodecTests {


    @Test
    public void cameraConfigCodecTest(){
        String desiredJson = "{\"height\":800,\"width\":600,\"brightness\":80,\"sharpness\":56,\"timeout\":2,\"timelapse\":100}";
        CameraConfigMessage message = new CameraConfigMessage(800, 600, 80, 56, 2, 100);

        CameraConfigMessageCodec codec = new CameraConfigMessageCodec();
        String encodeJson = codec.encode(message);

        CameraConfigMessage decodeJson = codec.decode(desiredJson);

        Assert.assertTrue("encode json", encodeJson.equals(desiredJson));
        Assert.assertTrue("decode json", decodeJson.equals(message));
    }
}
