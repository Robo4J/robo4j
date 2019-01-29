/*
 * Copyright (c) 2014-2019, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http.request;

import com.robo4j.RoboReference;

/**
 * Datagram read channel process wrapper
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public final class DatagramResponseProcess implements ChannelResponseProcess<RoboReference<Object>> {

    private final String path;
    private final RoboReference<Object> target;
    private final Object result;

    public DatagramResponseProcess(String path, RoboReference<Object> target, Object result) {
        this.path = path;
        this.target = target;
        this.result = result;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public RoboReference<Object> getTarget() {
        return target;
    }

    @Override
    public Object getResult() {
        return result;
    }

    @Override
    public String toString() {
        return "DatagramResponseProcess{" +
                "path='" + path + '\'' +
                ", target='" + target + '\'' +
                ", result=" + result +
                '}';
    }
}
