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
import com.robo4j.socket.http.message.DatagramDecoratedRequest;
import com.robo4j.socket.http.message.DatagramDenominator;
import com.robo4j.socket.http.util.DatagramBodyType;

import static com.robo4j.socket.http.util.RoboHttpUtils.PROPERTY_CODEC_REGISTRY;

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


        final String encodedMessage = clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY)
                .containsEncoder(message.getClazz()) ? processMessage(message.getClazz(), message.getMessage())
                : NOT_AVAILABLE;

        final ClientPathConfig pathConfig = clientContext.getPathConfig(message.getPath());
        final DatagramDenominator denominator = new DatagramDenominator(DatagramBodyType.JSON.getType(), pathConfig.getPath());
        final DatagramDecoratedRequest request = new DatagramDecoratedRequest(denominator);
        request.addMessage(encodedMessage.getBytes());

        getContext().getReference(target).sendMessage(request);
    }

    @SuppressWarnings("unchecked")
    private<T, R> R processMessage(Class<T> clazz, Object message) {
        return processMessage((T)message,
                clientContext.getPropertySafe(CodecRegistry.class, PROPERTY_CODEC_REGISTRY).getEncoder(clazz));
    }

    private <T, R> R processMessage(T message, SocketEncoder<T, R> encoder) {
        return encoder.encode(message);
    }

}
