package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.test.codec.NSBTypesTestMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class NSBTypesTestMessageCodec extends AbstractHttpMessageCodec<NSBTypesTestMessage> {
    public NSBTypesTestMessageCodec() {
        super(NSBTypesTestMessage.class);
    }
}
