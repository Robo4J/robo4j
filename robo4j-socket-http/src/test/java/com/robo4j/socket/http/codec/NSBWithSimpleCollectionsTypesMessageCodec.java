package com.robo4j.socket.http.codec;

import com.robo4j.socket.http.units.HttpProducer;
import com.robo4j.socket.http.units.test.codec.NSBWithSimpleCollectionsTypesMessage;

/**
 * Collection and various fields test codec
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
@HttpProducer
public class NSBWithSimpleCollectionsTypesMessageCodec
		extends AbstractHttpMessageCodec<NSBWithSimpleCollectionsTypesMessage> {

	public NSBWithSimpleCollectionsTypesMessageCodec() {
		super(NSBWithSimpleCollectionsTypesMessage.class);
	}
}
