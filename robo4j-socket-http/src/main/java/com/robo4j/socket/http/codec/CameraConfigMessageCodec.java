package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpProducer;

/**
 *
 * Camera Image config codec
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */

@HttpProducer
public class CameraConfigMessageCodec extends AbstractHttpMessageCodec<CameraConfigMessage> {

	public CameraConfigMessageCodec() {
		super(CameraConfigMessage.class);
	}
}
