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
package com.robo4j.socket.http.units;

/**
 * ClientMessageWrapper wraps information for appropriate codec client
 *
 * @see DatagramClientCodecUnit
 * @see HttpClientCodecUnit
 *
 * @author Marcus Hirt (@hirt)
 * @author Miroslav Wengner (@miragemiko)
 */
public class ClientMessageWrapper {

    private String path;
    private Class<?> clazz;
    private Object message;

    public ClientMessageWrapper(String path, Class<?> clazz, Object message) {
        this.path = path;
        this.clazz = clazz;
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Object getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ClientMessageWrapper{" +
                "path='" + path + '\'' +
                ", clazz=" + clazz +
                ", message=" + message +
                '}';
    }
}
