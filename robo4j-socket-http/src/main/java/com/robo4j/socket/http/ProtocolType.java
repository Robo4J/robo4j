/*
 * Copyright (c) 2014, 2023, Marcus Hirt, Miroslav Wengner
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
package com.robo4j.socket.http;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public enum  ProtocolType {

    //@formatter:off
    HTTP    (80, "http"),
    HTTPS   (443, "https");
    //@formatter:on

    private static Map<String, ProtocolType> mapByName;

    private final int port;
    private final String name;


    ProtocolType(int port, String name) {
        this.port = port;
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    // Utils Method
    public static ProtocolType getByName(String name) {
        if (Objects.isNull(mapByName)) {
            mapByName = iniMapping();
        }
        return mapByName.get(name);
    }

    // Private Methods
    private static Map<String, ProtocolType> iniMapping() {
        return Stream.of(values()).collect(Collectors.toMap(ProtocolType::getName, e -> e));
    }

    @Override
    public String toString() {
        return "ProtocolType{" +
                "port=" + port +
                ", name='" + name + '\'' +
                '}';
    }
}
