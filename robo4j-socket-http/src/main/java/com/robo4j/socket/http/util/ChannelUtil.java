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

package com.robo4j.socket.http.util;

import com.robo4j.logging.SimpleLoggingUtil;
import com.robo4j.socket.http.PropertiesProvider;
import com.robo4j.socket.http.SocketException;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;

import static com.robo4j.socket.http.util.RoboHttpUtils.HTTP_PROPERTY_PORT;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class ChannelUtil {
    /**
     * reading buffer
     *
     * @param channel
     * @param buffer
     * @return
     * @throws IOException
     */
    public static int readBuffer(ByteChannel channel, ByteBuffer buffer) throws IOException {
        int numberRead = channel.read(buffer);
        int position = 0;
        int totalRead = numberRead;
        while (numberRead >= 0 && position <= buffer.capacity()) {
            numberRead = channel.read(buffer);
            if (numberRead > 0) {
                totalRead += numberRead;
            }
            position++;
        }
        return totalRead;
    }

    /**
     * writing to channel buffer
     *
     * @param channel
     * @param buffer
     * @return
     * @throws IOException
     */
    public static int writeBuffer(ByteChannel channel, ByteBuffer buffer) throws IOException {
        int numberWriten = 0;
        int totalWritten = numberWriten;

        while (numberWriten >= 0 && buffer.hasRemaining()) {
            numberWriten = channel.write(buffer);
            totalWritten += numberWriten;
        }
        return totalWritten;
    }

    public static SelectionKey registerSelectionKey(ServerSocketChannel result){
        try{
            final Selector selector = Selector.open();
            return result.register(selector, SelectionKey.OP_ACCEPT);
        } catch (Exception e){
            SimpleLoggingUtil.error(ChannelUtil.class, "resister selection key", e);
            throw new SocketException("resister selection key", e);
        }
    }

    public static ServerSocketChannel initServerSocketChannel(PropertiesProvider properties) {
        try{
            ServerSocketChannel result = ServerSocketChannel.open();
            result.configureBlocking(false);
            result.bind(new InetSocketAddress(properties.getIntSafe(HTTP_PROPERTY_PORT)));
            return result;
        } catch (Exception e){
            SimpleLoggingUtil.error(ChannelUtil.class, "init server socket channel", e);
            throw new SocketException("init server socket channel", e);
        }
    }

    public static int getReadyChannelBySelectionKey(SelectionKey key){
        try {
            return  key.selector().select();
        } catch (Exception e){
            SimpleLoggingUtil.error(ChannelUtil.class, "get ready channel by selection key", e);
            throw new SocketException("get ready channel by selection key", e);
        }
    }

    /**
     * report time in moment M from start time
     *
     * @param message message
     * @param start   start time
     */
    public static void printMeasuredTime(Class<?> clazz, String message, long start) {
        System.out.println(String.format("%s message: %s duration: %d%n", clazz.getSimpleName(), message, System.currentTimeMillis() - start));
    }
}
