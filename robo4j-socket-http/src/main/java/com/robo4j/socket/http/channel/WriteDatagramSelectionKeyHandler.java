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
import com.robo4j.RoboReference;
import com.robo4j.socket.http.request.DatagramResponseProcess;
import com.robo4j.socket.http.units.ServerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.channels.SelectionKey;
import java.util.Map;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class WriteDatagramSelectionKeyHandler implements SelectionKeyHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(WriteDatagramSelectionKeyHandler.class);
    private final Map<SelectionKey, DatagramResponseProcess> outBuffers;
    private final SelectionKey key;

    public WriteDatagramSelectionKeyHandler(RoboContext context, ServerContext serverContext,
                                            Map<SelectionKey, DatagramResponseProcess> outBuffers, SelectionKey key) {
        this.outBuffers = outBuffers;
        this.key = key;
    }

    @Override
    public SelectionKey handle() {
//		final DatagramChannel channel = (DatagramChannel) key.channel();
//		ByteBuffer buffer = ByteBuffer.allocate(serverContext.getPropertySafe(Integer.class, PROPERTY_BUFFER_CAPACITY));
        final DatagramResponseProcess responseProcess = outBuffers.get(key);

//		buffer.clear();
//		buffer.put("ACCEPTED".getBytes());
//		buffer.flip();
//		try {
//			channel.write(buffer);
//		} catch (IOException e) {
//			throw new SocketException("handle", e);
//		}

        RoboReference<Object> reference = responseProcess.getTarget();
        Object responseMessage = responseProcess.getResult();
        reference.sendMessage(responseMessage);
        LOGGER.debug("responseMessage:{}", responseMessage);

        key.cancel();
        return key;
    }
}
