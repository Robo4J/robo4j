/*
 * Copyright (c) 2014, 2018, Marcus Hirt, Miroslav Wengner
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

import com.robo4j.RoboContext;

/**
 * DatagramClientCodecUnit decorates message the Datagram Client
 *
 * @see DatagramClientUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DatagramClientCodecUnit extends AbstractClientCodecUnit {

    private static final String NOT_AVAILABLE = "not available";

    public DatagramClientCodecUnit(RoboContext context, String id) {
        super(ClientMessageWrapper.class, context, id);
    }

    @Override
    public void onMessage(ClientMessageWrapper message) {


//        final Object encodedMessage = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY)
//                .containsEncoder(message.getClazz()) ? processMessage(message.getClazz())
//                : NOT_AVAILABLE;

    }

//    private <T> Object processMessage(Class<T> clazz, T message){
//         return clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY).getEncoder(clazz).encode(message);
//    }

}
