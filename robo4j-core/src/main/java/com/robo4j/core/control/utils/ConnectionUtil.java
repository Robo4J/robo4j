/*
 * Copyright (C) 2016. Miroslav Kopecky
 * This ConnectionUtil.java is part of robo4j.
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

package com.robo4j.core.control.utils;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Created by miroslavkopecky on 03/05/16.
 */
public final class ConnectionUtil {
    private static final int TIMEOUT = 2000;
    /* connect IP address */
    public static boolean ping(final String address) throws IOException {
        return InetAddress.getByName(address).isReachable(TIMEOUT);
    }

}
