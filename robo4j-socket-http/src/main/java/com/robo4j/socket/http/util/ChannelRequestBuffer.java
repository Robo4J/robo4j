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

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.HttpVersion;
import com.robo4j.socket.http.message.HttpDecoratedRequest;
import com.robo4j.socket.http.message.HttpRequestDenominator;

import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.util.HashMap;

/**
 * ChannelRequestBuffer
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ChannelRequestBuffer {

    private  final ByteBuffer requestBuffer = ByteBuffer.allocateDirect(ChannelBufferUtils.INIT_BUFFER_CAPACITY);

    public ChannelRequestBuffer() {
    }

    public HttpDecoratedRequest getHttpDecoratedRequestByChannel(ByteChannel channel) {
        final StringBuilder sbBasic = new StringBuilder();
        int readBytes = ChannelBufferUtils.readBytesByChannel(channel);
        if (readBytes != ChannelBufferUtils.BUFFER_MARK_END) {

            requestBuffer.flip();
            ChannelBufferUtils.addToStringBuilder(sbBasic, requestBuffer, readBytes);
            final StringBuilder sbAdditional = new StringBuilder();
            final HttpDecoratedRequest result = ChannelBufferUtils.extractDecoratedRequestByStringMessage(sbBasic.toString());

            int totalReadBytes = readBytes;

            if (result.getLength() != 0) {
                while (totalReadBytes < result.getLength()) {
                    readBytes = ChannelBufferUtils.readBytesByChannel(channel);
                    requestBuffer.flip();
                    ChannelBufferUtils.addToStringBuilder(sbAdditional, requestBuffer, readBytes);

                    totalReadBytes += readBytes;
                    requestBuffer.clear();
                }
                if (sbAdditional.length() > 0) {
                    result.addMessage(sbAdditional.toString());
                }
            }
            requestBuffer.clear();
            return result;
        } else {
            return new HttpDecoratedRequest(new HashMap<>(), new HttpRequestDenominator(HttpMethod.GET, HttpVersion.HTTP_1_1));
        }
    }
}
