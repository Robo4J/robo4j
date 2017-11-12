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

import com.robo4j.logging.SimpleLoggingUtil;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

/**
 * Handle OP_CONNECT
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public class ConnectSelectorHandler implements SelectorHandler {

    private final SelectionKey key;

    public ConnectSelectorHandler(SelectionKey key) {
        this.key = key;
    }

    @Override
    public SelectionKey handle() {
        try{
            ((SocketChannel) key.channel()).finishConnect();
        } catch (Exception e){
            SimpleLoggingUtil.error(getClass(), "handle connect", e);
        }
        return key;
    }
}
