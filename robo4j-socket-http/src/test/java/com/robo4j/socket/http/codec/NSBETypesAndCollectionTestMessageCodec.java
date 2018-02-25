package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.test.codec.NSBETypesAndCollectionTestMessage;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class NSBETypesAndCollectionTestMessageCodec extends AbstractHttpMessageCodec<NSBETypesAndCollectionTestMessage> {

    public NSBETypesAndCollectionTestMessageCodec() {
        super(NSBETypesAndCollectionTestMessage.class);
    }
}
