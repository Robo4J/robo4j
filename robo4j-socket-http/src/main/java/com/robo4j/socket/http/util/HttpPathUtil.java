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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Robo4J. If not, see <http://www.gnu.org/licenses/>.
 */

package com.robo4j.socket.http.util;

import com.robo4j.socket.http.HttpMethod;
import com.robo4j.socket.http.dto.PathMethodDTO;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utils for the path operation
 *
 * @author Marcus Hirt (@hirt)
 * @author Miro Wengner (@miragemiko)
 */
public final class HttpPathUtil {
    private static final int SEPARATOR_PATH = 12;

    /**
     *
     * @param source complete URI as string
     * @return list of paths
     */
    public static List<String> uriStringToPathList(String source){
        return Stream.of(source
                .split(HttpMessageUtil.getHttpSeparator(SEPARATOR_PATH)))
                .filter(e -> !e.isEmpty())
                .collect(Collectors.toList());
    }

    public static String pathsToUri(List<String> paths){
        return paths.stream().collect(Collectors.joining(HttpMessageUtil.getHttpSeparator(SEPARATOR_PATH)));
    }


    /**
     * Parsing string of JSON format to Set of PathMethodTargets
     *
     * @param value representing unit and accepted method
     * @return return map key
     */
    public static Set<PathMethodDTO> getPathMethodTargetByString(String value) {
        return JsonUtil.getMapNyJson(value)
                .entrySet().stream()
                .filter(e -> Objects.nonNull(e.getKey()) && Objects.nonNull(e.getKey()))
                .filter(e -> Objects.nonNull(HttpMethod.getByName(e.getValue().toString())))
                .map(e -> new PathMethodDTO(e.getKey(), HttpMethod.getByName(e.getValue().toString())))
                .collect(Collectors.toSet());
    }
}
