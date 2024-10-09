/*
 * Copyright (c) 2014, 2024, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.channel;

import com.robo4j.RoboContext;
import com.robo4j.socket.http.units.ClientContext;
import com.robo4j.socket.http.units.DatagramClientUnit;
import com.robo4j.socket.http.util.ChannelBufferUtils;
import com.robo4j.socket.http.util.ChannelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

/**
 * Inbound Datagram Handler for UDP client handles sending messages and
 * receiving response
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 * @see DatagramClientUnit
 */
public class OutboundDatagramSocketChannelHandler implements ChannelHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(OutboundDatagramSocketChannelHandler.class);
    private final RoboContext context;
    private final ClientContext clientContext;
    private final byte[] payload;
    private DatagramChannel channel;
    private volatile boolean active;

    public OutboundDatagramSocketChannelHandler(RoboContext context, ClientContext clientContext, byte[] payload) {
        this.context = context;
        this.clientContext = clientContext;
        this.payload = payload;
    }

    @Override
    public void start() {
        if (!active) {
            active = true;
            context.getScheduler().execute(() -> initDatagramSocket(clientContext));
        }
    }

    @Override
    public void stop() {
        try {
            if (channel != null) {
                active = false;
                if (channel.isConnected())
                    channel.close();
            }
        } catch (IOException e) {
            LOGGER.error("server stop problem:{}", e.getMessage(), e);
        }
    }

    private void initDatagramSocket(ClientContext clientContext) {
        channel = ChannelUtils.initDatagramChannel(DatagramConnectionType.CLIENT, clientContext);
        final ByteBuffer buffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);
        final SocketAddress address = ChannelUtils.getSocketAddressByContext(clientContext);
        // while (active.get()){
        try {
            buffer.clear();
            buffer.put(payload);
            buffer.flip();
            channel.send(buffer, address);
        } catch (Exception e) {
            LOGGER.error("datagram problem:{}", e.getMessage(), e);
        }

    }
}
