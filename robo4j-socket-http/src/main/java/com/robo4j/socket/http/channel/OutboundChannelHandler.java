/*
 * Copyright (c) 2014, 2017, Marcus Hirt, Miroslav Wengner
 *
 * Robo4J is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Robo4J is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.channel;

import com.robo4j.socket.http.HttpMessageDescriptor;
import com.robo4j.socket.http.SocketException;
import com.robo4j.socket.http.dto.PathMethodDTO;
import com.robo4j.socket.http.util.ChannelBufferUtils;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.List;

/**
 *
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class OutboundChannelHandler implements SocketHandler {

    private ByteChannel byteChannel;
    private List<PathMethodDTO> targetUnitByMethodMap;
    private HttpMessageDescriptor message;
    private HttpMessageDescriptor receivedMessage;

    public  OutboundChannelHandler(List<PathMethodDTO> targetUnitByMethodMap, ByteChannel byteChannel, HttpMessageDescriptor message) {
        this.targetUnitByMethodMap = targetUnitByMethodMap;
        this.byteChannel = byteChannel;
        this.message = message;
    }

    @Override
    public void start() {
        final PathMethodDTO pathMethod = new PathMethodDTO(message.getPath(), message.getMethod(), null);
        if(targetUnitByMethodMap.contains(pathMethod)){
            final ByteBuffer buffer = processMessageToClient(message);
            switch (message.getMethod()) {
                case GET:
                case HEAD:
                    break;
                case PUT:
                case POST:
                case PATCH:
                    try {
                        receivedMessage = ChannelBufferUtils.getHttpMessageDescriptorByChannel(byteChannel);
                    } catch (Exception e){
                        throw new SocketException("message body write problem", e);
                    }
                    break;
                case TRACE:
                case OPTIONS:
                case DELETE:
                default:
                    throw new SocketException(String.format("not implemented method: %s", message));
            }
            buffer.clear();
        }
    }

    @Override
    public void stop() {
        try {
            byteChannel.close();
        } catch (Exception e){
            throw new SocketException("closing channel problem", e);
        }
    }

    public HttpMessageDescriptor getReceivedMessage() {
        return receivedMessage;
    }

    private ByteBuffer processMessageToClient(HttpMessageDescriptor message) {
        byte[] requestBytes = message.getMessage().getBytes();
        ByteBuffer buffer = ByteBuffer.allocate(requestBytes.length);
        buffer.put(requestBytes);
        buffer.flip();
        return buffer;
    }
}
