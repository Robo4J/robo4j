/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This NetworkUtils.java is part of robo4j.
 *
 *     robo4j is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     robo4j is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with robo4j .  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.robo4j.core.io;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.util.function.Supplier;

/**
 * Simple network utils used for ping
 *
 * @author Miro Kopecky (@miragemiko)
 * @since 04.06.2016
 */
public final class NetworkUtils {

    private static final Logger logger = LoggerFactory.getLogger(NetworkUtils.class);
    private static final int DEFAULT_TIMEOUT = 5000;

    public static Supplier<Boolean> pingBrick(String address){
        return () -> {
            try {
                return InetAddress.getByName(address).isReachable(DEFAULT_TIMEOUT);
            } catch (IOException e) {
                logger.error("NETWORK PING error:", e);
                return false;
            }
        };
    }

}
