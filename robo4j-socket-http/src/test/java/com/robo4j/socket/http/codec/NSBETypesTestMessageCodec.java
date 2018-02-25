package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.test.codec.NSBETypesTestMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class NSBETypesTestMessageCodec extends AbstractHttpMessageCodec<NSBETypesTestMessage> {
    public NSBETypesTestMessageCodec() {
        super(NSBETypesTestMessage.class);
    }
}
