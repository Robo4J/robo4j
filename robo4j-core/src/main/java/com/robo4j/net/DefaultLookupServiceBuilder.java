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

package com.robo4j.net;

import com.robo4j.logging.SimpleLoggingUtil;

import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Builder helps to build  {@link LookupService}
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class DefaultLookupServiceBuilder {

    private String address;
    private Integer port;
    private Float missedHeartbeatsBeforeRemoval;
    private LocalLookupServiceImpl localContexts;


    private DefaultLookupServiceBuilder(){

    }

    public static DefaultLookupServiceBuilder Build(){
        return new DefaultLookupServiceBuilder();
    }

    public DefaultLookupServiceBuilder setAddress(String address) {
        this.address = address;
        return this;
    }

    public DefaultLookupServiceBuilder setPort(int port) {
        this.port = port;
        return this;
    }

    public DefaultLookupServiceBuilder setMissedHeartbeatsBeforeRemoval(float missedHeartbeatsBeforeRemoval) {
        this.missedHeartbeatsBeforeRemoval = missedHeartbeatsBeforeRemoval;
        return this;
    }

    public DefaultLookupServiceBuilder setLocalContexts(LocalLookupServiceImpl localContexts) {
        this.localContexts = localContexts;
        return this;
    }

    public LookupService build(){
        try {
            return new LookupServiceImpl(address, port, missedHeartbeatsBeforeRemoval, localContexts);
        } catch (SocketException | UnknownHostException e) {
            SimpleLoggingUtil.error(LookupServiceProvider.class,
                    "Failed to set up LookupService! No multicast route? Will use null provider...", e);
            return new NullLookupService();
        }
    }
}
